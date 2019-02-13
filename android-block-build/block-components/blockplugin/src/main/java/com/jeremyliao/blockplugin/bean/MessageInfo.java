package com.jeremyliao.blockplugin.bean;

import java.util.List;

/**
 * Created by liaohailiang on 2019/1/24.
 */
public class MessageInfo {

    private String module;
    private List<MessageFieldInfo> messageFieldInfos;

    public String getModule() {
        return module;
    }

    public void setModule(String module) {
        this.module = module;
    }

    public List<MessageFieldInfo> getMessageFieldInfos() {
        return messageFieldInfos;
    }

    public void setMessageFieldInfos(List<MessageFieldInfo> messageFieldInfos) {
        this.messageFieldInfos = messageFieldInfos;
    }

    public static class MessageFieldInfo {

        private String name;
        private String value;
        private String type;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }
    }
}
