package com.jeremyliao.modulea.export;

import android.app.Activity;
import android.content.Intent;

import com.jeremyliao.modulea.activity.GoodsActivity;
import com.jeremyliao.modulea.activity.MainActivity;
import com.jeremyliao.modulea.export.bean.DemoBean;
import com.jeremyliao.modulea.export.bean.DemoOutBean;

/**
 * Created by liaohailiang on 2019/1/24.
 */
public class ModuleAInterfaceImpl implements ModuleAInterface {

    private DemoBean demoBean;

    @Override
    public void enterMain(Activity activity) {
        if (activity == null) {
            return;
        }
        activity.startActivity(new Intent(activity, MainActivity.class));
    }

    @Override
    public void enterGoodsPage(Activity activity) {
        if (activity == null) {
            return;
        }
        activity.startActivity(new Intent(activity, GoodsActivity.class));
    }

    @Override
    public String getValue(int type) {
        return null;
    }

    @Override
    public DemoBean getBean() {
        return getSampleDemoBean();
    }

    @Override
    public void setBean(DemoBean demoBean) {
        this.demoBean = demoBean;
    }

    private static DemoBean getSampleDemoBean() {
        DemoBean demoBean = new DemoBean();
        demoBean.setIntValue(100);
        demoBean.setStrValue("DemoBean");
        DemoBean.DemoInnerBean demoInnerBean = new DemoBean.DemoInnerBean();
        demoInnerBean.setStrValue("DemoInnerBean");
        demoBean.setInnerBean(demoInnerBean);
        DemoOutBean demoOutBean = new DemoOutBean();
        demoOutBean.setStrValue("DemoOutBean");
        demoBean.setOutBean(demoOutBean);
        return demoBean;
    }
}
