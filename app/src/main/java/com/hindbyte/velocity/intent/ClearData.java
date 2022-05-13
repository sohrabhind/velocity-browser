package com.hindbyte.velocity.intent;

import android.content.Context;
import android.os.Build;
import android.webkit.CookieManager;
import android.webkit.WebStorage;
import android.webkit.WebViewDatabase;

import com.hindbyte.velocity.activity.BrowserActivity;
import com.hindbyte.velocity.history.HistoryData;
import com.hindbyte.velocity.webview.MyWebView;

public class ClearData {

    private final Context context;

    public ClearData(Context context) {
      this.context = context;
    }

    public void clearCaches() {
        clearCookie();
        HistoryData db = new HistoryData(context);
        db.deleteData();
        MyWebView mm = new MyWebView(context);
        mm.clearSslPreferences();
        mm.clearCache(true);
        WebStorage.getInstance().deleteAllData();
        WebViewDatabase.getInstance(context).clearFormData();
        WebViewDatabase.getInstance(context).clearHttpAuthUsernamePassword();
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR2) {
            WebViewDatabase.getInstance(context).clearUsernamePassword();
        }
    }

    private void clearCookie() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            CookieManager cookieManager = CookieManager.getInstance();
            cookieManager.flush();
            cookieManager.removeAllCookies(value -> {
            });
        } else {
            CookieManager cookieManager = CookieManager.getInstance();
            cookieManager.removeAllCookie();
        }
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