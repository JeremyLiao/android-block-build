package com.jeremyliao.blockcommon.bean;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by liaohailiang on 2019/2/21.
 */
public class VersionInfo {

    private String module;
    private Map<String, Integer> versionMap = new HashMap<>();

    public String getModule() {
        return module;
    }

    public void setModule(String module) {
        this.module = module;
    }

    public Map<String, Integer> getVersionMap() {
        return versionMap;
    }

    public void setVersionMap(Map<String, Integer> versionMap) {
        this.versionMap = versionMap;
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("module: ").append(module).append(";");
        sb.append("versionMap: ").append(versionMap).append(";");
        return sb.toString();
    }
}
