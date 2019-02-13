package com.jeremyliao.block.app;

import android.app.Application;

import com.jeremyliao.blockcore.ApiManager;


/**
 * Created by liaohailiang on 2018/8/18.
 */
public class DemoApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        // 初始化
        ApiManager.init(this);
    }
}
