package com.github.fineke.core;

import com.intellij.codeInsight.daemon.GutterIconNavigationHandler;
import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.codeInsight.daemon.LineMarkerProvider;
import com.intellij.execution.lineMarker.ExecutorAction;
import com.intellij.execution.lineMarker.RunLineMarkerContributor;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.openapi.externalSystem.model.ProjectSystemId;
import com.intellij.openapi.externalSystem.model.execution.ExternalSystemTaskExecutionSettings;
import com.intellij.openapi.externalSystem.task.ExternalSystemTaskManager;
import com.intellij.openapi.externalSystem.util.ExternalSystemUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.IconLoader;
import com.intellij.psi.*;
import com.intellij.ui.icons.RowIcon;
import com.intellij.util.Function;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.idea.maven.execution.MavenRunner;
import org.jetbrains.idea.maven.execution.MavenRunnerParameters;
import org.jetbrains.idea.maven.execution.MavenRunnerSettings;
import org.jetbrains.idea.maven.project.MavenProjectsManager;

import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Collections;

import static com.github.fineke.core.DeployAction.createDeployAction;
import static com.intellij.util.PlatformIcons.ADD_ICON;

public class ModuleLineMarkerProvider extends RunLineMarkerContributor {


    private boolean isValidMainMethod(PsiMethod method) {
        PsiParameterList params = method.getParameterList();
        return method.hasModifierProperty(PsiModifier.PUBLIC) &&
                method.hasModifierProperty(PsiModifier.STATIC) &&
                "void".equals(method.getReturnType().getCanonicalText()) &&
                params.getParametersCount() == 1 &&
                params.getParameters()[0].getType().equalsToText("java.lang.String[]");
    }

    private void runOrDebug(Project project) {
        compile(project);
        // 在点击按钮时编译项目并上传 JAR 文件
        // compileAndRun(project);
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


    private static final ProjectSystemId MAVEN_SYSTEM_ID = new ProjectSystemId("Maven");

    private void compile(Project project) {


        MavenRunner runner = MavenRunner.getInstance(project);
        MavenRunnerSettings settings = runner.getState().clone();
        settings.getMavenProperties().put("interactiveMode", "false");
        MavenRunnerParameters params = new MavenRunnerParameters();
        params.setWorkingDirPath(project.getProjectFilePath());
        params.setGoals(Collections.singletonList("install"));
        runner.run(params, settings, () -> System.out.println("maven goal execution completed"));
    }

    @Override
    public @Nullable Info getInfo(@NotNull PsiElement element) {

        if (isModule(element)) {
            String module = getAnnotationValue(((PsiClass) element.getParent()).getAnnotation("com.oklink.blockchain.Module"));
            return new RunLineMarkerContributor.Info(AllIcons.Actions.Execute, new Function<PsiElement, String>() {
                @Override
                public String fun(PsiElement psiElement) {
                    return "Compile and Run";
                }
            }, createDeployAction(false, module),
                    createDeployAction(true, module));
        }

        return null;
    }


    private boolean isModule(PsiElement element) {
        if (element instanceof PsiIdentifier && element.getParent() instanceof PsiClass) {
            PsiClass psiClass = (PsiClass) element.getParent();

            // 检查类是否有 @Module 注解
            PsiAnnotation moduleAnnotation = psiClass.getAnnotation("com.oklink.blockchain.Module");
            if (moduleAnnotation != null) {
                return true;

            }
        }
        return false;
    }

    private String getAnnotationValue(PsiAnnotation annotation) {
        PsiAnnotationMemberValue valueAttribute = annotation.findAttributeValue("value");
        if (valueAttribute instanceof PsiLiteralExpression) {
            Object value = ((PsiLiteralExpression) valueAttribute).getValue();
            return value != null ? value.toString() : "Unknown";
        }
        return "Unknown";
    }
}
