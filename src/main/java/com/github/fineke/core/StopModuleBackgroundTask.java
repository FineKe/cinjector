package com.github.fineke.core;

import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public class StopModuleBackgroundTask extends Task.Backgroundable {
    private static final String TITLE = "Start Module Background Task";
    private String baseURL;
    private String artifactId;
    private String module;

    public StopModuleBackgroundTask(Project project, String baseURL, String artifactId, String module) {
        super(project, TITLE, true); // 参数说明：项目、进度条标题、是否可取消
        this.baseURL = baseURL;
        this.artifactId = artifactId;
        this.module = module;
    }

    @Override
    public void run(@NotNull ProgressIndicator indicator) {
        indicator.setIndeterminate(true); // 不确定进度模式（滚动条）
        indicator.setText("Stop module...");
        try {
            // stop module
            ParserNodeBridge.stopModule(baseURL, artifactId, module);
            MyNotification.showNotification(myProject, "Stop module", "Module stopped successfully!");
            ParserNodeBridge.uninstallJar(baseURL, artifactId);
            indicator.setText("Done!");
            MyNotification.showNotification(myProject, "Uninstall jar", "Jar uninstalled successfully!");
        } catch (Exception e) {
            indicator.setText("Error!");
            MyNotification.showNotification(myProject, "Stop Running Module Error", e.getMessage());
        }
    }
}