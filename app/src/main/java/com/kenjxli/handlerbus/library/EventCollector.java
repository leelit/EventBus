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

    private Map<Integer, Event.StickyEvent> stickyEvents = new HashMap<>();

    private static class CallBackParameter {
        WeakReference<OnBusCallBack> callback;
        boolean inMainThread;
    }


    /**
     * 重构，回调的线程应该在注册时确定，而不是在发送时确定
     *
     * @param inMainThread
     */
    public synchronized void register(int type, OnBusCallBack callBack, boolean inMainThread) {

        checkSticky(type, callBack, inMainThread); // 注册时检查这个事件是不是之前发送的sticky事件

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

    private void checkSticky(int type, final OnBusCallBack callBack, boolean inMainThread) {
        if (stickyEvents.containsKey(type)) {
            final Event.StickyEvent stickyEvent = stickyEvents.get(type);
            long lastTime = stickyEvent.postTime;
            if (System.currentTimeMillis() - lastTime <= stickyEvent.event.stickyMs) {
                if (inMainThread) {
                    callBack.onBusCall(stickyEvent.event);
                } else {
                    Executors.newCachedThreadPool().execute(new Runnable() {
                        @Override
                        public void run() {
                            callBack.onBusCall(stickyEvent.event);
                        }
                    });
                }
            } else {
                stickyEvents.remove(type);
            }
        }
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
        saveStickyEvent(event);

        List<CallBackParameter> list = typeRelativeEvents.get(event.type);

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

    private void saveStickyEvent(Event event) {
        if (event.stickyMs > 0) {
            Event.StickyEvent lastEvent = stickyEvents.get(event.type);
            // 没有 || 选择sticky事件更长的
            if (lastEvent == null || event.stickyMs > lastEvent.event.stickyMs) {
                Event.StickyEvent stickyEvent = new Event.StickyEvent(event, System.currentTimeMillis());
                stickyEvents.put(event.type, stickyEvent);
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
