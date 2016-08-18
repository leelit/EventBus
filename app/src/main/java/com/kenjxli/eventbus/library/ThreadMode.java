package com.kenjxli.eventbus.library;

/**
 * Created by kenjxli on 2016/8/18.
 */
public enum ThreadMode {
    POST_THREAD,      // 发送事件的线程
    MAIN_THREAD,  // 主线程
    SUB_THREAD    // 子线程，缓存线程池
}

