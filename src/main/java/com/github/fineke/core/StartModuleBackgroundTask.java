package com.github.fineke.core;

import com.intellij.execution.ProgramRunnerUtil;
import com.intellij.execution.RunManager;
import com.intellij.execution.RunnerAndConfigurationSettings;
import com.intellij.execution.remote.RemoteConfiguration;
import com.intellij.execution.remote.RemoteConfigurationType;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;

public class StartModuleBackgroundTask extends Task.Backgroundable {
    private static final String TITLE = "Start Module Background Task";
    private String baseURL;
    private String jarPath;
    private String artifactId;
    private String module;
    private ExecutionEnvironment environment;

    public StartModuleBackgroundTask(Project project, ExecutionEnvironment environment, String baseURL, String jarPath, String artifactId, String module) {
        super(project, TITLE, true); // 参数说明：项目、进度条标题、是否可取消
        this.baseURL = baseURL;
        this.jarPath = jarPath;
        this.artifactId = artifactId;
        this.module = module;
        this.environment = environment;
    }

    @Override
    public void run(@NotNull ProgressIndicator indicator) {
        indicator.setIndeterminate(true); // 不确定进度模式（滚动条）
        indicator.setText("Install jar...");
        try {

            if (environment!=null) {
                startDebug(myProject, module,environment);
            }

            // install jar包
            ParserNodeBridge.installJar(baseURL, Path.of(jarPath));
            MyNotification.showNotification(myProject, "Install jar", "Jar installed successfully!");
            indicator.setText("Start module...");
            // 启动 module
            ParserNodeBridge.startModule(baseURL, artifactId, module);
            indicator.setText("Done!");
            MyNotification.showNotification(myProject, "Start module", "Module started successfully!");
        } catch (Exception e) {
            indicator.setText("Error!");
            MyNotification.showNotification(myProject, "Run module Error", e.getMessage());
        }
    }


    private void startDebug(Project project, String module,@NotNull ExecutionEnvironment environment) {
        RunManager runManager = RunManager.getInstance(project);
        String name = String.format("Debug %s", module);
        RunnerAndConfigurationSettings runnerAndConfigurationSettings = runManager.createConfiguration(name, RemoteConfigurationType.getInstance());
        runnerAndConfigurationSettings.setName(name);
        var configuration = (RemoteConfiguration) runnerAndConfigurationSettings.getConfiguration();
        // 设置 Remote Debug 参数
        configuration.HOST = "localhost";
        configuration.PORT = String.valueOf(4556);
        configuration.USE_SOCKET_TRANSPORT = true;
        configuration.SERVER_MODE = false;
        configuration.AUTO_RESTART = true;
        ProgramRunnerUtil.executeConfiguration(runnerAndConfigurationSettings, environment.getExecutor());
    }

    @Override
    public void onSuccess() {
        super.onSuccess();
    }
}