package com.github.fineke.core;

import com.intellij.execution.ExecutorRegistry;
import com.intellij.execution.ProgramRunnerUtil;
import com.intellij.execution.RunManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.NlsActions;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
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
        Path path = Path.of(mavenProject.getDirectory(), String.format("%s.%s", mavenProject.getFinalName(), mavenProject.getPackaging()));




        MavenRunner runner = MavenRunner.getInstance(project);
        MavenRunnerSettings settings = runner.getState().clone();
        settings.getMavenProperties().put("interactiveMode", "false");
        MavenRunnerParameters params = new MavenRunnerParameters();
        params.setWorkingDirPath(project.getBasePath());
        params.setGoals(Collections.singletonList("install"));
        runner.run(params, settings, () -> System.out.println("maven goal execution completed"));

        RunManager runManager = RunManager.getInstance(project);
        var cf = runManager.createConfiguration("deploy", new DemoConfigurationFactory(new DemoRunConfigurationType()));
        cf.setName(path.toString());
        runManager.addConfiguration(cf);
        ProgramRunnerUtil.executeConfiguration(cf, ExecutorRegistry.getInstance().getRegisteredExecutors()[0]);
    }



}
