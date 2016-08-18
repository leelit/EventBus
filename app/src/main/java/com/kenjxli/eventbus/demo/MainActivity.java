package com.kenjxli.eventbus.demo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import com.kenjxli.eventbus.R;
import com.kenjxli.eventbus.library.Event;
import com.kenjxli.eventbus.library.EventBus;
import com.kenjxli.eventbus.library.OnBusCallBack;


public class MainActivity extends AppCompatActivity implements OnBusCallBack, View.OnClickListener {


    private TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        EventBus.register(EventType.MAIN_ACTIVITY_EVENT, this);
        EventBus.register(EventType.DELAY_EVENT, this);

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
                Event event = EventBus.createEvent(EventType.FRAGMENT1_EVENT, "message from main_activity");
                EventBus.post(event);
                break;
        }
    }
}
