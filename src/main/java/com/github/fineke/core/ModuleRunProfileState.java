package com.github.fineke.core;

import com.intellij.execution.*;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.configurations.RunProfileState;
import com.intellij.execution.filters.TextConsoleBuilderFactory;
import com.intellij.execution.process.KillableProcessHandler;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.runners.ProgramRunner;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.openapi.project.Project;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Path;

public class ModuleRunProfileState implements RunProfileState {
    private final ModuleRunConfiguration config;
    private final ExecutionEnvironment environment;
    private final Project project;

    public ModuleRunProfileState(ModuleRunConfiguration config, ExecutionEnvironment environment) {
        this.config = config;
        this.environment = environment;
        this.project = environment.getProject();
    }

    @Override
    public ExecutionResult execute(Executor executor, ProgramRunner<?> runner) throws ExecutionException {
        Project project = environment.getProject();
        String jarPath = config.getJarPath();
        String baserUrl = config.getPnUrl();
        String artifactId = config.getArtifactId();
        String module = config.getModule();


        try {
            installJar(baserUrl, Path.of(jarPath));
            startModule(baserUrl, artifactId, module);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }


        ConsoleView consoleView = TextConsoleBuilderFactory.getInstance().createBuilder(project).getConsole();

        // 2. 执行 Docker 日志查看
        KillableProcessHandler logProcess = null;
        try {
            logProcess = new KillableProcessHandler(new GeneralCommandLine("docker", "-H" ,"127.0.0.1:2375",  "logs" ,"-f", "parser-node"));
            RunContentExecutor runContentExecutor = new RunContentExecutor(project, logProcess);
            runContentExecutor.withTitle("Parser Node Logs").withActivateToolWindow(true)
                    .withStop(()->{
                        try {
                            stopModule(baserUrl, artifactId, module);
                            uninstallJar(baserUrl, artifactId);
                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }, ()->true)
                    .run();
        } catch (Exception e) {
           e.printStackTrace();
        }
        consoleView.attachToProcess(logProcess);

        return new DefaultExecutionResult(consoleView, logProcess);
    }


    private void installJar(String baserUrl, Path jarPath) throws IOException, InterruptedException {
        // 将 JAR 文件通过 HTTP 上传到本地 Java 服务
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(String.format("%s/install",baserUrl)))
                .POST(HttpRequest.BodyPublishers.ofFile(jarPath))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        System.out.println("Upload response: " + response.body());
    }

    public void startModule(String baserUrl,String artifactId,String module) throws IOException, InterruptedException {
        // 启动 Java 服务中的指定模块
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(String.format("%s/%s/%s/start",baserUrl,artifactId,module)))
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        System.out.println("start response: " + response.body());
    }

    public void stopModule(String baserUrl,String artifactId,String module) throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(String.format("%s/%s/%s/stop",baserUrl,artifactId,module)))
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        System.out.println("stop response: " + response.body());
    }

    public void uninstallJar(String baserUrl,String artifactId) throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(String.format("%s/%s/uninstall",baserUrl,artifactId)))
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        System.out.println("stop response: " + response.body());
    }

    public void doRunModule() {

    }
}
