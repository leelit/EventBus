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

    private static int type1 = 1;
    private static int type2 = type1 + 1;

    public void testRegister() throws InterruptedException {

        HandlerBus.register(type1, new OnBusCallBack() {
            @Override
            public void onBusCall(Event msg) {
                assertEquals(MESSAGE1, msg.data);
            }
        });
        HandlerBus.register(type2, new OnBusCallBack() {
            @Override
            public void onBusCall(Event msg) {
                assertEquals(MESSAGE2, msg.data);
            }
        });
        HandlerBus.post(createEvent(type1, MESSAGE1));
        HandlerBus.post(createEvent(type2, MESSAGE2));
        Thread.sleep(1000);
    }

    public void testSubThread() throws InterruptedException {
        HandlerBus.register(type1, new OnBusCallBack() {
            @Override
            public void onBusCall(Event msg) {
                assertTrue(Thread.currentThread() == Looper.getMainLooper().getThread());
            }
        });
        HandlerBus.register(type2, new OnBusCallBack() {
            @Override
            public void onBusCall(Event msg) {
                assertTrue(Thread.currentThread() != Looper.getMainLooper().getThread());
            }
        }, false); // 最后一个参数为false

        HandlerBus.post(createEvent(type1));
        HandlerBus.post(createEvent(type2));
        Thread.sleep(1000);
    }


    public void testUnregister() throws InterruptedException {
        OnBusCallBack callBack = new OnBusCallBack() {
            @Override
            public void onBusCall(Event msg) {
                fail();
            }
        };
        HandlerBus.register(type1, callBack);
        HandlerBus.unregister(type1, callBack);
        HandlerBus.post(createEvent(type1));
    }

    long postTime;

    public void testDelay() throws InterruptedException {
        HandlerBus.register(type1, new OnBusCallBack() {
            @Override
            public void onBusCall(Event msg) {
                if (System.currentTimeMillis() - postTime < 2000) {
                    fail();
                }
            }
        });
        HandlerBus.postDelay(createEvent(type1, "message1"), 2000);
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
        HandlerBus.register(type1, onBusCallBack);
        onBusCallBack = null; // 这里应该回收了
        System.gc();
        Thread.sleep(2000);

        HandlerBus.post(createEvent(type1, "message1"));
        Thread.sleep(1000);
    }


    private Event createEvent(int type) {
        return HandlerBus.createEvent(type);
    }

    private Event createEvent(int type, Object data) {
        return HandlerBus.createEvent(type, data);
    }
}
