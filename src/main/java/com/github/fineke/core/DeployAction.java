package com.github.fineke.core;

import com.intellij.execution.ProgramRunnerUtil;
import com.intellij.execution.RunManager;
import com.intellij.execution.RunnerAndConfigurationSettings;
import com.intellij.execution.executors.DefaultRunExecutor;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.NlsActions;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.Icons;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.idea.maven.execution.MavenRunner;
import org.jetbrains.idea.maven.execution.MavenRunnerParameters;
import org.jetbrains.idea.maven.execution.MavenRunnerSettings;
import org.jetbrains.idea.maven.project.MavenProject;

import java.nio.file.Path;
import java.util.Collections;

public class DeployAction extends AnAction {

    public DeployAction() {
        super("deploy","jhhhhhhh", Icons.ADD_ICON);
    }

    public DeployAction(@Nullable @NlsActions.ActionText String text) {
        super(text,"jhhhhhhh", Icons.ADD_ICON);
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        Project project = e.getProject();
        VirtualFile currentFile = e.getData(CommonDataKeys.VIRTUAL_FILE);
        MavenProject mavenProject = MavenProjectUtils.getMavenProjectForFile(project, currentFile);
        Path path = Path.of(mavenProject.getDirectory(), String.format("%s.%s", mavenProject.getFinalName()+"-ark-biz", mavenProject.getPackaging()));
        String artifactId = mavenProject.getName();


        MavenRunner runner = MavenRunner.getInstance(project);
        MavenRunnerSettings settings = runner.getState().clone();
        settings.getMavenProperties().put("interactiveMode", "false");
        MavenRunnerParameters params = new MavenRunnerParameters();
        params.setWorkingDirPath(project.getBasePath());
        params.setGoals(Collections.singletonList("install"));

        runner.run(params, settings, () -> {
            try {
                RunManager runManager = RunManager.getInstance(project);
                RunnerAndConfigurationSettings cf = null;
                if (runManager.getConfigurationSettingsList(DemoRunConfigurationType.class).isEmpty()){
                    cf = runManager.createConfiguration("deploy", new DemoConfigurationFactory(new DemoRunConfigurationType()));
                    cf.setName("runModuleRunner");
                    ((DemoRunConfiguration) cf.getConfiguration()).setJarPath(path.toString());
                    ((DemoRunConfiguration) cf.getConfiguration()).setModule("demo");
                    ((DemoRunConfiguration) cf.getConfiguration()).setArtifactId(artifactId);
                    ((DemoRunConfiguration) cf.getConfiguration()).setPnUrl("http://localhost:8080/bridge");
                    // todo: set artifactId, module, pnUrl
                    runManager.addConfiguration(cf);
                    runManager.setSelectedConfiguration(cf);
                }else {
                    cf = runManager.getConfigurationSettingsList(DemoRunConfigurationType.class).get(0);

                }

                ProgramRunnerUtil.executeConfiguration(cf, DefaultRunExecutor.getRunExecutorInstance());
            } catch (Exception executionException) {
                executionException.printStackTrace();
            }
        });


    }



}
