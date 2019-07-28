package com.jeremyliao.modulea.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.jeremyliao.blockcore.exception.RemoteCallException;
import com.jeremyliao.libcommon.bean.CommonTestBean;
import com.jeremyliao.libcommon.bean.CommonTestParamBean;
import com.jeremyliao.modulea.R;
import com.modulea.apicenter.ApiCenter;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_a_main);
    }

    public void toModuleBMainPage(View view) {
        try {
            ApiCenter.get().moduleb().enterMain(this);
        } catch (RemoteCallException e) {
            e.printStackTrace();
            Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show();
        }
    }

    public void testGetCommonBean(View view) {
        try {
            CommonTestBean bean = ApiCenter.get().moduleb().getCommonBean();
            Toast.makeText(this, bean == null ? "" : bean.toString(), Toast.LENGTH_LONG).show();
        } catch (RemoteCallException e) {
            e.printStackTrace();
            Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show();
        }
    }

    public void testGetCommonBean1(View view) {
        try {
            CommonTestParamBean paramBean = new CommonTestParamBean();
            paramBean.setIntValue(1000);
            paramBean.setStrValue("alpha");
            CommonTestBean bean = ApiCenter.get().moduleb().getCommonBean1(paramBean);
            Toast.makeText(this, bean == null ? "" : bean.toString(), Toast.LENGTH_LONG).show();
        } catch (RemoteCallException e) {
            e.printStackTrace();
            Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show();
        }
    }
}
