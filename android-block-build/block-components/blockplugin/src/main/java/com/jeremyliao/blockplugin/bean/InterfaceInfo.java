package com.jeremyliao.blockplugin.bean;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by liaohailiang on 2019/1/24.
 */
public class InterfaceInfo {

    private String module;
    private String interfaceName;
    private String implementClassName;
    private List<InterfaceMethodInfo> methodInfos;

    public String getModule() {
        return module;
    }

    public void setModule(String module) {
        this.module = module;
    }

    public String getInterfaceName() {
        return interfaceName;
    }

    public void setInterfaceName(String interfaceName) {
        this.interfaceName = interfaceName;
    }

    public String getImplementClassName() {
        return implementClassName;
    }

    public void setImplementClassName(String implementClassName) {
        this.implementClassName = implementClassName;
    }

    public List<InterfaceMethodInfo> getMethodInfos() {
        return methodInfos;
    }

    public void setMethodInfos(List<InterfaceMethodInfo> methodInfos) {
        this.methodInfos = methodInfos;
    }

    public static class InterfaceMethodInfo {

        private String name;
        private String returnType;
        private List<String> paramTypes = new ArrayList<>();
        private List<String> params = new ArrayList<>();

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getReturnType() {
            return returnType;
        }

        public void setReturnType(String returnType) {
            this.returnType = returnType;
        }

        public List<String> getParamTypes() {
            return paramTypes;
        }

        public void setParamTypes(List<String> paramTypes) {
            this.paramTypes = paramTypes;
        }

        public List<String> getParams() {
            return params;
        }

        public void setParams(List<String> params) {
            this.params = params;
        }
    }
}
