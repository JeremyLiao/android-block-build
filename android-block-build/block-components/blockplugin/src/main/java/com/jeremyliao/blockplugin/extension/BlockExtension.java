package com.jeremyliao.blockplugin.extension;

import groovy.lang.Closure;

/**
 * Created by liaohailiang on 2019/1/25.
 */
public class BlockExtension {

    private String packageName;
    private Closure dependencies;

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public Closure getDependencies() {
        return dependencies;
    }

    public void setDependencies(Closure dependencies) {
        this.dependencies = dependencies;
    }
}
