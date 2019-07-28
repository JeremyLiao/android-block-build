package com.jeremyliao.libcommon.bean;

/**
 * Created by liaohailiang on 2019/2/26.
 */
public class CommonTestParamBean {

    private int intValue;
    private String strValue;

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

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("intValue: ").append(intValue).append(";");
        sb.append("strValue: ").append(strValue).append(";");
        return sb.toString();
    }
}
