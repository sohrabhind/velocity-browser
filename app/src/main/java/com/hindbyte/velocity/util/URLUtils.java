package com.hindbyte.velocity.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.util.Patterns;

import com.hindbyte.velocity.R;

import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class URLUtils {

    private final String URL_SCHEME_HTTP = "http://";
    private final Pattern URI_SCHEMA = Pattern.compile("^(http|https|file|market|intent|inline|data|about|content|javascript|mailto|view-source|tel|magnet|geo):(.*)", Pattern.CASE_INSENSITIVE);
    public final Pattern INTENT_SCHEMA = Pattern.compile("^(market|intent|mailto|tel|magnet|geo):(.*)", Pattern.CASE_INSENSITIVE);
    public final Pattern RegexURL = Pattern.compile("^(?:https?://)?(?:www\\.)?(amazon|flipkart)(\\..*)", Pattern.CASE_INSENSITIVE);

    private boolean isURL(String url) {
        url = url.trim();
        boolean hasSpace = (url.indexOf(' ') != -1);
        Matcher matcher = URI_SCHEMA.matcher(url);
        return !hasSpace && matcher.matches() || Patterns.WEB_URL.matcher(url).matches();
    }

    public String queryWrapper(Context context, String query) {
        if (isURL(query)) {
            if (!URI_SCHEMA.matcher(query).matches()) {
                query = URL_SCHEME_HTTP + query;
            }
            return query;
        } else {
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
            final int i = Integer.valueOf(Objects.requireNonNull(sp.getString(context.getString(R.string.sp_search_engine), "0")));
            switch (i) {
                case 1:
                    return "https://www.yandex.com/search/touch/?text=" + query;
                case 2:
                    return "https://www.bing.com/search?q=" + query;
                case 3:
                    return "https://duckduckgo.com/?q=" + query;
                case 4:
                    return "https://search.yahoo.com/search?p=" + query;
                default:
                    return "https://www.google.com/m?q=" + query;
            }
        }
    }

    public  String urlWrapper(String url) {
        String green500 = "<font color='#199317'>{content}</font>";
        String gray500 = "<font color='#B8B8B8'>{content}</font>";
        String URL_SCHEME_HTTPS = "https://";
        if (url.startsWith(URL_SCHEME_HTTPS)) {
            String scheme = green500.replace("{content}", URL_SCHEME_HTTPS);
            url = scheme + url.substring(8);
        } else if (url.startsWith(URL_SCHEME_HTTP)) {
            String scheme = gray500.replace("{content}", URL_SCHEME_HTTP);
            url = scheme + url.substring(7);
        }
        return url;
    }

    public String AffiliateUrl(String url) {
        if (url.contains("amazon.com")) {
            return replaceUriParameter(url, "tag", "20190101-20");
        } else if (url.contains("amazon.in")){
            return replaceUriParameter(url, "tag", "20180101-21");
        } else if (url.contains("amazon.co.uk")) {
            return replaceUriParameter(url, "tag", "2018010104-21");
        } else if (url.contains("amazon.it")) {
            return replaceUriParameter(url, "tag", "2018010101-21");
        } else if (url.contains("amazon.de")) {
            return replaceUriParameter(url, "tag", "2018010107-21");
        } else if (url.contains("flipkart.com")) {
            return replaceUriParameter(url, "affid", "20180101");
        } else {
            return url;
        }
    }

    private String replaceUriParameter(String url, String key, String value) {
        Uri uri = Uri.parse(url);
        final Set<String> params = uri.getQueryParameterNames();
        final Uri.Builder builder = uri.buildUpon().clearQuery();
        for (String param : params) {
            if (!param.equals(key)) {
                builder.appendQueryParameter(param, uri.getQueryParameter(param));
            }
        }
        builder.appendQueryParameter(key, value);
        return builder.build().toString();
    }
}
