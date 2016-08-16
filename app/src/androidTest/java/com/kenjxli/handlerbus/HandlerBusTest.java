package com.kenjxli.handlerbus;

import android.os.Looper;
import android.test.AndroidTestCase;

import com.kenjxli.handlerbus.library.Event;
import com.kenjxli.handlerbus.library.HandlerBus;
import com.kenjxli.handlerbus.library.OnBusCallBack;

/**
 * Created by kenjxli on 2016/8/12.
 */
public class HandlerBusTest extends AndroidTestCase {

    private static final String MESSAGE1 = "message1";
    private static final String MESSAGE2 = "message2";

    private static final int TYPE1 = 1;
    private static final int TYPE2 = TYPE1 + 1;

    public void testRegister() throws InterruptedException {
        HandlerBus.register(TYPE1, new OnBusCallBack() {
            @Override
            public void onBusCall(Event msg) {
                assertEquals(MESSAGE1, msg.data);
            }
        });
        HandlerBus.register(TYPE2, new OnBusCallBack() {
            @Override
            public void onBusCall(Event msg) {
                assertEquals(MESSAGE2, msg.data);
            }
        });
        HandlerBus.post(createEvent(TYPE1, MESSAGE1));
        HandlerBus.post(createEvent(TYPE2, MESSAGE2));
        Thread.sleep(1000);
    }

    public void testSubThread() throws InterruptedException {
        HandlerBus.register(TYPE1, new OnBusCallBack() {
            @Override
            public void onBusCall(Event msg) {
                assertTrue(Thread.currentThread() == Looper.getMainLooper().getThread());
            }
        });
        HandlerBus.register(TYPE2, new OnBusCallBack() {
            @Override
            public void onBusCall(Event msg) {
                assertTrue(Thread.currentThread() != Looper.getMainLooper().getThread());
            }
        }, false); // 最后一个参数为false

        HandlerBus.post(createEvent(TYPE1));
        HandlerBus.post(createEvent(TYPE2));
        Thread.sleep(1000);
    }


    public void testUnregister() throws InterruptedException {
        OnBusCallBack callBack = new OnBusCallBack() {
            @Override
            public void onBusCall(Event msg) {
                fail();
            }
        };
        HandlerBus.register(TYPE1, callBack);
        HandlerBus.unregister(TYPE1, callBack);
        HandlerBus.post(createEvent(TYPE1));
    }

    private long postTime;

    public void testDelay() throws InterruptedException {
        HandlerBus.register(TYPE1, new OnBusCallBack() {
            @Override
            public void onBusCall(Event msg) {
                if (System.currentTimeMillis() - postTime < 2000) {
                    fail();
                }
            }
        });
        HandlerBus.postDelay(createEvent(TYPE1), 2000);
        postTime = System.currentTimeMillis();
        Thread.sleep(2200);
    }

    public void testWeakGc() throws InterruptedException {
        OnBusCallBack onBusCallBack = new OnBusCallBack() {
            @Override
            public void onBusCall(Event msg) {
                fail(); // 这里应该回收了
            }
        };
        HandlerBus.register(TYPE1, onBusCallBack);
        onBusCallBack = null; // 这里应该回收了
        System.gc();
        Thread.sleep(2000);

        HandlerBus.post(createEvent(TYPE1));
        Thread.sleep(1000);
    }

    private boolean isStickyCall;

    public void testSticky1() throws InterruptedException {
        Event event = createEvent(TYPE1, MESSAGE1);
        HandlerBus.postSticky(event, 2500); // sticky 事件为2500ms

        Thread.sleep(2498);                  // 延迟2498ms后注册能接受到事件
        HandlerBus.register(TYPE1, new OnBusCallBack() {
            @Override
            public void onBusCall(Event msg) {
                isStickyCall = true;
            }
        });
        HandlerBus.register(TYPE1, new OnBusCallBack() {
            @Override
            public void onBusCall(Event msg) {
                assertTrue(Thread.currentThread() != Looper.getMainLooper().getThread());
            }
        }, false); // 子线程
        Thread.sleep(10); // 给点时间给上面子线程
        if (!isStickyCall) {
            fail();
        }
    }

    public void testSticky2() throws InterruptedException {
        Event event = createEvent(TYPE1, MESSAGE1);
        HandlerBus.postSticky(event, 2500); // sticky 事件为2500ms

        Thread.sleep(2501);                  // 延迟2501ms后注册不能接受的事件
        HandlerBus.register(TYPE1, new OnBusCallBack() {
            @Override
            public void onBusCall(Event msg) {
                isStickyCall = false;
            }
        });

        Thread.sleep(1);
        if (isStickyCall) {
            fail();
        }
    }

    public void testSticky3() throws InterruptedException {
        Event event1 = createEvent(TYPE1);
        HandlerBus.postSticky(event1, 2900); //  第一次
        Event event2 = createEvent(TYPE1);
        HandlerBus.postSticky(event2, 3000);   // 第二次
        Event event3 = createEvent(TYPE1);
        HandlerBus.postSticky(event3, 2900); //  第三次

        Thread.sleep(2950);                  // 保留时间长的，所以会调用
        HandlerBus.register(TYPE1, new OnBusCallBack() {
            @Override
            public void onBusCall(Event msg) {
                isStickyCall = true;
            }
        });
        Thread.sleep(1);
        if (!isStickyCall) {
            fail();
        }
    }

    private Event createEvent(int type) {
        return HandlerBus.createEvent(type);
    }

    private Event createEvent(int type, Object data) {
        return HandlerBus.createEvent(type, data);
    }
}
