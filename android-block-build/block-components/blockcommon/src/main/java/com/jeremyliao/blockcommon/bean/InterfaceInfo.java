package com.jeremyliao.blockcommon.bean;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by liaohailiang on 2019/1/24.
 */
public class InterfaceInfo {

    private String module;
    private String interfaceName;
    private String implementClassName;
    private int version;
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

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public List<InterfaceMethodInfo> getMethodInfos() {
        return methodInfos;
    }

    public void setMethodInfos(List<InterfaceMethodInfo> methodInfos) {
        this.methodInfos = methodInfos;
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("module: ").append(module).append(";");
        sb.append("interfaceName: ").append(interfaceName).append(";");
        sb.append("implementClassName: ").append(implementClassName).append(";");
        sb.append("version: ").append(version).append(";");
        sb.append("methodInfos: ").append(methodInfos).append(";");
        return sb.toString();
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

        @Override
        public String toString() {
            StringBuffer sb = new StringBuffer();
            sb.append("name: ").append(name).append(";");
            sb.append("returnType: ").append(returnType).append(";");
            sb.append("paramTypes: ").append(paramTypes).append(";");
            sb.append("params: ").append(params).append(";");
            return sb.toString();
        }
    }
}
