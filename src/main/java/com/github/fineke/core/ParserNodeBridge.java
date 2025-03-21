package com.github.fineke.core;

import com.google.gson.Gson;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.nio.file.Path;

public class ParserNodeBridge {

    private static Gson gson = new Gson();
    private static HttpClient client = HttpClient.newBuilder()
            .connectTimeout(java.time.Duration.ofSeconds(10))
            .build();


    public static void installJar(String baserUrl, Path jarPath) throws IOException, InterruptedException {
        // 将 JAR 文件通过 HTTP 上传到本地 parser-node 服务
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(String.format("%s/install", baserUrl)))
                .POST(HttpRequest.BodyPublishers.ofFile(jarPath))
                .build();
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            APIResult apiResult = gson.fromJson(response.body(), APIResult.class);
            if (apiResult.code != 0) {
                throw new RuntimeException(String.format("install jar %s failed: %s", jarPath, apiResult.getMsg()));
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void startModule(String baserUrl, String artifactId, String module) throws IOException, InterruptedException {
        // 启动 parser-node 服务中的指定module模块
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(String.format("%s/%s/%s/start", baserUrl, artifactId, module)))
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            APIResult apiResult = gson.fromJson(response.body(), APIResult.class);
            if (apiResult.code != 0) {
                throw new RuntimeException(String.format("start module %s failed: %s", module, apiResult.getMsg()));
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void stopModule(String baserUrl, String artifactId, String module) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(String.format("%s/%s/%s/stop", baserUrl, artifactId, module)))
                .timeout(java.time.Duration.ofSeconds(60))
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            APIResult apiResult = gson.fromJson(response.body(), APIResult.class);
            if (apiResult.code != 0) {
                throw new RuntimeException(String.format("stop module %s failed: %s", module, apiResult.getMsg()));
            }
        } catch (HttpTimeoutException e) {
            throw e;
        }
    }

    public static void uninstallJar(String baserUrl, String artifactId) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(String.format("%s/%s/uninstall", baserUrl, artifactId)))
                .timeout(java.time.Duration.ofSeconds(60))
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            APIResult apiResult = gson.fromJson(response.body(), APIResult.class);
            if (apiResult.code != 0) {
                throw new RuntimeException(String.format("uninstall jar %s failed: %s", artifactId, apiResult.getMsg()));
            }
        } catch (HttpTimeoutException e) {
            throw e;
        }
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
