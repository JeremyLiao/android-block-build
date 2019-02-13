package com.jeremyliao.blockcommon.bean;

import java.util.List;

/**
 * Created by liaohailiang on 2019/1/30.
 */
public class CompileInfo {

    private String module;
    private InterfaceInfo interfaceInfo;
    private MessageInfo messageInfo;
    private List<BeanInfo> beanInfos;

    public String getModule() {
        return module;
    }

    public void setModule(String module) {
        this.module = module;
    }

    public InterfaceInfo getInterfaceInfo() {
        return interfaceInfo;
    }

    public void setInterfaceInfo(InterfaceInfo interfaceInfo) {
        this.interfaceInfo = interfaceInfo;
    }

    public MessageInfo getMessageInfo() {
        return messageInfo;
    }

    public void setMessageInfo(MessageInfo messageInfo) {
        this.messageInfo = messageInfo;
    }

    public List<BeanInfo> getBeanInfos() {
        return beanInfos;
    }

    public void setBeanInfos(List<BeanInfo> beanInfos) {
        this.beanInfos = beanInfos;
    }
}
