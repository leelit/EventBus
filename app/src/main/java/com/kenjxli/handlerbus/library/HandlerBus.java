package com.kenjxli.handlerbus.library;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

public class HandlerBus extends Handler {

    public static HandlerBus bus = new HandlerBus(Looper.getMainLooper());

    private static EventCollector collectors = new EventCollector();

    private HandlerBus(Looper looper) {
        super(looper);
    }

    @Override
    public void handleMessage(Message msg) {
        collectors.post(msg);
    }

    public static Event createEvent(int type) {
        return createEvent(type, null);
    }

    public static Event createEvent(int type, Object data) {
        Event event = new Event();
        event.type = type;
        event.data = data;
        return event;
    }

    public static void register(int type, OnBusCallBack callBack) {
        register(type, callBack, true);
    }

    public static void register(int type, OnBusCallBack callBack, boolean inMainThread) {
        if (type <= 0) {
            throw new RuntimeException("HandlerBus register error: your type must > 0");
        }
        if (callBack == null) {
            throw new RuntimeException("HandlerBus register error: you must have callback");
        }
        collectors.register(type, callBack, inMainThread);
    }

    public static void unregister(int type, OnBusCallBack callBack) {
        collectors.unregister(type, callBack);
    }

    public static void post(Event event) {
        if (event == null) {
            throw new RuntimeException("HandlerBus post error: event must not be null");
        }
        bus.sendMessage(wrapEventToMessage(event));
    }

    public static void postDelay(Event event, int ms) {
        if (event == null) {
            throw new RuntimeException("HandlerBus postDelay error: event must not be null");
        }
        if (ms <= 0) {
            throw new RuntimeException("HandlerBus postDelay error: your have not set ms parameter");
        }
        bus.sendMessageDelayed(wrapEventToMessage(event), ms);
    }

    public static void postSticky(Event event,int ms){
        if (event == null) {
            throw new RuntimeException("HandlerBus postSticky error: event must not be null");
        }
        if (ms <= 0) {
            throw new RuntimeException("HandlerBus postSticky error: your have not set ms parameter");
        }
        event.stickyMs = ms;
        bus.sendMessage(wrapEventToMessage(event));
    }

    /**
     * 一般不需要调用这个，因为主线程绝大部分情况下都不会阻塞很久
     *
     * @param event
     */
    public static void postUrgently(Event event) {
        if (event == null) {
            throw new RuntimeException("HandlerBus post error: event must not be null");
        }
        bus.sendMessageAtFrontOfQueue(wrapEventToMessage(event));
    }

    private static Message wrapEventToMessage(Event event) {
        Message msg = Message.obtain();
        msg.obj = event;
        return msg;
    }

}
