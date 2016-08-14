package com.kenjxli.handlerbus.demo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.kenjxli.handlerbus.R;
import com.kenjxli.handlerbus.library.Event;
import com.kenjxli.handlerbus.library.HandlerBus;


public class SecondActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);
        findViewById(R.id.postDelay).setOnClickListener(this);
        findViewById(R.id.postSubThread).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.postDelay:
                Event event = HandlerBus.createEvent(EventType.DELAY_EVENT, "message from second_activity delay 3000ms");
                HandlerBus.postDelay(event, 3000);
                break;

            case R.id.postSubThread:
                event = HandlerBus.createEvent(EventType.SUB_THREAD_EVENT);
                HandlerBus.post(event);
                break;
        }
    }
}
