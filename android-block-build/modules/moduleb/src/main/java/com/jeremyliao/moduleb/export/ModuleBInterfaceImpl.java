package com.jeremyliao.moduleb.export;

import android.app.Activity;

/**
 * Created by liaohailiang on 2019/1/24.
 */
public class ModuleBInterfaceImpl implements ModuleBInterface {

    @Override
    public void enterMain(Activity activity) {
        if (activity == null) {
            return;
        }
    }

    @Override
    public String getValue(int type) {
        return null;
    }
}
