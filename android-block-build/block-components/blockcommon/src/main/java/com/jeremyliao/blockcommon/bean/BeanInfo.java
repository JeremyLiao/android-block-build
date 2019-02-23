package com.jeremyliao.blockcommon.bean;

import java.util.List;

/**
 * Created by liaohailiang on 2019/1/24.
 */
public class BeanInfo {

    private String className;
    private List<FieldInfo> fieldInfos;

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public List<FieldInfo> getFieldInfos() {
        return fieldInfos;
    }

    public void setFieldInfos(List<FieldInfo> fieldInfos) {
        this.fieldInfos = fieldInfos;
    }

    public static class FieldInfo {

        private String name;
        private String returnType;

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

        @Override
        public String toString() {
            StringBuffer sb = new StringBuffer();
            sb.append("name: ").append(name).append(";");
            sb.append("returnType: ").append(returnType).append(";");
            return sb.toString();
        }
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("className: ").append(className).append(";");
        sb.append("fieldInfos: ").append(fieldInfos).append(";");
        return sb.toString();
    }
}
