package com.kenjxli.handlerbus.demo;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.kenjxli.handlerbus.R;
import com.kenjxli.handlerbus.library.Event;
import com.kenjxli.handlerbus.library.HandlerBus;
import com.kenjxli.handlerbus.library.OnBusCallBack;

/**
 * Created by kenjxli on 2016/8/14.
 */
public class Fragment1 extends Fragment implements OnBusCallBack {

    private TextView textView;
    private Button button;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        HandlerBus.register(EventType.FRAGMENT1_EVENT, this);
        HandlerBus.register(EventType.DELAY_EVENT, this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment1, container, false);
        textView = (TextView) view.findViewById(R.id.textView);
        button = (Button) view.findViewById(R.id.btn);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Event event = HandlerBus.createEvent(EventType.FRAGMENT2_EVENT, "message from fragment1");
                HandlerBus.post(event);
            }
        });
        return view;
    }

    private static int i = 1;

    @Override
    public void onBusCall(Event msg) {
        switch (msg.type) {
            case EventType.FRAGMENT1_EVENT:
                textView.setText(msg.data.toString() + " times:" + (i++));
                break;

            case EventType.DELAY_EVENT:
                textView.setText(msg.data.toString());
                break;

        }
    }
}
