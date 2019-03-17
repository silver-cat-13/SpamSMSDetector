package com.personal.frbk1992.spamsmsdetector;

import android.os.Looper;

/**
 * Class use to check if I am in the main thread or not
 */
class ThreadPreconditions {
    static void checkOnMainThread() {
        if (Thread.currentThread() != Looper.getMainLooper().getThread()) {
            throw new IllegalStateException("This method should be called from the Main Thread");
        }
    }
}


