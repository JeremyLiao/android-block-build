package com.jeremyliao.moduleb.export;

import android.app.Activity;
import android.content.Intent;

import com.jeremyliao.libcommon.bean.CommonTestBean;
import com.jeremyliao.libcommon.bean.CommonTestParamBean;
import com.jeremyliao.moduleb.activity.ModuleBActivity;

/**
 * Created by liaohailiang on 2019/1/24.
 */
public class ModuleBInterfaceImpl implements ModuleBInterface {

    @Override
    public void enterMain(Activity activity) {
        if (activity == null) {
            return;
        }
        activity.startActivity(new Intent(activity, ModuleBActivity.class));
    }

    @Override
    public String getValue(int type) {
        return "hello world";
    }

    @Override
    public CommonTestBean getCommonBean() {
        CommonTestBean bean = new CommonTestBean();
        bean.setIntValue(100);
        bean.setStrValue("hello");
        return bean;
    }

    @Override
    public CommonTestBean getCommonBean1(CommonTestParamBean paramBean) {
        CommonTestBean bean = new CommonTestBean();
        bean.setIntValue(paramBean.getIntValue());
        bean.setStrValue(paramBean.getStrValue());
        return bean;
    }
}
