package com.github.fineke.core;

import com.intellij.execution.configurations.RunConfigurationOptions;
import com.intellij.openapi.components.StoredProperty;

public class DemoRunConfigurationOptions extends RunConfigurationOptions {

  private final StoredProperty<String> myScriptName =
      string("").provideDelegate(this, "jarPath");

  public String getScriptName() {
    return myScriptName.getValue(this);
  }

  public void setScriptName(String scriptName) {
    myScriptName.setValue(this, scriptName);
  }

}