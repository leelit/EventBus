package com.kenjxli.handlerbus.demo;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.kenjxli.handlerbus.R;
import com.kenjxli.handlerbus.library.Event;
import com.kenjxli.handlerbus.library.HandlerBus;
import com.kenjxli.handlerbus.library.OnBusCallBack;


public class MainActivity extends AppCompatActivity implements OnBusCallBack, View.OnClickListener {


    private TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        HandlerBus.register(EventType.MAIN_ACTIVITY_EVENT, this);
        HandlerBus.register(EventType.DELAY_EVENT, this);
        HandlerBus.register(EventType.SUB_THREAD_EVENT, this, false);

        textView = (TextView) findViewById(R.id.textView);

        findViewById(R.id.sendToFragment1).setOnClickListener(this);
        findViewById(R.id.secondActivity).setOnClickListener(this);
    }

    @Override
    public void onBusCall(Event msg) {
        switch (msg.type) {
            case EventType.MAIN_ACTIVITY_EVENT:
                textView.setText(msg.data.toString() + " times:" + (i++));
                break;

            case EventType.DELAY_EVENT:
                textView.setText(msg.data.toString());
                break;

            case EventType.SUB_THREAD_EVENT:
                final String currentThread = Thread.currentThread().toString();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        textView.setText("execute in: " + currentThread);
                    }
                });
                break;
        }
    }

    private static int i = 1;

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.secondActivity:
                startActivity(new Intent(this, SecondActivity.class));
                break;

            case R.id.sendToFragment1:
                Event event = HandlerBus.createEvent(EventType.FRAGMENT1_EVENT, "message from main_activity");
                HandlerBus.post(event);
                break;
        }
    }
}
