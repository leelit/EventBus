package com.kenjxli.eventbus.library;

import android.os.Handler;
import android.os.Looper;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;

public class EventBus {

    private static Map<Integer, List<CallBackParameter>> typeRelativeEvents = new HashMap<>();
    private static ReferenceQueue<OnBusCallBack> referenceQueue = new ReferenceQueue<>();

    private static Map<Integer, Event.StickyEvent> stickyEvents = new HashMap<>();

    private static class CallBackParameter {
        WeakReference<OnBusCallBack> callback; // can be GC
        ThreadMode threadMode;
    }

    public static synchronized void register(int type, OnBusCallBack callBack) {
        register(type, callBack, ThreadMode.MAIN_THREAD);
    }

    /**
     * 重构，回调的线程应该在注册时确定，而不是在发送时确定
     *
     * @param
     */
    public static synchronized void register(int type, OnBusCallBack callBack, ThreadMode mode) {
        if (type <= 0) {
            throw new RuntimeException("EventBus register error: your type must > 0");
        }
        if (callBack == null) {
            throw new RuntimeException("EventBus register error: you must have callback");
        }

        checkSticky(type, callBack, mode); // 注册时检查这个事件是不是之前发送的sticky事件

        List<CallBackParameter> eventList = typeRelativeEvents.get(type);
        if (eventList == null) {
            eventList = new ArrayList<>();
            typeRelativeEvents.put(type, eventList);
        }

        removeGcObj(eventList);

        for (CallBackParameter each : eventList) {
            if (each.callback.get() == callBack) {
                return;
            }
        }

        CallBackParameter parameter = new CallBackParameter();
        parameter.callback = new WeakReference<>(callBack, referenceQueue);
        parameter.threadMode = mode;
        eventList.add(parameter);

    }

    private static void checkSticky(int type, final OnBusCallBack callBack, ThreadMode mode) {
        if (stickyEvents.containsKey(type)) {
            final Event.StickyEvent stickyEvent = stickyEvents.get(type);
            long lastTime = stickyEvent.postTime;
            if (System.currentTimeMillis() - lastTime <= stickyEvent.event.stickyMs) {
                switch (mode) {
                    case MAIN_THREAD:
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                callBack.onBusCall(stickyEvent.event);
                            }
                        });
                        break;
                    case SUB_THREAD:
                        Executors.newCachedThreadPool().execute(new Runnable() {
                            @Override
                            public void run() {
                                callBack.onBusCall(stickyEvent.event);
                            }
                        });
                        break;
                    case POST_THREAD:
                        callBack.onBusCall(stickyEvent.event);
                        break;
                }
            } else {
                stickyEvents.remove(type);
            }
        }
    }

    public static synchronized void unregister(int type, OnBusCallBack callBack) {
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


    public static synchronized void post(final Event event) {
        if (event == null) {
            throw new RuntimeException("EventBus post error: event must not be null");
        }

        if (event.stickyMs > 0) {
            saveStickyEvent(event);
        }

        List<CallBackParameter> list = typeRelativeEvents.get(event.type);

        if (list != null && list.size() > 0) {

            removeGcObj(list);

            for (final CallBackParameter each : list) {
                switch (each.threadMode) {
                    case MAIN_THREAD:
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                call(event, each);
                            }
                        });
                        break;
                    case SUB_THREAD:
                        Executors.newCachedThreadPool().execute(new Runnable() {
                            @Override
                            public void run() {
                                call(event, each);
                            }
                        });
                        break;
                    case POST_THREAD:
                        call(event, each);
                        break;
                }


            }

        }

    }

    private static void saveStickyEvent(Event event) {
        Event.StickyEvent lastEvent = stickyEvents.get(event.type);
        // 没有 || 选择sticky事件更长的
        if (lastEvent == null || event.stickyMs > lastEvent.event.stickyMs) {
            Event.StickyEvent stickyEvent = new Event.StickyEvent(event, System.currentTimeMillis());
            stickyEvents.put(event.type, stickyEvent);
        }
    }

    public static synchronized void postSticky(Event event, int ms) {
        if (event == null) {
            throw new RuntimeException("EventBus postSticky error: event must not be null");
        }
        if (ms <= 0) {
            throw new RuntimeException("EventBus postSticky error: your have not set ms parameter");
        }
        event.stickyMs = ms;
        post(event);
    }

    /**
     * 如果使用postDelay，指定回调线程为current时，真正回调线程是Timer
     * 其他两种方式不受影响
     * @param event
     * @param ms
     */
    public static void postDelay(final Event event, int ms) {
        if (event == null) {
            throw new RuntimeException("EventBus postDelay error: event must not be null");
        }
        if (ms <= 0) {
            throw new RuntimeException("EventBus postDelay error: your have not set ms parameter");
        }
        Timer timer = new Timer();
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                post(event);
            }
        };
        timer.schedule(timerTask, ms);
    }

    private static void removeGcObj(List<CallBackParameter> list) {
        Reference<? extends OnBusCallBack> clearObj;
        while ((clearObj = referenceQueue.poll()) != null) {
            for (Iterator<CallBackParameter> iterator = list.iterator(); iterator.hasNext(); ) {
                if (iterator.next().callback == clearObj) {
                    iterator.remove();
                }
            }
        }
    }

    private static void call(Event event, CallBackParameter each) {
        OnBusCallBack callBack = each.callback.get();
        // 有可能正在调用的时候却被回收了
        if (callBack != null) {
            callBack.onBusCall(event);
        }
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

    /**
     * just for test!!
     */
    public static void clearAllEvents() {
        typeRelativeEvents.clear();
    }
}
