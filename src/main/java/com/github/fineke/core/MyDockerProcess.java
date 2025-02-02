package com.github.fineke.core;

import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.process.ProcessOutputTypes;
import com.intellij.openapi.application.ApplicationManager;

import java.io.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

public class MyDockerProcess extends ProcessHandler {
    private final String jarPath;
    private final String baserUrl;
    private final String artifactId;
    private final String module;


    public MyDockerProcess(String jarPath, String baserUrl, String artifactId, String module) {
        this.jarPath = jarPath;
        this.baserUrl = baserUrl;
        this.artifactId = artifactId;
        this.module = module;
    }

    @Override
    protected void destroyProcessImpl() {
        // 终止进程
    }

    @Override
    protected void detachProcessImpl() {
        notifyProcessDetached();
    }

    @Override
    public boolean detachIsDefault() {
        return false;
    }

    @Override
    public OutputStream getProcessInput() {
        return null;
    }

    @Override
    public void startNotify() {
        super.startNotify();
        ApplicationManager.getApplication().executeOnPooledThread(() -> {
            try {
                // 1. 编译项目（Maven/Gradle）
                runCommand("mvn clean package");

                installJar(baserUrl, Path.of(jarPath));
                startModule(baserUrl, artifactId, module);

                // 3. 显示 Docker 容器日志
                runCommand("docker logs -f parser-node");
                
                notifyProcessTerminated(0);
            } catch (Exception e) {
                notifyProcessTerminated(1);
            }
        });
    }

    private void runCommand(String command) throws IOException, InterruptedException {
        Process process = Runtime.getRuntime().exec(command);
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line;
        while ((line = reader.readLine()) != null) {
            notifyTextAvailable(line + "\n", ProcessOutputTypes.STDOUT);
        }
        process.waitFor();
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
}
