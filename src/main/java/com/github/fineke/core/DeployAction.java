package com.github.fineke.core;

import com.intellij.execution.ProgramRunnerUtil;
import com.intellij.execution.RunManager;
import com.intellij.execution.RunnerAndConfigurationSettings;
import com.intellij.execution.executors.DefaultRunExecutor;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.NlsActions;
import com.intellij.openapi.vfs.VirtualFile;
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

    private String module;

    public DeployAction() {
    }

    public DeployAction(@Nullable @NlsActions.ActionText String text, @Nullable @NlsActions.ActionDescription String description, @Nullable Icon icon, String module) {
        super(text, description, icon);
        this.module = module;
    }

    public DeployAction(String module) {
        super("deploy","jhhhhhhh", AllIcons.Actions.Execute);
        this.module= module;
    }

    public DeployAction(@Nullable @NlsActions.ActionText String text,String module) {
        super(text,"jhhhhhhh", AllIcons.Actions.Execute);
        this.module = module;
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        MavenProject currentProject = getCurrentMavenProject(e);
        Path path = Path.of(currentProject.getBuildDirectory(), String.format("%s.%s", currentProject.getFinalName()+"-ark-biz", currentProject.getPackaging()));
        String artifactId = currentProject.getMavenId().getArtifactId();


                RunManager runManager = RunManager.getInstance(e.getProject());
                RunnerAndConfigurationSettings cf = null;
                if (runManager.getConfigurationSettingsList(ModuleRunConfigurationType.class).isEmpty()){
                    cf = runManager.createConfiguration("deploy", new RunModuleConfigurationFactory(new ModuleRunConfigurationType()));
                    cf.setName("runModuleRunner");
                    ModuleRunConfiguration configuration = (ModuleRunConfiguration) cf.getConfiguration();
                    configuration.setJarPath(path.toString());
                    configuration.setModule(this.module);
                    configuration.setArtifactId(artifactId);
                    configuration.setPnUrl("http://localhost:8080/bridge");
                    configuration.setMavenProject(currentProject);
                    runManager.addConfiguration(cf);
                    runManager.setSelectedConfiguration(cf);
                }else {
                    cf = runManager.getConfigurationSettingsList(ModuleRunConfigurationType.class).get(0);
                    ModuleRunConfiguration configuration = (ModuleRunConfiguration) cf.getConfiguration();
                    configuration.setMavenProject(currentProject);
                }

                ProgramRunnerUtil.executeConfiguration(cf, DefaultRunExecutor.getRunExecutorInstance());

    }

    private MavenProject getCurrentMavenProject(AnActionEvent e) {
        VirtualFile currentFile = e.getData(CommonDataKeys.VIRTUAL_FILE);
        MavenProjectsManager manager = MavenProjectsManager.getInstance(e.getProject());
        var currentProject = manager
                .getProjects()
                .stream()
                .filter(mp-> currentFile.getPath().startsWith(mp.getDirectory()))
                .sorted((a,b)->b.getDirectory().length()-a.getDirectory().length())
                .findFirst().get();

        return currentProject;
    }
}
