package com.github.fineke.core;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.process.KillableProcessHandler;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public class MyKillableProcessHandler extends KillableProcessHandler {
    private String baseUrl;
    private String artifactId;
    private String module;
    private Project project;

    public MyKillableProcessHandler(@NotNull GeneralCommandLine commandLine,Project project, String baseUrl, String artifactId, String module) throws ExecutionException {
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
    public void destroyProcess() {
        try {
            StopModuleBackgroundTask task = new StopModuleBackgroundTask(project, baseUrl, artifactId, module);
            ProgressManager.getInstance().run(task);
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.destroyProcess();
    }





}
