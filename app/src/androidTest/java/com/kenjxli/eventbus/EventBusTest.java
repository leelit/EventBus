package com.kenjxli.eventbus;

import android.os.Looper;
import android.test.AndroidTestCase;
import android.util.Log;

import com.kenjxli.eventbus.library.Event;
import com.kenjxli.eventbus.library.EventBus;
import com.kenjxli.eventbus.library.OnBusCallBack;
import com.kenjxli.eventbus.library.ThreadMode;

/**
 * Created by kenjxli on 2016/8/12.
 */
public class EventBusTest extends AndroidTestCase {

    private static final String MESSAGE1 = "message1";
    private static final String MESSAGE2 = "message2";

    private static final int TYPE1 = 1;
    private static final int TYPE2 = TYPE1 + 1;

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        EventBus.clearAllEvents();
    }

    public void testRegister() throws InterruptedException {
        EventBus.register(TYPE1, new OnBusCallBack() {
            @Override
            public void onBusCall(Event msg) {
                assertEquals(MESSAGE1, msg.data);
            }
        });
        EventBus.register(TYPE2, new OnBusCallBack() {
            @Override
            public void onBusCall(Event msg) {
                assertEquals(MESSAGE2, msg.data);
            }
        });
        EventBus.post(createEvent(TYPE1, MESSAGE1));
        EventBus.post(createEvent(TYPE2, MESSAGE2));
        Thread.sleep(100); // 默认主线程执行，给点时间给上面的事件在主线程回调
    }

    public void testThreadMode() throws InterruptedException {
        EventBus.register(TYPE1, new OnBusCallBack() {
            @Override
            public void onBusCall(Event msg) {
                Log.e("ThreadMode", "main: " + Thread.currentThread().toString());
                assertTrue(Thread.currentThread() == Looper.getMainLooper().getThread());
            }
        });
        EventBus.register(TYPE2, new OnBusCallBack() {
            @Override
            public void onBusCall(Event msg) {
                Log.e("ThreadMode", "sub: " + Thread.currentThread().toString());
                assertTrue(Thread.currentThread() != Looper.getMainLooper().getThread());
            }
        }, ThreadMode.SUB_THREAD); // 最后一个参数为false
        EventBus.register(3, new OnBusCallBack() {
            @Override
            public void onBusCall(Event msg) {
                Log.e("ThreadMode", "current: " + Thread.currentThread().toString());
                assertTrue(Thread.currentThread() != Looper.getMainLooper().getThread());
            }
        }, ThreadMode.CURRENT);

        EventBus.post(createEvent(TYPE1));
        EventBus.post(createEvent(TYPE2));
        EventBus.post(createEvent(3));
        Thread.sleep(100); // 默认主线程执行，给点时间给上面的事件在主线程回调
    }


    public void testUnregister() throws InterruptedException {
        OnBusCallBack callBack = new OnBusCallBack() {
            @Override
            public void onBusCall(Event msg) {
                fail();
            }
        };
        EventBus.register(TYPE1, callBack);
        EventBus.unregister(TYPE1, callBack);
        EventBus.post(createEvent(TYPE1));
        Thread.sleep(100); // 默认主线程执行，给点时间给上面的事件在主线程回调
    }

    private long postTime;

    public void testDelay() throws InterruptedException {
        EventBus.register(TYPE1, new OnBusCallBack() {
            @Override
            public void onBusCall(Event msg) {
                long timeGap = System.currentTimeMillis() - postTime - 2000;
                if (Math.abs(timeGap) > 10) {
                    fail();
                }
                Log.e("tag", Thread.currentThread().toString());
            }
        }, ThreadMode.CURRENT);
        EventBus.postDelay(createEvent(TYPE1), 2000);
        postTime = System.currentTimeMillis();
        Thread.sleep(2100);
    }

    public void testWeakGc() throws InterruptedException {
        OnBusCallBack onBusCallBack = new OnBusCallBack() {
            @Override
            public void onBusCall(Event msg) {
                fail(); // 这里应该回收了
            }
        };
        EventBus.register(TYPE1, onBusCallBack);
        onBusCallBack = null; // 这里应该回收了
        System.gc();
        Thread.sleep(2000);

        EventBus.post(createEvent(TYPE1));
        Thread.sleep(1000);
    }

    private boolean isStickyCall;

    public void testSticky1() throws InterruptedException {
        Event event = createEvent(TYPE1, MESSAGE1);
        EventBus.postSticky(event, 2500); // sticky 事件为2500ms

        Thread.sleep(2497);                  // 延迟2497ms后注册能接受到事件
        EventBus.register(TYPE1, new OnBusCallBack() {
            @Override
            public void onBusCall(Event msg) {
                isStickyCall = true;
            }
        });
        EventBus.register(TYPE1, new OnBusCallBack() {
            @Override
            public void onBusCall(Event msg) {
                assertTrue(Thread.currentThread() != Looper.getMainLooper().getThread());
            }
        }, ThreadMode.SUB_THREAD); // 子线程

        Thread.sleep(100); // 给点时间给上面线程回调
        if (!isStickyCall) {
            fail();
        }
    }

    public void testSticky2() throws InterruptedException {
        Event event = createEvent(TYPE1, MESSAGE1);
        EventBus.postSticky(event, 2500); // sticky 事件为2500ms

        Thread.sleep(2503);                  // 延迟2501ms后注册不能接受的事件
        EventBus.register(TYPE1, new OnBusCallBack() {
            @Override
            public void onBusCall(Event msg) {
                isStickyCall = false;
            }
        });

        Thread.sleep(100); // 给点时间给上面线程回调
        if (isStickyCall) {
            fail();
        }
    }

    public void testSticky3() throws InterruptedException {
        Event event1 = createEvent(TYPE1);
        EventBus.postSticky(event1, 2900); //  第一次
        Event event2 = createEvent(TYPE1);
        EventBus.postSticky(event2, 3000);   // 第二次
        Event event3 = createEvent(TYPE1);
        EventBus.postSticky(event3, 2900); //  第三次

        Thread.sleep(2997);                  // 保留时间长的，所以会调用
        EventBus.register(TYPE1, new OnBusCallBack() {
            @Override
            public void onBusCall(Event msg) {
                isStickyCall = true;
            }
        });

        Thread.sleep(100); // 给点时间给上面线程回调
        if (!isStickyCall) {
            fail();
        }
    }

    private Event createEvent(int type) {
        return EventBus.createEvent(type);
    }

    private Event createEvent(int type, Object data) {
        return EventBus.createEvent(type, data);
    }
}
