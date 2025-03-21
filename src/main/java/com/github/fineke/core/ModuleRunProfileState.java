package com.github.fineke.core;

import com.intellij.execution.*;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.configurations.RunProfileState;
import com.intellij.execution.impl.ConsoleViewImpl;
import com.intellij.execution.process.KillableProcessHandler;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.runners.ProgramRunner;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.execution.ui.RunContentDescriptor;
import com.intellij.execution.ui.RunContentManager;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindowId;
import org.jetbrains.idea.maven.execution.MavenRunner;
import org.jetbrains.idea.maven.execution.MavenRunnerParameters;
import org.jetbrains.idea.maven.execution.MavenRunnerSettings;
import org.jetbrains.idea.maven.project.MavenProject;
import org.jetbrains.idea.maven.project.MavenProjectsManager;

import java.io.File;
import java.util.Collections;

import static com.github.fineke.core.MyKeys.LAST_MODIFY_TIME_KEY;

public class ModuleRunProfileState implements RunProfileState {
    private final ModuleRunConfiguration config;
    private final ExecutionEnvironment environment;
    private final Project project;
    private final MavenProject mavenProject;

    public ModuleRunProfileState(ModuleRunConfiguration config, ExecutionEnvironment environment) {
        this.config = config;
        this.environment = environment;
        this.project = environment.getProject();
        this.mavenProject = MavenProjectsManager
                .getInstance(project)
                .getProjects()
                .stream()
                .filter(mavenProject1 -> mavenProject1.getMavenId().getArtifactId().equals(config.getArtifactId()))
                .findFirst().get();
    }

    @Override
    public ExecutionResult execute(Executor executor, ProgramRunner<?> runner) throws ExecutionException {
        Project project = environment.getProject();
        String baserUrl = config.getPnUrl();
        String artifactId = config.getArtifactId();
        String module = config.getModule();


        ConsoleView consoleView = createConsoleView();
        KillableProcessHandler processHandler = new MyKillableProcessHandler(new GeneralCommandLine("docker", "logs", "-f", "parser-node"),
                project, baserUrl, artifactId, module);

        RunContentExecutor runContentExecutor = new RunContentExecutor(project, processHandler);
        if (ToolWindowId.DEBUG.equals(environment.getExecutor().getId())) {
            compileJar(() -> runModule(config, environment, consoleView));
        } else {
            compileJar(() -> runModule(config, null, consoleView));
        }

        try {
            runContentExecutor.withTitle(String.format("Running %s", config.getName()))
                    .withActivateToolWindow(true)
                    .withFocusToolWindow(true)
                    .run();
        } catch (Exception e) {
            e.printStackTrace();
        }
        consoleView.attachToProcess(processHandler);
        return new DefaultExecutionResult(consoleView, processHandler);
    }


    private void runModule(ModuleRunConfiguration config, ExecutionEnvironment environment, ConsoleView consoleView) {
        consoleView.print("Install Jar\n", ConsoleViewContentType.NORMAL_OUTPUT);
        StartModuleBackgroundTask task = new StartModuleBackgroundTask(project, environment, config);
        ProgressManager.getInstance().run(task);
    }

    private void compileJar(Runnable runnable) {
        if (needCompile()) {
            MavenRunner runner = MavenRunner.getInstance(project);
            MavenRunnerSettings settings = runner.getState().clone();
            settings.getMavenProperties().put("interactiveMode", "false");
            MavenRunnerParameters params = new MavenRunnerParameters();
            params.setWorkingDirPath(mavenProject.getDirectory());
            params.setGoals(Collections.singletonList("package"));
            params.setCmdOptions("-Dmaven.test.skip=true");
            runner.run(params, settings, runnable);
        } else {
            ApplicationManager.getApplication().invokeLater(runnable, ModalityState.NON_MODAL);
        }
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

    private boolean needCompile() {

        String lastDigest = (String) this.mavenProject.getCachedValue(LAST_MODIFY_TIME_KEY);
        // check if the file is changed
        String latestDigest = FolderMD5Calculator.calculateFolderMD5(new File(this.mavenProject.getDirectory()));
        if (lastDigest == null || !latestDigest.equals(lastDigest)) {
            mavenProject.resetCache();
            mavenProject.putCachedValue(LAST_MODIFY_TIME_KEY, latestDigest);
            return true;
        }
        return false;
    }
}
