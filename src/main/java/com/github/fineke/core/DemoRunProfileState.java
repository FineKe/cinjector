package com.github.fineke.core;

import com.intellij.execution.*;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.configurations.RunProfileState;
import com.intellij.execution.filters.TextConsoleBuilderFactory;
import com.intellij.execution.process.KillableProcessHandler;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.runners.ProgramRunner;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.openapi.project.Project;

public class DemoRunProfileState implements RunProfileState {
    private final DemoRunConfiguration config;
    private final ExecutionEnvironment environment;
    private final Project project;

    public DemoRunProfileState(DemoRunConfiguration config, ExecutionEnvironment environment) {
        this.config = config;
        this.environment = environment;
        this.project = environment.getProject();
    }

    @Override
    public ExecutionResult execute(Executor executor, ProgramRunner<?> runner) throws ExecutionException {
        Project project = environment.getProject();
        String jarPath = config.getJarPath();
        String baserUrl = config.getPnUrl();
        String artifactId = config.getArtifactId();
        String module = config.getModule();


        if (jarPath == null ) {
            throw new ExecutionException("JAR path, Docker API URL, or container name is not set");
        }

        ConsoleView consoleView = TextConsoleBuilderFactory.getInstance().createBuilder(project).getConsole();

        // 2. 执行 Docker 日志查看
        KillableProcessHandler logProcess = null;
        try {
            logProcess = new KillableProcessHandler(new GeneralCommandLine("docker", "-H" ,"127.0.0.1:2375",  "logs" ,"-f", "parser-node"));
            RunContentExecutor runContentExecutor = new RunContentExecutor(project, logProcess);
            runContentExecutor.withTitle("Parser Node Logs").withActivateToolWindow(true).run();
        } catch (Exception e) {
           e.printStackTrace();
        }
        consoleView.attachToProcess(logProcess);

        return new DefaultExecutionResult(consoleView, logProcess);
    }


    private ConsoleView createConsoleView(String title, ProcessHandler handler) {
        ConsoleView consoleView = TextConsoleBuilderFactory.getInstance().createBuilder(project).getConsole();
        consoleView.attachToProcess(handler);
        new RunContentExecutor(project, handler)
                .withTitle(title)
                .withActivateToolWindow(true)
                .run();
        return consoleView;
    }
}
