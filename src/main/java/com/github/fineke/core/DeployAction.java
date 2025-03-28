package com.github.fineke.core;

import com.intellij.execution.*;
import com.intellij.execution.executors.DefaultRunExecutor;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.NlsActions;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ToolWindowId;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.idea.maven.execution.MavenRunner;
import org.jetbrains.idea.maven.execution.MavenRunnerParameters;
import org.jetbrains.idea.maven.execution.MavenRunnerSettings;
import org.jetbrains.idea.maven.project.MavenProject;
import org.jetbrains.idea.maven.project.MavenProjectsManager;
import org.jetbrains.idea.maven.tasks.MavenBeforeRunTask;

import javax.swing.*;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

public class DeployAction extends AnAction {

    public static final String DEFAULT_BRIDGE = "http://localhost:4555/bridge";
    private String module;
    private boolean debug;

    public DeployAction() {
    }


    public DeployAction(@Nullable @NlsActions.ActionText String text, @Nullable @NlsActions.ActionDescription String description, @Nullable Icon icon, String module, boolean debug) {
        super(text, description, icon);
        this.module = module;
        this.debug = debug;
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        MavenProject currentProject = getCurrentMavenProject(e);
        Path path = Path.of(currentProject.getBuildDirectory(), String.format("%s.%s", currentProject.getFinalName() + "-ark-biz", currentProject.getPackaging()));
        String artifactId = currentProject.getMavenId().getArtifactId();


        RunManager runManager = RunManager.getInstance(e.getProject());
        String name = String.format("[%s]%s", artifactId,module);
        String configId = String.format("%s-%s", artifactId, module);
        RunnerAndConfigurationSettings cf = null;

        for (RunnerAndConfigurationSettings runnerAndConfigurationSettings : runManager.getConfigurationSettingsList(ModuleRunConfigurationType.class)) {
            ModuleRunConfiguration configuration = (ModuleRunConfiguration) runnerAndConfigurationSettings.getConfiguration();
            if (configuration.getId().equals(configId)) {
                cf = runnerAndConfigurationSettings;
                break;
            }
        }

        if (cf == null) {
            cf = runManager.createConfiguration(name, new RunModuleConfigurationFactory(new ModuleRunConfigurationType()));
            cf.setName(name);
            ModuleRunConfiguration configuration = (ModuleRunConfiguration) cf.getConfiguration();
            configuration.setPnUrl(DEFAULT_BRIDGE);
            configuration.setId(configId);
            runManager.addConfiguration(cf);
        }

        ModuleRunConfiguration configuration = (ModuleRunConfiguration) cf.getConfiguration();
        configuration.setModule(this.module);
        configuration.setArtifactId(artifactId);
        configuration.setJarPath(path.toString());
        runManager.setSelectedConfiguration(cf);

        Executor executor = DefaultRunExecutor.getRunExecutorInstance();

        if (debug) {
            executor = ExecutorRegistry.getInstance().getExecutorById(ToolWindowId.DEBUG);
        }
        ProgramRunnerUtil.executeConfiguration(cf, executor);

    }

    private MavenProject getCurrentMavenProject(AnActionEvent e) {
        VirtualFile currentFile = e.getData(CommonDataKeys.VIRTUAL_FILE);
        MavenProjectsManager manager = MavenProjectsManager.getInstance(e.getProject());
        var currentProject = manager
                .getProjects()
                .stream()
                .filter(mp -> currentFile.getPath().startsWith(mp.getDirectory()))
                .sorted((a, b) -> b.getDirectory().length() - a.getDirectory().length())
                .findFirst().get();

        return currentProject;
    }

    public static DeployAction createDeployAction(boolean debug, String module) {
        if (debug) {
            return new DeployAction(String.format("Debug %s", module), String.format("Build %s and Run it with Debug", module), AllIcons.Actions.StartDebugger, module, true);
        }
        return new DeployAction(String.format("Run %s", module), String.format("Build %s and Run it ", module), AllIcons.Actions.Execute, module, debug);
    }
}
