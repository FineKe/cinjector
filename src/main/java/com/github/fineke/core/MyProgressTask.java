package com.github.fineke.core;

import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public class MyProgressTask {

    public static void runProgressTask(Project project, String title, Runnable runnable) {
        ProgressManager.getInstance().run(new Task.Backgroundable(project, title, true) {
            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                indicator.setText("Processing...");
                indicator.setIndeterminate(true);
                try {
                    runnable.run();
                }catch (Exception e) {
                    indicator.setText(String.format("%s Error",title));

                    MyNotification.showNotification(project, String.format("%s Error",title), e.getMessage());
                    throw e;
                }
                indicator.setText("Done!");
            }
        });
    }
}