package com.jeremyliao.modulea.export.bean;

import com.jeremyliao.blockbase.annotation.BeanExport;

/**
 * Created by liaohailiang on 2019/1/31.
 */
@BeanExport
public class DemoBean {
    private int intValue;
    private String strValue;
    private DemoInnerBean innerBean;
    private DemoOutBean outBean;

    public int getIntValue() {
        return intValue;
    }

    public void setIntValue(int intValue) {
        this.intValue = intValue;
    }

    public String getStrValue() {
        return strValue;
    }

    public void setStrValue(String strValue) {
        this.strValue = strValue;
    }

    public DemoInnerBean getInnerBean() {
        return innerBean;
    }

    public void setInnerBean(DemoInnerBean innerBean) {
        this.innerBean = innerBean;
    }

    public DemoOutBean getOutBean() {
        return outBean;
    }

    public void setOutBean(DemoOutBean outBean) {
        this.outBean = outBean;
    }

    @BeanExport
    public static class DemoInnerBean {
        private String strValue;

        public String getStrValue() {
            return strValue;
        }

        public void setStrValue(String strValue) {
            this.strValue = strValue;
        }
    }
}
