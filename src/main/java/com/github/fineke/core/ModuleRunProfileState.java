package com.github.fineke.core;

import com.google.gson.Gson;
import com.intellij.execution.*;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.configurations.RunProfileState;
import com.intellij.execution.filters.TextConsoleBuilderFactory;
import com.intellij.execution.impl.ConsoleViewImpl;
import com.intellij.execution.process.*;
import com.intellij.execution.remote.RemoteConfiguration;
import com.intellij.execution.remote.RemoteConfigurationType;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.runners.ProgramRunner;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.wm.ToolWindowId;
import kotlinx.serialization.json.Json;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.idea.maven.execution.MavenRunner;
import org.jetbrains.idea.maven.execution.MavenRunnerParameters;
import org.jetbrains.idea.maven.execution.MavenRunnerSettings;
import org.jetbrains.idea.maven.project.MavenProject;
import org.jetbrains.io.JsonUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Path;
import java.util.Collections;

public class ModuleRunProfileState implements RunProfileState {
    private final ModuleRunConfiguration config;
    private final ExecutionEnvironment environment;
    private final Project project;
    private final MavenProject mavenProject;

    public ModuleRunProfileState(ModuleRunConfiguration config, ExecutionEnvironment environment) {
        this.config = config;
        this.environment = environment;
        this.project = environment.getProject();
        this.mavenProject = config.getMavenProject();
    }

    @Override
    public ExecutionResult execute(Executor executor, ProgramRunner<?> runner) throws ExecutionException {
        Project project = environment.getProject();
        String jarPath = config.getJarPath();
        String baserUrl = config.getPnUrl();
        String artifactId = config.getArtifactId();
        String module = config.getModule();


        ConsoleView consoleView = createConsoleView();
        KillableProcessHandler processHandler = new MyKillableProcessHandler(new GeneralCommandLine("docker", "logs", "-f", "parser-node"),
                project, baserUrl, artifactId, module);

        RunContentExecutor runContentExecutor = new RunContentExecutor(project, processHandler);
        if (ToolWindowId.DEBUG.equals(environment.getExecutor().getId())) {
            compileJar(() -> runModule(baserUrl, environment, jarPath, artifactId, module, consoleView));
        } else {
            compileJar(() -> runModule(baserUrl, null, jarPath, artifactId, module, consoleView));
        }

        try {
            runContentExecutor.withTitle(String.format("Running %s", module)).withActivateToolWindow(true)
                    .withFocusToolWindow(true)
                    .run();
        } catch (Exception e) {
            e.printStackTrace();
        }
        consoleView.attachToProcess(processHandler);
        return new DefaultExecutionResult(consoleView, processHandler);
    }

    private void runCommand(ConsoleView consoleView, String... command) throws ExecutionException {
        GeneralCommandLine commandLine = new GeneralCommandLine(command);
        commandLine.setWorkDirectory(project.getBasePath());
        OSProcessHandler processHandler = new OSProcessHandler(commandLine);

        // 添加日志监听器
        processHandler.addProcessListener(new ProcessAdapter() {
            @Override
            public void onTextAvailable(@NotNull ProcessEvent event, @NotNull Key outputType) {
                consoleView.print(event.getText(), ConsoleViewContentType.getConsoleViewType(outputType));
            }
        });

        // 启动进程
        processHandler.startNotify();
        processHandler.waitFor();
    }


    private void runModule(String baserUrl, ExecutionEnvironment environment, String jarPath, String artifactId, String module, ConsoleView consoleView) {
        consoleView.print("Install Jar\n", ConsoleViewContentType.NORMAL_OUTPUT);
        StartModuleBackgroundTask task = new StartModuleBackgroundTask(project, environment, baserUrl, jarPath, artifactId, module);
        ProgressManager.getInstance().run(task);
    }

    private void compileJar(Runnable runnable) {
        MavenRunner runner = MavenRunner.getInstance(project);
        MavenRunnerSettings settings = runner.getState().clone();
        settings.getMavenProperties().put("interactiveMode", "false");
        MavenRunnerParameters params = new MavenRunnerParameters();
        params.setWorkingDirPath(mavenProject.getDirectory());
        params.setGoals(Collections.singletonList("package"));
        params.setCmdOptions("-Dmaven.test.skip=true");
        runner.run(params, settings, runnable);

    }

    private ConsoleView createConsoleView() {
        return new ConsoleViewImpl(environment.getProject(), true);
    }

    public static class APIResult {
        private int code;
        private String msg;

        public int getCode() {
            return code;
        }

        public void setCode(int code) {
            this.code = code;
        }

        public String getMsg() {
            return msg;
        }

        public void setMsg(String msg) {
            this.msg = msg;
        }
    }
}
