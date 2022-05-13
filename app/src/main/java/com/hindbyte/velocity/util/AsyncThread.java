package com.hindbyte.velocity.util;

import android.os.Handler;
import android.os.Looper;

public abstract class AsyncThread extends Thread {

    private Object[] params;
    private final Handler mMainHandler;

    public AsyncThread() {
        mMainHandler = new Handler(Looper.getMainLooper());
    }

    abstract protected void onPreExecute();

    abstract protected Object doInBackground(Object... params);

    abstract protected void onPostExecute(Object result);

    public void execute(Object... params) {
        this.params = params;
        start();
    }

    public void run() {
        mMainHandler.post(this::onPreExecute);
        Object result = null;
        try {
            result = doInBackground(params);
        } finally {
            final Object finalResult = result;
            mMainHandler.post(() -> onPostExecute(finalResult));
        }
    }
}