package com.github.fineke.core;

import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.util.ui.FormBuilder;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;

public class DemoSettingsEditor extends SettingsEditor<DemoRunConfiguration> {

  private final JPanel myPanel;
  private final JTextField scriptPathField;
  private final JTextField artifactIdField;
  private final JTextField moduleField;
  private final JTextField pnUrlField;

  public DemoSettingsEditor() {
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
  protected void resetEditorFrom(DemoRunConfiguration demoRunConfiguration) {
    scriptPathField.setText(demoRunConfiguration.getJarPath());
    artifactIdField.setText(demoRunConfiguration.getArtifactId());
    moduleField.setText(demoRunConfiguration.getModule());
    pnUrlField.setText(demoRunConfiguration.getPnUrl());
  }

  @Override
  protected void applyEditorTo(@NotNull DemoRunConfiguration demoRunConfiguration) {
    demoRunConfiguration.setJarPath(scriptPathField.getText());
    demoRunConfiguration.setArtifactId(artifactIdField.getText());
    demoRunConfiguration.setModule(moduleField.getText());
    demoRunConfiguration.setPnUrl(pnUrlField.getText());
  }

  @NotNull
  @Override
  protected JComponent createEditor() {
    return myPanel;
  }

}