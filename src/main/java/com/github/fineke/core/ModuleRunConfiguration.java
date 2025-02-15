package com.github.fineke.core;

import com.intellij.execution.BeforeRunTask;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.Executor;
import com.intellij.execution.configurations.*;
import com.intellij.execution.process.OSProcessHandler;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.process.ProcessHandlerFactory;
import com.intellij.execution.process.ProcessTerminatedListener;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.idea.maven.tasks.MavenBeforeRunTask;

import java.util.List;

public class ModuleRunConfiguration extends RunConfigurationBase<ModuleRunConfigurationOptions> {

  protected ModuleRunConfiguration(Project project,
                                   ConfigurationFactory factory,
                                   String name) {
    super(project, factory, name);
  }

  @Override
  protected @NotNull ModuleRunConfigurationOptions getOptions() {
    return (ModuleRunConfigurationOptions)super.getOptions();
  }

  public String getJarPath() {
    return getOptions().getJarPath();
  }

  public void setJarPath(String jarPath) {
    getOptions().setJarPath(jarPath);
  }

  public String getArtifactId() {
    return getOptions().getArtifactId();
  }

    public void setArtifactId(String artifactId) {
        getOptions().setArtifactId(artifactId);
    }

    public String getModule() {
        return getOptions().getModule();
    }

    public void setModule(String module) {
        getOptions().setModule(module);
    }

    public String getPnUrl() {
        return getOptions().getPnUrl();
    }

    public void setPnUrl(String pnUrl) {
        getOptions().setPnUrl(pnUrl);
    }

  @NotNull
  @Override
  public SettingsEditor<? extends RunConfiguration> getConfigurationEditor() {
    return new ModuleSettingsEditor();
  }

  @Nullable
  @Override
  public RunProfileState getState(@NotNull Executor executor,
                                  @NotNull ExecutionEnvironment environment) {
    return new ModuleRunProfileState(this,environment);

//    return new CommandLineState(environment) {
//      @NotNull
//      @Override
//      protected ProcessHandler startProcess() throws ExecutionException {
//        GeneralCommandLine commandLine =
//                new GeneralCommandLine("docker", "-H" ,"127.0.0.1:2375",  "logs" ,"-f", "parser-node");
//        OSProcessHandler processHandler = ProcessHandlerFactory.getInstance()
//                .createColoredProcessHandler(commandLine);
//        ProcessTerminatedListener.attach(processHandler);
//        return processHandler;
//      }
//    };
  }
}