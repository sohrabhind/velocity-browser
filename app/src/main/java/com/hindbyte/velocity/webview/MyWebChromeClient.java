package com.hindbyte.velocity.webview;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.webkit.GeolocationPermissions;
import android.webkit.PermissionRequest;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;

import com.hindbyte.velocity.activity.BrowserActivity;
import com.hindbyte.velocity.fragment.BrowserInterface;

public class MyWebChromeClient extends WebChromeClient {

    private final MyWebView myWebView;
    private final Activity context;
    BrowserInterface browserInterface;

    MyWebChromeClient(MyWebView myWebView, Activity context, BrowserInterface browserInterface) {
        this.myWebView = myWebView;
        this.context = context;
        this.browserInterface = browserInterface;
    }

    @Override
    public void onShowCustomView(View view, WebChromeClient.CustomViewCallback callback) {
        ((BrowserActivity) context).onShowCustomView(view, callback);
    }

    @Override
    public void onHideCustomView() {
        ((BrowserActivity) context).onHideCustomView();
    }

    @Override
    public void onCloseWindow(WebView view) {
    }

    @Override
    public void onProgressChanged(WebView view, int progress) {
        if (myWebView.foreground) {
            ((BrowserActivity) context).updateProgress(progress);
        }
    }

    @Override
    public Bitmap getDefaultVideoPoster() {
        return ((BrowserActivity) context).getDefaultVideoPoster();
    }

    @Override
    public void onReceivedTitle(WebView view, String title) {
        myWebView.updateTitle(title);
        myWebView.updateUrl(view.getUrl());
    }

    @Override
    public boolean onCreateWindow(WebView view, boolean isDialog, boolean isUserGesture, Message resultMsg) {
        MyWebView webView = new MyWebView(context, browserInterface);
        MyWebView.WebViewTransport transport = (MyWebView.WebViewTransport) resultMsg.obj;
        transport.setWebView(webView);
        ((BrowserActivity) context).addTab(null, true, webView);
        resultMsg.sendToTarget();
        return true;
    }

    @Override
    public void onPermissionRequest(final PermissionRequest request) {
        int MY_CAMERA_REQUEST_CODE = 111;
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(context, new String[]{Manifest.permission.CAMERA}, MY_CAMERA_REQUEST_CODE);
        }
        int MY_RECORD_AUDIO_REQUEST_CODE = 222;
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(context, new String[]{Manifest.permission.RECORD_AUDIO}, MY_RECORD_AUDIO_REQUEST_CODE);
        }
    }

    @Override
    public boolean onShowFileChooser(WebView view, ValueCallback<Uri[]> filePathCallback, WebChromeClient.FileChooserParams fileChooserParams) {
        ((BrowserActivity) context).showFileChooser(filePathCallback);
        return true;
    }

    @Override
    public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(context, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 0);
        }
        new AlertDialog.Builder(context)
                .setCancelable(false)
                .setMessage(origin + " Would like to use your Current Location")
                .setNegativeButton("Don't Allow", (arg0, arg1) -> {
                    callback.invoke(origin, false, true);
                })
                .setPositiveButton("Allow", (arg0, arg1) -> {
                    callback.invoke(origin, true, true);
                })
                .create().show();
    }
}
