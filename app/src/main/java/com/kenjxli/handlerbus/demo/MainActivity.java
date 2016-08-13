package com.kenjxli.handlerbus.demo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.kenjxli.handlerbus.R;
import com.kenjxli.handlerbus.library.Event;
import com.kenjxli.handlerbus.library.HandlerBus;
import com.kenjxli.handlerbus.library.OnBusCallBack;


public class MainActivity extends AppCompatActivity implements OnBusCallBack, View.OnClickListener {

    public static final int TYPE_1 = 1;
    public static final int TYPE_2 = 2;
    private TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        HandlerBus.register(TYPE_1, this, false);
        HandlerBus.register(TYPE_2, this);

        textView = (TextView) findViewById(R.id.textView);
        findViewById(R.id.secondActivity).setOnClickListener(this);
    }


    @Override
    public void onBusCall(Event msg) {
        switch (msg.type) {
            case TYPE_1:
                Log.e("tag", Thread.currentThread().toString());
                textView.setText(msg.toString());
                break;
            case TYPE_2:
                Log.e("tag", Thread.currentThread().toString());
                textView.setText(msg.toString());
                break;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.secondActivity:
                startActivity(new Intent(this, SecondActivity.class));
                break;
        }
    }
}
