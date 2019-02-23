package com.jeremyliao.blockcommon.bean;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by liaohailiang on 2019/1/28.
 */
public class RuntimeInfo {

    public static Info createInfo(String moduleName, String interfaceClassName,
                                  String implementClassName, int version) {
        Info info = new Info();
        info.moduleName = moduleName;
        info.interfaceClassName = interfaceClassName;
        info.implementClassName = implementClassName;
        info.version = version;
        return info;
    }

    private Map<String, Info> infoMap = new HashMap<>();
    private Map<String, String> newToOriBeanMap = new HashMap<>();

    public Map<String, Info> getInfoMap() {
        return infoMap;
    }

    public void setInfoMap(Map<String, Info> infoMap) {
        this.infoMap = infoMap;
    }

    public Map<String, String> getNewToOriBeanMap() {
        return newToOriBeanMap;
    }

    public void setNewToOriBeanMap(Map<String, String> newToOriBeanMap) {
        this.newToOriBeanMap = newToOriBeanMap;
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("infoMap: ").append(infoMap).append(";");
        sb.append("newToOriBeanMap: ").append(newToOriBeanMap).append(";");
        return sb.toString();
    }

    public static class Info {

        private String moduleName;
        private String interfaceClassName;
        private String implementClassName;
        private int version;

        public String getModuleName() {
            return moduleName;
        }

        public void setModuleName(String moduleName) {
            this.moduleName = moduleName;
        }

        public String getInterfaceClassName() {
            return interfaceClassName;
        }

        public void setInterfaceClassName(String interfaceClassName) {
            this.interfaceClassName = interfaceClassName;
        }

        public String getImplementClassName() {
            return implementClassName;
        }

        public void setImplementClassName(String implementClassName) {
            this.implementClassName = implementClassName;
        }

        public int getVersion() {
            return version;
        }

        public void setVersion(int version) {
            this.version = version;
        }

        @Override
        public String toString() {
            StringBuffer sb = new StringBuffer();
            sb.append("moduleName: ").append(moduleName).append(";");
            sb.append("interfaceClassName: ").append(interfaceClassName).append(";");
            sb.append("implementClassName: ").append(implementClassName).append(";");
            sb.append("version: ").append(version).append(";");
            return sb.toString();
        }
    }
}
