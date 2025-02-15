package com.github.fineke.core;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.process.KillableProcessHandler;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class MyKillableProcessHandler extends KillableProcessHandler {
    private String baseUrl;
    private String artifactId;
    private String module;

    public MyKillableProcessHandler(@NotNull GeneralCommandLine commandLine, String baseUrl, String artifactId, String module) throws ExecutionException {
        super(commandLine);
        this.baseUrl = baseUrl;
        this.artifactId = artifactId;
        this.module = module;
    }

    public MyKillableProcessHandler(@NotNull GeneralCommandLine commandLine) throws ExecutionException {
        super(commandLine);
    }

    @Override
    public void destroyProcess() {
        try {
            stopModule(baseUrl, artifactId, module);
            uninstallJar(baseUrl, artifactId);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        super.destroyProcess();
    }




    public void stopModule(String baserUrl, String artifactId, String module) throws IOException, InterruptedException {
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
}
