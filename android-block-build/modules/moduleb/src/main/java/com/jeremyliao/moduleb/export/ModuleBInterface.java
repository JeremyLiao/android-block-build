package com.jeremyliao.moduleb.export;

import android.app.Activity;

import com.jeremyliao.blockbase.annotation.InterfaceExport;

/**
 * Created by liaohailiang on 2019/1/24.
 */
@InterfaceExport(module = "moduleb", impl = ModuleBInterfaceImpl.class)
public interface ModuleBInterface {

    void enterMain(Activity activity);

    String getValue(int type);
}
