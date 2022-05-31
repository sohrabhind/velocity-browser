package com.hindbyte.velocity.webview;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.preference.PreferenceManager;
import android.view.ContextMenu;
import android.view.GestureDetector;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.WebSettings;
import android.webkit.WebView;

import androidx.annotation.NonNull;

import com.hindbyte.velocity.R;
import com.hindbyte.velocity.activity.BrowserActivity;
import com.hindbyte.velocity.fragment.BrowserInterface;
import com.hindbyte.velocity.tabs.TabLayout;
import com.hindbyte.velocity.tabs.TabManager;
import com.hindbyte.velocity.util.URLUtils;

import java.net.URISyntaxException;

public class MyWebView extends WebView implements TabManager {

    Activity context;
    final TabLayout tabLayout;
    final MyWebViewClient webViewClient;
    final MyWebChromeClient webChromeClient;
    final GestureDetector gestureDetector;
    final MyDownloadListener downloadListener;
    WebSettings webSettings;
    SharedPreferences sp;
    final SharedPreferences pref;
    CookieManager cookieManager;

    boolean foreground;
    String mobileUserAgent;
    BrowserInterface browserInterface;

    public MyWebView(Activity context, BrowserInterface browserInterface) {
        super(context);
        this.context = context;
        this.browserInterface = browserInterface;
        this.foreground = false;

        this.tabLayout = new TabLayout(context, this);
        initTab();
        this.webViewClient = new MyWebViewClient(this, context);
        this.webChromeClient = new MyWebChromeClient(this, context, browserInterface);
        this.downloadListener = new MyDownloadListener(context, browserInterface);
        this.gestureDetector = new GestureDetector(context, new GestureListener(this));

        sp = PreferenceManager.getDefaultSharedPreferences(context);
        pref = context.getSharedPreferences("myPref", 0);

        cookieManager = CookieManager.getInstance();
        cookieManager.setAcceptCookie(true);
        initWebView();
        initWebSettings();
    }


    private synchronized void initWebView() {
        setHorizontalScrollBarEnabled(false);
        setVerticalScrollBarEnabled(false);
        setScrollbarFadingEnabled(true);
        setClickable(true);

        setWebViewClient(webViewClient);
        setWebChromeClient(webChromeClient);
        setDownloadListener(downloadListener);
        setOnTouchListener((view, arg) -> {
            performClick();
            gestureDetector.onTouchEvent(arg);
            return false;
        });
    }



    public synchronized void initWebSettings() {
        webSettings = getSettings();
        mobileUserAgent = webSettings.getUserAgentString();
        webSettings.setSaveFormData(true);

        webSettings.setDatabaseEnabled(true);
        webSettings.setDomStorageEnabled(true);

        webSettings.setAllowFileAccessFromFileURLs(true);
        webSettings.setAllowUniversalAccessFromFileURLs(true);
        webSettings.setCacheMode(WebSettings.LOAD_DEFAULT);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setUseWideViewPort(true);
        webSettings.setSupportMultipleWindows(true);
        webSettings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        webSettings.setAllowContentAccess(true);
        webSettings.setAllowFileAccess(true);

        webSettings.setGeolocationEnabled(true);
        webSettings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NORMAL);
        webSettings.setSupportZoom(true);
        webSettings.setBuiltInZoomControls(true);
        webSettings.setDisplayZoomControls(false);
    }

    public synchronized void initPreferences() {
        webSettings.setLoadsImagesAutomatically(sp.getBoolean(context.getString(R.string.sp_images), true));
        webSettings.setJavaScriptEnabled(sp.getBoolean(context.getString(R.string.sp_javascript), true));
        webSettings.setJavaScriptCanOpenWindowsAutomatically(sp.getBoolean(context.getString(R.string.sp_javascript), true));
        if (pref.getBoolean("key_desktop_mode", false)) {
            webSettings.setUserAgentString("Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:80.0) Gecko/9999999 Chrome/999 Firefox/999 Safari/999");
        } else {
            webSettings.setUserAgentString(mobileUserAgent);
        }
    }

    @Override
    public synchronized void loadUrl(@NonNull String url) {
        URLUtils urlUtils = new URLUtils();
        url = urlUtils.queryWrapper(context, url.trim());
        if (urlUtils.INTENT_SCHEMA.matcher(url).matches()) {
            try {
                Intent intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME);
                intent.addCategory(Intent.CATEGORY_BROWSABLE);
                intent.setComponent(null);
                intent.setSelector(null);
                try {
                    Activity host = (Activity) getContext();
                    host.startActivity(intent);
                } catch (ActivityNotFoundException e) {
                    updateUrl(url);
                    super.loadUrl(url);
                }
            } catch (URISyntaxException e) {
                updateUrl(url);
                super.loadUrl(url);
            }
        } else if (urlUtils.RegexURL.matcher(url).matches()) {
            updateUrl(url);
            super.loadUrl(urlUtils.AffiliateUrl(url));
        } else {
            updateUrl(url);
            super.loadUrl(url);
        }
    }

    private synchronized void initTab() {
        tabLayout.setTabTitle("Home");
        tabLayout.setUrl("");
    }

    @Override
    public View getTabView() {
        return tabLayout.getTabView();
    }

    public synchronized void updateTitle(String title) {
        if (title != null) {
            if (title.equals("about:blank")) {
                tabLayout.setTabTitle("Home");
            } else {
                tabLayout.setTabTitle(title);
            }
        }
    }

    public synchronized void updateUrl(String url) {
        if(url != null) {
            if(url.equals("about:blank")) {
                tabLayout.setUrl("");
            } else {
                tabLayout.setUrl(url);
            }
            if (foreground) {
                ((BrowserActivity) context).updateInputBox(url);
            }
        }
    }


    private final Handler clickHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            ((BrowserActivity) context).onLongPress(msg.getData().getString("url"));
        }
    };

    public void onLongPress() {
        Message click = clickHandler.obtainMessage();
        click.setTarget(clickHandler);
        requestFocusNodeHref(click);
    }

    @Override
    public synchronized void activate() {
        foreground = true;
        requestFocus();
        initPreferences();
        tabLayout.activate();
        onResume();
    }

    @Override
    public synchronized void deactivate() {
        foreground = false;
        clearFocus();
        pauseTimers();
        resumeTimers();
        onPause();
        tabLayout.deactivate();
    }
}
