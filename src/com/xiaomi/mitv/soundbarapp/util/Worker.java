package com.xiaomi.mitv.soundbarapp.util;

import android.os.Looper;

/**
 * Created by chenxuetong on 9/26/14.
 */
public class Worker implements Runnable {
    private final Object mLock = new Object();
    private Looper mLooper;

    /**
     * Creates a worker thread with the given name. The thread
     * then runs a {@link android.os.Looper}.
     * @param name A name for the new thread
     */
    public Worker(String name) {
        Thread t = new Thread(null, this, name);
        t.setPriority(Thread.MIN_PRIORITY);
        t.start();
        synchronized (mLock) {
            while (mLooper == null) {
                try {
                    mLock.wait();
                } catch (InterruptedException ex) {
                }
            }
        }
    }

    public Looper getLooper() {
        return mLooper;
    }

    public void run() {
        synchronized (mLock) {
            Looper.prepare();
            mLooper = Looper.myLooper();
            mLock.notifyAll();
        }
        Looper.loop();
    }

    public void quit() {
        mLooper.quit();
    }
}