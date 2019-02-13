package com.jeremyliao.modulea.export.bean;

import com.jeremyliao.blockbase.annotation.BeanExport;

/**
 * Created by liaohailiang on 2019/1/31.
 */
@BeanExport
public class DemoOutBean {

    private String strValue;

    public String getStrValue() {
        return strValue;
    }

    public void setStrValue(String strValue) {
        this.strValue = strValue;
    }
}
