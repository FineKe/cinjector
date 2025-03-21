package com.github.fineke.core;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.process.KillableProcessHandler;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.Future;

public class MyKillableProcessHandler extends KillableProcessHandler {
    private String baseUrl;
    private String artifactId;
    private String module;
    private Project project;

    public MyKillableProcessHandler(@NotNull GeneralCommandLine commandLine, Project project, String baseUrl, String artifactId, String module) throws ExecutionException {
        super(commandLine);
        this.project = project;
        this.baseUrl = baseUrl;
        this.artifactId = artifactId;
        this.module = module;
    }

    public MyKillableProcessHandler(@NotNull GeneralCommandLine commandLine) throws ExecutionException {
        super(commandLine);
    }

    @Override
    public @NotNull Future<?> executeTask(@NotNull Runnable task) {
        return super.executeTask(task);
    }


    @Override
    public void destroyProcess() {
        try {
            StopModuleBackgroundTask task = new StopModuleBackgroundTask(project, baseUrl, artifactId, module, () -> {
                super.destroyProcess();
            });
            ProgressManager.getInstance().run(task);
        } catch (Exception e) {
            e.printStackTrace();
        }
//        super.destroyProcess();
    }

    @Override
    public boolean canKillProcess() {

        return super.canKillProcess();
    }
}
