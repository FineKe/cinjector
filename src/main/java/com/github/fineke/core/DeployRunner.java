package com.github.fineke.core;

import com.intellij.execution.*;
import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.ConfigurationType;
import com.intellij.execution.configurations.RunProfile;
import com.intellij.execution.remote.RemoteConfiguration;
import com.intellij.execution.remote.RemoteConfigurationType;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.runners.ProgramRunner;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Path;

public class DeployRunner implements ProgramRunner {
    @Override
    public @NotNull @NonNls String getRunnerId() {
        return "installjarRunner";
    }

    @Override
    public boolean canRun(@NotNull String executorId, @NotNull RunProfile profile) {
        return true;
    }

    @Override
    public void execute(@NotNull ExecutionEnvironment environment) throws ExecutionException {
        var cf = environment.getRunnerAndConfigurationSettings();
        String path = ((DemoRunConfiguration) cf.getConfiguration()).getScriptName();
        System.out.println(path);
        try {
//            installJar(Path.of(path));
            startDebug(environment.getProject(),environment);
        } catch (Exception e) {
            throw new ExecutionException(e);
        }
    }


    private void installJar(Path jarPath) throws IOException, InterruptedException {
        // 将 JAR 文件通过 HTTP 上传到本地 Java 服务
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/bride/install"))
                .POST(HttpRequest.BodyPublishers.ofFile(jarPath))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        System.out.println("Upload response: " + response.body());
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

        ProgramRunnerUtil.executeConfiguration(runnerAndConfigurationSettings, ExecutorRegistry.getInstance().getRegisteredExecutors()[0]);
    }
}
