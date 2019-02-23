package com.jeremyliao.blockplugin.extension;

import groovy.lang.Closure;

/**
 * Created by liaohailiang on 2019/1/25.
 */
public class BlockExtension {

    private String module;
    private Closure dependencies;

    public String getModule() {
        return module;
    }

    public void setModule(String module) {
        this.module = module;
    }

    public Closure getDependencies() {
        return dependencies;
    }

    public void setDependencies(Closure dependencies) {
        this.dependencies = dependencies;
    }
}
