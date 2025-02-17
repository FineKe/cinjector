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
        KillableProcessHandler processHandler = new MyKillableProcessHandler(new GeneralCommandLine("docker", "logs" ,"-f", "parser-node"),
                baserUrl, artifactId, module);

        RunContentExecutor runContentExecutor = new RunContentExecutor(project, processHandler);
        if (ToolWindowId.DEBUG.equals(environment.getExecutor().getId())) {
            compileJar(()-> {
                runModule(baserUrl, jarPath, artifactId, module, consoleView);
                startDebug(environment.getProject(), environment);
            });
        }else {
            compileJar(()->runModule(baserUrl, jarPath, artifactId, module, consoleView));
        }

        try {
            runContentExecutor.withTitle("Parser Node Logs").withActivateToolWindow(true)
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


    private void installJar(String baserUrl, Path jarPath) throws IOException, InterruptedException {
        // 将 JAR 文件通过 HTTP 上传到本地 Java 服务
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(String.format("%s/install",baserUrl)))
                .POST(HttpRequest.BodyPublishers.ofFile(jarPath))
                .build();
        MyProgressTask.runProgressTask(project,"install", ()->{
            try {
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                Gson gson = new Gson();
                APIResult apiResult = gson.fromJson(response.body(), APIResult.class);
                if (apiResult.code!=0) {
                    throw new RuntimeException(apiResult.getMsg());
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });


    }

    public void startModule(String baserUrl,String artifactId,String module) throws IOException, InterruptedException {
        // 启动 Java 服务中的指定模块
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(String.format("%s/%s/%s/start",baserUrl,artifactId,module)))
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();
        MyProgressTask.runProgressTask(project,"start", ()->{
            try {
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                Gson gson = new Gson();
                APIResult apiResult = gson.fromJson(response.body(), APIResult.class);
                if (apiResult.code!=0) {
                    throw new RuntimeException(apiResult.getMsg());
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private void runModule(String baserUrl,String jarPath,String artifactId,String module,ConsoleView consoleView) {
        try {
            consoleView.print("Install Jar\n", ConsoleViewContentType.NORMAL_OUTPUT);
            installJar(baserUrl, Path.of(jarPath));
            startModule(baserUrl, artifactId, module);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
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

//    private void runCommand(String command) throws IOException, InterruptedException {
//        Process process = Runtime.getRuntime().exec(command);
//        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
//        String line;
//        while ((line = reader.readLine()) != null) {
//            notifyTextAvailable(line + "\n", ProcessOutputTypes.STDOUT);
//        }
//        process.waitFor();
//    }

    private void startDebug(Project project, @NotNull ExecutionEnvironment environment) {
        RunManager runManager = RunManager.getInstance(project);
        RunnerAndConfigurationSettings runnerAndConfigurationSettings =  runManager.createConfiguration("Remote Debug", RemoteConfigurationType.getInstance());
        runnerAndConfigurationSettings.setName("remote debug");
        var configuration = (RemoteConfiguration)runnerAndConfigurationSettings.getConfiguration();
        // 设置 Remote Debug 参数
        configuration.HOST = "localhost";
        configuration.PORT = String.valueOf(5005);
        configuration.USE_SOCKET_TRANSPORT = true;
        configuration.SERVER_MODE = false;
        configuration.AUTO_RESTART = true;


        ProgramRunnerUtil.executeConfiguration(runnerAndConfigurationSettings, environment.getExecutor());
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
