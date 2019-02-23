package com.jeremyliao.block;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.jeremyliao.moduleb.activity.ModuleBActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        startActivity(new Intent(this,
                com.jeremyliao.modulea.activity.MainActivity.class));
        finish();
    }
}
