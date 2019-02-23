package com.jeremyliao.moduleb.export;

import android.app.Activity;
import android.content.Intent;

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
}
