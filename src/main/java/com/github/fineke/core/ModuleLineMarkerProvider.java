package com.github.fineke.core;

public class ModuleLineMarkerProvider implements LineMarkerProvider{

    @Override
    public @Nullable LineMarkerInfo<?> getLineMarkerInfo(@NotNull PsiElement element) {
        if (element instanceof PsiMethod && isValidMainMethod((PsiMethod) element)) {
            return new LineMarkerInfo<>(
                    element,  // 关联的 PSI 元素
                    element.getTextRange(),
                    IconLoader.getIcon("/icons/run.svg"),  // 自定义的图标
                    LineMarkerInfo.Alignment.CENTER,
                    null,  // 点击事件
                    new GutterIconNavigationHandler<PsiElement>() {
                        @Override
                        public void navigate(MouseEvent e, PsiElement elt) {
                            // 点击按钮后触发的操作
                            runOrDebug(elt.getProject());
                        }
                    },
                    GutterIconRenderer.Alignment.CENTER
            );
        }
        return null;
    }

    private boolean isValidMainMethod(PsiMethod method) {
        PsiParameterList params = method.getParameterList();
        return method.hasModifierProperty(PsiModifier.PUBLIC) &&
                method.hasModifierProperty(PsiModifier.STATIC) &&
                "void".equals(method.getReturnType().getCanonicalText()) &&
                params.getParametersCount() == 1 &&
                params.getParameters()[0].getType().equalsToText("java.lang.String[]");
    }

    private void runOrDebug(Project project) {
        // 在点击按钮时编译项目并上传 JAR 文件
        compileAndRun(project);
    }

    private void compileAndRun(Project project) {
        // 这里执行编译任务和上传 JAR 文件的操作
        try {
            // 假设你已经有了构建 JAR 和上传 JAR 的逻辑
            File jarFile = compileToJar(project);
            uploadJar(jarFile);
            startJarWithDebug();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private File compileToJar(Project project) throws Exception {
        // 使用 Maven 或 Gradle 编译项目为 JAR 文件
        // 这里是一个示例，实际可以调用对应的构建工具
        ProcessBuilder builder = new ProcessBuilder("mvn", "clean", "package");
        builder.directory(new File(project.getBasePath()));
        builder.start().waitFor();
        return new File(project.getBasePath(), "target/your-project.jar");
    }

    private void uploadJar(File jarFile) throws Exception {
        // 将 JAR 文件通过 HTTP 上传到本地 Java 服务
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/upload"))
                .header("Content-Type", "application/java-archive")
                .POST(HttpRequest.BodyPublishers.ofFile(jarFile.toPath()))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        System.out.println("Upload response: " + response.body());
    }

    private void startJarWithDebug() {
        // 启动 JAR 并开启远程调试
        try {
            String command = "java -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005 -jar target/your-project.jar";
            ProcessBuilder builder = new ProcessBuilder(command.split(" "));
            builder.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
