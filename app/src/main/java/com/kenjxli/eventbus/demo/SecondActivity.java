package com.kenjxli.eventbus.demo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import com.kenjxli.eventbus.R;
import com.kenjxli.eventbus.library.Event;
import com.kenjxli.eventbus.library.EventBus;
import com.kenjxli.eventbus.library.OnBusCallBack;
import com.kenjxli.eventbus.library.ThreadMode;


public class SecondActivity extends AppCompatActivity implements View.OnClickListener, OnBusCallBack {

    private TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);

        textView = (TextView) findViewById(R.id.stickyTextView);

        findViewById(R.id.postDelay).setOnClickListener(this);
        findViewById(R.id.postSubThread).setOnClickListener(this);
        findViewById(R.id.postSticky).setOnClickListener(this);

        EventBus.register(EventType.DELAY_EVENT, this);
        EventBus.register(EventType.SUB_THREAD_EVENT, this, ThreadMode.SUB_THREAD);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.postDelay:
                Event event1 = EventBus.createEvent(EventType.DELAY_EVENT, "message from second_activity delay 3000ms");
                EventBus.postDelay(event1, 3000);
                break;

            case R.id.postSubThread:
                Event event2 = EventBus.createEvent(EventType.SUB_THREAD_EVENT);
                EventBus.post(event2);
                break;

            case R.id.postSticky:
                Event event1_ = EventBus.createEvent(EventType.STICKY_EVENT, "sticky event work");
                EventBus.postSticky(event1_, 2900);
                Event event2_ = EventBus.createEvent(EventType.STICKY_EVENT, "sticky event work");
                EventBus.postSticky(event2_, 3000);
                Event event3_ = EventBus.createEvent(EventType.STICKY_EVENT, "sticky event work");
                EventBus.postSticky(event3_, 2900);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(2950);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        EventBus.register(EventType.STICKY_EVENT, SecondActivity.this);
                    }
                }).start();
                break;
        }
    }

    @Override
    public void onBusCall(Event msg) {
        switch (msg.type) {
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

            case EventType.STICKY_EVENT:
                textView.setText(msg.data.toString());
                break;
        }
    }
}
