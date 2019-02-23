package com.jeremyliao.modulea.export;

import android.app.Activity;

import com.jeremyliao.blockbase.annotation.InterfaceExport;
import com.jeremyliao.modulea.export.bean.DemoBean;

/**
 * Created by liaohailiang on 2019/1/24.
 */
@InterfaceExport(module = "modulea", impl = ModuleAInterfaceImpl.class, version = 2)
public interface ModuleAInterface {

    void enterMain(Activity activity);

    void enterGoodsPage(Activity activity);

    String getValue(int type);

    DemoBean getBean();

    void setBean(DemoBean demoBean);
}
