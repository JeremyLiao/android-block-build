package com.jeremyliao.modulea.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.jeremyliao.modulea.R;
import com.modulea.apicenter.ApiCenter;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_a_main);
    }

    public void toModuleBMainPage(View view){
        ApiCenter.get().moduleb().enterMain(this);
    }
}
