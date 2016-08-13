package com.kenjxli.handlerbus.demo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.kenjxli.handlerbus.R;
import com.kenjxli.handlerbus.library.HandlerBus;


public class SecondActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);
        findViewById(R.id.postNow).setOnClickListener(this);
        findViewById(R.id.postDelay).setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.postNow:
                HandlerBus.post(HandlerBus.createEvent(MainActivity.TYPE_1, "message1"));
                break;

            case R.id.postDelay:
                HandlerBus.postDelay(HandlerBus.createEvent(MainActivity.TYPE_2, "message2"), 2000);
                break;
        }
    }
}
