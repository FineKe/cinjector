package com.github.fineke.core;

import com.github.weisj.jsvg.S;
import com.intellij.execution.configurations.RunConfigurationOptions;
import com.intellij.openapi.components.StoredProperty;

public class ModuleRunConfigurationOptions extends RunConfigurationOptions {

    private final StoredProperty<String> jarPath = string("").provideDelegate(this, "jarPath");
    private final StoredProperty<String> artifactId = string("").provideDelegate(this, "artifactId");
    private final StoredProperty<String> module = string("").provideDelegate(this, "module");
    private final StoredProperty<String> pnUrl = string("").provideDelegate(this, "pnUrl");
    private final StoredProperty<String> id = string("").provideDelegate(this, "id");
    private final StoredProperty<String> md5 = string("").provideDelegate(this, "md5");
    public String getJarPath() {
        return jarPath.getValue(this);
    }

    public void setJarPath(String jarPath) {
        this.jarPath.setValue(this, jarPath);
    }

    public String getArtifactId() {
        return artifactId.getValue(this);
    }

    public void setArtifactId(String artifactId) {
        this.artifactId.setValue(this, artifactId);
    }

    public String getModule() {
        return module.getValue(this);
    }

    public void setModule(String module) {
        this.module.setValue(this, module);
    }

    public String getPnUrl() {
        return pnUrl.getValue(this);
    }

    public void setPnUrl(String pnUrl) {
        this.pnUrl.setValue(this, pnUrl);
    }

    public String getId() {
        return id.getValue(this);
    }

    public void setId(String id) {
        this.id.setValue(this, id);
    }

    public String getMd5() {
        return md5.getValue(this);
    }

    public void setMd5(String md5) {
        this.md5.setValue(this, md5);
    }
}