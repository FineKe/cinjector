package com.github.fineke.core;

import com.intellij.openapi.options.SettingsEditor;
import com.intellij.util.ui.FormBuilder;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class ModuleSettingsEditor extends SettingsEditor<ModuleRunConfiguration> {

  private final JPanel myPanel;
  private final JTextField scriptPathField;
  private final JTextField artifactIdField;
  private final JTextField moduleField;
  private final JTextField pnUrlField;

  public ModuleSettingsEditor() {
    scriptPathField = new JTextField();
    artifactIdField = new JTextField();
    moduleField = new JTextField();
    pnUrlField = new JTextField();

    myPanel = FormBuilder.createFormBuilder()
            .setVertical(true)
            .addLabeledComponent("Jar Path", scriptPathField)
            .addLabeledComponent("Artifact Id", artifactIdField)
            .addLabeledComponent("Module", moduleField)
            .addLabeledComponent("PN URL", pnUrlField)
        .getPanel();
  }

  @Override
  protected void resetEditorFrom(ModuleRunConfiguration moduleRunConfiguration) {
    scriptPathField.setText(moduleRunConfiguration.getJarPath());
    artifactIdField.setText(moduleRunConfiguration.getArtifactId());
    moduleField.setText(moduleRunConfiguration.getModule());
    pnUrlField.setText(moduleRunConfiguration.getPnUrl());
  }

  @Override
  protected void applyEditorTo(@NotNull ModuleRunConfiguration moduleRunConfiguration) {
    moduleRunConfiguration.setJarPath(scriptPathField.getText());
    moduleRunConfiguration.setArtifactId(artifactIdField.getText());
    moduleRunConfiguration.setModule(moduleField.getText());
    moduleRunConfiguration.setPnUrl(pnUrlField.getText());
  }

  @NotNull
  @Override
  protected JComponent createEditor() {
    return myPanel;
  }

}