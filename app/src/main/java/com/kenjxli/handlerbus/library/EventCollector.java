package com.kenjxli.handlerbus.library;

import android.os.Message;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;

public class EventCollector {

    private Map<Integer, List<CallBackParameter>> typeRelativeEvents = new HashMap<>();
    private ReferenceQueue<OnBusCallBack> referenceQueue = new ReferenceQueue<>();

    private static class CallBackParameter {
        WeakReference<OnBusCallBack> callback;
        boolean inMainThread;
    }


    public synchronized void register(int type, OnBusCallBack callBack) {
        this.register(type, callBack, true);
    }

    /**
     * 重构，回调的线程应该在注册时确定，而不是在发送时确定
     *
     * @param inMainThread
     */
    public synchronized void register(int type, OnBusCallBack callBack, boolean inMainThread) {

        if (type <= 0) {
            throw new RuntimeException("HandlerBus register error: your type must > 0");
        }

        List<CallBackParameter> list = typeRelativeEvents.get(type);
        if (list == null) {
            list = new ArrayList<>();
            typeRelativeEvents.put(type, list);
        }

        removeGcObj(list);

        for (CallBackParameter each : list) {
            if (each.callback.get() == callBack) {
                return;
            }
        }

        CallBackParameter parameter = new CallBackParameter();
        parameter.callback = new WeakReference<>(callBack, referenceQueue);
        parameter.inMainThread = inMainThread;
        list.add(parameter);

    }

    public synchronized void unregister(int type, OnBusCallBack callBack) {
        List<CallBackParameter> list = typeRelativeEvents.get(type);

        if (list != null && list.size() > 0) {

            removeGcObj(list);

            for (Iterator<CallBackParameter> iterator = list.iterator(); iterator.hasNext(); ) {
                if (iterator.next().callback.get() == callBack) {
                    iterator.remove();
                    return;
                }
            }

        }
    }

    public synchronized void post(Message msg) {
        final Event event = (Event) msg.obj;
        final List<CallBackParameter> list = typeRelativeEvents.get(event.type);

        if (list != null && list.size() > 0) {

            removeGcObj(list);

            for (final CallBackParameter each : list) {
                if (each.inMainThread) {
                    call(event, each);
                } else {
                    Executors.newCachedThreadPool().execute(new Runnable() {
                        @Override
                        public void run() {
                            call(event, each);
                        }
                    });
                }
            }


        }

    }

    private void removeGcObj(List<CallBackParameter> list) {
        Reference<? extends OnBusCallBack> clearObj;
        while ((clearObj = referenceQueue.poll()) != null) {
            for (Iterator<CallBackParameter> iterator = list.iterator(); iterator.hasNext(); ) {
                if (iterator.next().callback == clearObj) {
                    iterator.remove();
                }
            }
        }
    }

    private void call(Event event, CallBackParameter each) {
        OnBusCallBack callBack = each.callback.get();
        // 有可能正在调用的时候却被回收了
        if (callBack != null) {
            callBack.onBusCall(event);
        }
    }

}
