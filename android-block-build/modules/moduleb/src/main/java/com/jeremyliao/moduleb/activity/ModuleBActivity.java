package com.jeremyliao.moduleb.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.jeremyliao.blockcore.exception.RemoteCallException;
import com.jeremyliao.moduleb.R;
import com.moduleb.apicenter.ApiCenter;
import com.moduleb.apicenter.inner.bean.modulea.DemoBean;


public class ModuleBActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_b_main);
    }

    public void onTest1(View v) {
        try {
            ApiCenter.get().modulea().enterGoodsPage(this);
        } catch (RemoteCallException e) {
            e.printStackTrace();
            Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show();
        }
    }

    public void onTest2(View v) {
        try {
            DemoBean bean = ApiCenter.get().modulea().getBean();
            Toast.makeText(this, bean.strValue, Toast.LENGTH_LONG).show();
        } catch (RemoteCallException e) {
            e.printStackTrace();
            Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show();
        }
    }

    public void onTest3(View v) {

    }

}
