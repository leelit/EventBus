package com.kenjxli.handlerbus.library;

/**
 * Created by kenjxli on 2016/8/12.
 */
public class Event {
    public int type;
    public Object data;

    Event() {

    }

    @Override
    public String toString() {
        return "Event{" +
                "type=" + type +
                ", data=" + data +
                '}';
    }
}
