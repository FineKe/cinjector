package com.github.fineke.core;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.ProgramRunnerUtil;
import com.intellij.execution.RunManager;
import com.intellij.execution.RunnerAndConfigurationSettings;
import com.intellij.execution.configurations.RunProfile;
import com.intellij.execution.remote.RemoteConfiguration;
import com.intellij.execution.remote.RemoteConfigurationType;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.runners.ProgramRunner;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindowId;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

public class DeployRunner implements ProgramRunner {
    @Override
    public @NotNull @NonNls String getRunnerId() {
        return "runModuleRunner";
    }

    @Override
    public boolean canRun(@NotNull String executorId, @NotNull RunProfile profile) {
        if (ToolWindowId.RUN.equals(executorId)) {
            return profile instanceof DemoRunConfiguration;
        }
        if (ToolWindowId.DEBUG.equals(executorId)) {
            return profile instanceof DemoRunConfiguration;
        }
        return false;
    }

    @Override
    public void execute(@NotNull ExecutionEnvironment environment) throws ExecutionException {

        if (ToolWindowId.DEBUG.equals(environment.getExecutor().getId())) {
            startDebug(environment.getProject(), environment);
        }else {
            environment.getState().execute(environment.getExecutor(), this);
        }
    }


    private void compileJar(Project project) {
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

    public void startModule(String baserUrl,String artifactId,String module) {
        // 启动 Java 服务中的指定模块
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(String.format("/%s/%s/start",baserUrl,artifactId,module)))
                .build();
        CompletableFuture<HttpResponse<String>> response = client.sendAsync(request, HttpResponse.BodyHandlers.ofString());
        response.thenAccept(r -> System.out.println("Start response: " + r.body()));
    }


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

}
