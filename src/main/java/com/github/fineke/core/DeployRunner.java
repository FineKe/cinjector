package com.github.fineke.core;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.RunProfile;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.runners.ProgramRunner;
import com.intellij.openapi.wm.ToolWindowId;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

public class DeployRunner implements ProgramRunner {
    @Override
    public @NotNull @NonNls String getRunnerId() {
        return "ModuleRunner";
    }

    @Override
    public boolean canRun(@NotNull String executorId, @NotNull RunProfile profile) {
        if (ToolWindowId.RUN.equals(executorId)) {
            return profile instanceof ModuleRunConfiguration;
        }
        if (ToolWindowId.DEBUG.equals(executorId)) {
            return profile instanceof ModuleRunConfiguration;
        }
        return false;
    }

    @Override
    public void execute(@NotNull ExecutionEnvironment environment) throws ExecutionException {
        environment.getState().execute(environment.getExecutor(), this);
    }
}
