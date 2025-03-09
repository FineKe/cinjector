package com.github.fineke.core;

import com.github.weisj.jsvg.S;
import com.intellij.execution.Executor;
import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.execution.configurations.RunConfigurationBase;
import com.intellij.execution.configurations.RunProfileState;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.idea.maven.project.MavenProject;

public class ModuleRunConfiguration extends RunConfigurationBase<ModuleRunConfigurationOptions> {
  private MavenProject mavenProject;
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

    public void setId(String id) {
        getOptions().setId(id);
    }

    public String getId() {
        return getOptions().getId();
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
  }

  public MavenProject getMavenProject() {
    return mavenProject;
  }

  public void setMavenProject(MavenProject mavenProject) {
    this.mavenProject = mavenProject;
  }
}