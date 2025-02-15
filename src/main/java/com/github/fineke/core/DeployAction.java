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
import org.jetbrains.idea.maven.project.MavenProjectsManager;

import javax.swing.*;
import java.nio.file.Path;
import java.util.Collections;

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
        Project project = e.getProject();
        VirtualFile currentFile = e.getData(CommonDataKeys.VIRTUAL_FILE);
        MavenProjectsManager manager = MavenProjectsManager.getInstance(project);
        var currentProject = manager
                .getProjects()
                .stream()
                .filter(mp-> currentFile.getPath().startsWith(mp.getDirectory()))
                .sorted((a,b)->b.getDirectory().length()-a.getDirectory().length())
                .findFirst().get();

        if (currentProject==null) {

            return;
        }



        Path path = Path.of(currentProject.getBuildDirectory(), String.format("%s.%s", currentProject.getFinalName()+"-ark-biz", currentProject.getPackaging()));
        String artifactId = currentProject.getName();


        MavenRunner runner = MavenRunner.getInstance(project);
        MavenRunnerSettings settings = runner.getState().clone();
        settings.getMavenProperties().put("interactiveMode", "false");
        MavenRunnerParameters params = new MavenRunnerParameters();
        params.setWorkingDirPath(project.getBasePath());
        params.setGoals(Collections.singletonList("package"));
        params.setCmdOptions("-Dmaven.test.skip=true");

        runner.run(params, settings, () -> {
            try {
                RunManager runManager = RunManager.getInstance(project);
                RunnerAndConfigurationSettings cf = null;
                if (runManager.getConfigurationSettingsList(ModuleRunConfigurationType.class).isEmpty()){
                    cf = runManager.createConfiguration("deploy", new RunModuleConfigurationFactory(new ModuleRunConfigurationType()));
                    cf.setName("runModuleRunner");
                    ModuleRunConfiguration configuration = (ModuleRunConfiguration) cf.getConfiguration();
                    configuration.setJarPath(path.toString());
                    configuration.setModule(this.module);
                    configuration.setArtifactId(artifactId);
                    configuration.setPnUrl("http://localhost:8080/bridge");
                    runManager.addConfiguration(cf);
                    runManager.setSelectedConfiguration(cf);
                }else {
                    cf = runManager.getConfigurationSettingsList(ModuleRunConfigurationType.class).get(0);
                }

                ProgramRunnerUtil.executeConfiguration(cf, DefaultRunExecutor.getRunExecutorInstance());
            } catch (Exception executionException) {
                executionException.printStackTrace();
            }
        });


    }
}
