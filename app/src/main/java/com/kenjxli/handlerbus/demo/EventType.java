package com.kenjxli.handlerbus.demo;

/**
 * Created by kenjxli on 2016/8/14.
 */
public class EventType {
    public static final int MAIN_ACTIVITY_EVENT = 1;
    public static final int FRAGMENT1_EVENT = MAIN_ACTIVITY_EVENT + 1;
    public static final int FRAGMENT2_EVENT = FRAGMENT1_EVENT + 1;
    public static final int DELAY_EVENT = FRAGMENT2_EVENT + 1;
    public static final int SUB_THREAD_EVENT = DELAY_EVENT + 1;
}
