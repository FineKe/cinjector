package com.github.fineke.core;

import com.intellij.execution.DefaultRunExecutor;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.ExecutionManager;
import com.intellij.execution.Executor;
import com.intellij.execution.configurations.RemoteRunConfiguration;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.runners.ExecutionEnvironmentBuilder;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;

public class RemoteDebugStarter {

    public static void startRemoteDebug(Project project) {
        RemoteRunConfiguration runConfig = new RemoteRunConfiguration("Remote Debug", project);
        runConfig.setHost("localhost");
        runConfig.setPort(5005);  // 调试端口

        ExecutionEnvironment environment = ExecutionEnvironmentBuilder.create(runConfig.getType(), runConfig).build();

        // 在新的线程中启动远程调试
        ApplicationManager.getApplication().invokeLater(() -> {
            try {
                Executor executor = DefaultRunExecutor.getRunExecutorInstance();
                ExecutionManager.getInstance(project).startRunProfile(environment, executor);
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        });
    }
}
