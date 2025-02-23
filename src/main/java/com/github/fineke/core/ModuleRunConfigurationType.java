package com.github.fineke.core;

import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.ConfigurationTypeBase;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.util.NotNullLazyValue;

public class ModuleRunConfigurationType extends ConfigurationTypeBase {
    static final String ID = "ModuleRunConfiguration";

    ModuleRunConfigurationType() {
        super(ID, "Run Module", "Run Module configuration type",
                NotNullLazyValue.createValue(() -> AllIcons.Nodes.Console));
        addFactory(new RunModuleConfigurationFactory(this));
    }

}
