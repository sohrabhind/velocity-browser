package com.hindbyte.velocity.webview;

import android.view.GestureDetector;
import android.view.MotionEvent;

public class GestureListener extends GestureDetector.SimpleOnGestureListener {

    private final MyWebView webView;

    GestureListener(MyWebView webView) {
        super();
        this.webView = webView;
    }

    @Override
    public void onLongPress(MotionEvent e) {
        webView.onLongPress();
    }

}
