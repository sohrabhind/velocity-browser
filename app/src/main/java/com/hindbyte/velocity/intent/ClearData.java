package com.hindbyte.velocity.intent;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.webkit.CookieManager;
import android.webkit.WebStorage;
import android.webkit.WebViewDatabase;

import com.hindbyte.velocity.activity.BrowserActivity;
import com.hindbyte.velocity.fragment.BrowserInterface;
import com.hindbyte.velocity.history.HistoryData;
import com.hindbyte.velocity.webview.MyWebView;

public class ClearData {

    private final Activity context;

    public ClearData(Activity context) {
      this.context = context;
    }

    public void clearCaches() {
        clearCookie();
        HistoryData db = new HistoryData(context);
        db.deleteData();
        WebStorage.getInstance().deleteAllData();
        WebViewDatabase.getInstance(context).clearFormData();
        WebViewDatabase.getInstance(context).clearHttpAuthUsernamePassword();
    }

    private void clearCookie() {
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.flush();
        cookieManager.removeAllCookies(value -> {
        });
    }

    private void deleteAppData() {
        try {
            String packageName = context.getPackageName();
            Runtime runtime = Runtime.getRuntime();
            runtime.exec("pm clear "+ packageName);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
