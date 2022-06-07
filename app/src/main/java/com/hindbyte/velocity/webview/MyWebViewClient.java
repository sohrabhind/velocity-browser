package com.hindbyte.velocity.webview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Message;
import android.text.method.PasswordTransformationMethod;
import android.view.LayoutInflater;
import android.webkit.HttpAuthHandler;
import android.webkit.SslErrorHandler;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.webkit.WebViewAssetLoader;

import com.hindbyte.velocity.R;
import com.hindbyte.velocity.history.HistoryData;
import com.hindbyte.velocity.util.URLUtils;

public class MyWebViewClient extends WebViewClient {

    private final MyWebView myWebView;
    private final Context context;
    private final SharedPreferences pref;
    private final HistoryData db;
    WebViewAssetLoader assetLoader;

    MyWebViewClient(MyWebView myWebView, Context context) {
        this.myWebView = myWebView;
        this.context = context;
        this.db = new HistoryData(context);
        pref = context.getSharedPreferences("myPref", 0);
        assetLoader = new WebViewAssetLoader.Builder()
                .addPathHandler("/assets/", new WebViewAssetLoader.AssetsPathHandler(context))
                .addPathHandler("/res/", new WebViewAssetLoader.ResourcesPathHandler(context))
                .build();
    }

    @Override
    public void onPageStarted(WebView view, String url, Bitmap favicon) {
        myWebView.updateTitle(view.getTitle());
        myWebView.updateUrl(url);
    }

    @Override
    public void doUpdateVisitedHistory(WebView view, String url, boolean isReload) {
        if (!pref.getBoolean("key_private_mode", false) && !isReload && url != null && !url.equals("about:blank") && view.getTitle()!=null ) {
            db.addItem(view.getTitle(), url);
        }
    }

    @Override
    public void onPageFinished(WebView view, String url) {
        myWebView.updateTitle(view.getTitle());
        myWebView.updateUrl(url);
    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        URLUtils urlUtils = new URLUtils();
        if (urlUtils.INTENT_SCHEMA.matcher(url).matches()) {
            try {
                Intent intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME);
                context.startActivity(intent);
                return true;
            } catch (Exception e) {
                myWebView.updateUrl(url);
                return false;
            }
        } else if (urlUtils.RegexURL.matcher(url).matches()) {
            myWebView.loadUrl(url);
            return true;
        } else {
            myWebView.updateUrl(url);
            return false;
        }
    }

    @Override
    public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
        return assetLoader.shouldInterceptRequest(Uri.parse(url));
    }

    @Override
    public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
        return super.shouldInterceptRequest(view, request);
    }

    @Override
    public void onFormResubmission(WebView view, @NonNull final Message dontResend, final Message resend) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setCancelable(false);
        builder.setTitle(R.string.dialog_title_resubmission);
        builder.setMessage(R.string.dialog_content_resubmission);
        builder.setPositiveButton(R.string.dialog_button_positive, (dialog, which) -> resend.sendToTarget());
        builder.setNegativeButton(R.string.dialog_button_negative, (dialog, which) -> dontResend.sendToTarget());
        builder.create().show();
    }


    @Override
    public void onReceivedHttpAuthRequest(WebView view, @NonNull final HttpAuthHandler handler, String host, String realm) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setCancelable(false);
        builder.setTitle("Sign in");

        @SuppressLint("InflateParams") LinearLayout signInLayout = (LinearLayout) LayoutInflater.from(context).inflate(R.layout.dialog_sign_in, null, false);
        final EditText userEdit = signInLayout.findViewById(R.id.dialog_sign_in_username);
        final EditText passEdit = signInLayout.findViewById(R.id.dialog_sign_in_password);
        passEdit.setTypeface(Typeface.DEFAULT);
        passEdit.setTransformationMethod(new PasswordTransformationMethod());
        builder.setView(signInLayout);

        builder.setPositiveButton(R.string.dialog_button_positive, (dialog, which) -> {
            String user = userEdit.getText().toString().trim();
            String pass = passEdit.getText().toString().trim();
            handler.proceed(user, pass);
        });

        builder.setNegativeButton(R.string.dialog_button_negative, (dialog, which) -> handler.cancel());
        builder.create().show();
    }

    @SuppressLint("WebViewClientOnReceivedSslError")
    @Override
    public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
        new AlertDialog.Builder(context)
                .setCancelable(false)
                .setMessage("This site is using an invalid security certificate.\nDo you want to proceed anyway?")
                .setNegativeButton("Cancel", (arg0, arg1) -> handler.cancel())
                .setPositiveButton("Proceed", (arg0, arg1) -> handler.proceed())
                .create().show();
    }
}