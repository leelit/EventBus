package com.kenjxli.eventbus.library;

/**
 * Created by kenjxli on 2016/8/12.
 */
public class Event {
    public int type;
    int stickyMs; // 延迟时间，包可见
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


    public static class StickyEvent {
        Event event;
        long postTime;

        public StickyEvent(Event event, long postTime) {
            this.event = event;
            this.postTime = postTime;
        }
    }
}

