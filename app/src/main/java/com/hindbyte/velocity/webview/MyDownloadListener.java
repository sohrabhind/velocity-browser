package com.hindbyte.velocity.webview;

import static android.content.Context.DOWNLOAD_SERVICE;

import android.Manifest;
import android.app.Activity;
import android.app.DownloadManager;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Environment;
import android.webkit.CookieManager;
import android.webkit.MimeTypeMap;
import android.webkit.URLUtil;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;

import com.hindbyte.velocity.R;
import com.hindbyte.velocity.fragment.BrowserInterface;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MyDownloadListener implements android.webkit.DownloadListener {

    private final Activity context;
    BrowserInterface browserInterface;

    public MyDownloadListener(Activity context, BrowserInterface browserInterface) {
        super();
        this.context = context;
        this.browserInterface = browserInterface;
    }

    @Override
    public void onDownloadStart(final String url, final String userAgent, final String contentDisposition, final String mimeType, long contentLength) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setCancelable(false);
        builder.setTitle(R.string.dialog_title_download);
        builder.setPositiveButton(R.string.dialog_button_positive, (dialog, which) -> {
            download(url, contentDisposition, mimeType);
        });
        builder.setNegativeButton(R.string.dialog_button_negative, null);
        builder.create().show();
    }


    public void download(String url, String contentDisposition, String mimeType) {
        String permission = Manifest.permission.WRITE_EXTERNAL_STORAGE;
        if(ActivityCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED) {
            try {
                DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
                request.setMimeType(mimeType);
                String cookies = CookieManager.getInstance().getCookie(url);
                request.addRequestHeader("cookie", cookies);
                request.setDescription("Downloading file...");
                request.setTitle(guessFileName(url, contentDisposition, mimeType));
                request.allowScanningByMediaScanner();
                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, URLUtil.guessFileName(url, contentDisposition, mimeType));
                DownloadManager dm = (DownloadManager) context.getSystemService(DOWNLOAD_SERVICE);
                dm.enqueue(request);
                Toast.makeText(context, "Downloading File", Toast.LENGTH_LONG).show();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            browserInterface.setStrings(url, contentDisposition, mimeType);
            final int WRITE_EXTERNAL_STORAGE_TASK_CODE = 777;
            String[] permissionsToRequest = new String[] { Manifest.permission.WRITE_EXTERNAL_STORAGE };
            ActivityCompat.requestPermissions(context, permissionsToRequest, WRITE_EXTERNAL_STORAGE_TASK_CODE);
        }
    }

    private String guessFileName(String url, String contentDisposition, String mimeType) {
        String filename = null;
        String extension = null;
        if (contentDisposition != null) {
            filename = parseContentDisposition(contentDisposition);
            if (filename != null) {
                int index = filename.lastIndexOf('/') + 1;
                if (index > 0) {
                    filename = filename.substring(index);
                }
            }
        }
        if (filename == null) {
            String decodedUrl = Uri.decode(url);
            if (decodedUrl != null) {
                int queryIndex = decodedUrl.indexOf('?');
                if (queryIndex > 0) {
                    decodedUrl = decodedUrl.substring(0, queryIndex);
                }
                if (!decodedUrl.endsWith("/")) {
                    int index = decodedUrl.lastIndexOf('/') + 1;
                    if (index > 0) {
                        filename = decodedUrl.substring(index);
                    }
                }
            }
        }

        if (filename == null) {
            filename = "download file";
        }

        int dotIndex = filename.indexOf('.');
        if (dotIndex < 0) {
            if (mimeType != null) {
                extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType);
                if (extension != null) {
                    extension = "." + extension;
                }
            }
            if (extension == null) {
                if (mimeType != null && mimeType.toLowerCase(Locale.ROOT).startsWith("text/")) {
                    if (mimeType.equalsIgnoreCase("text/html")) {
                        extension = ".html";
                    } else {
                        extension = ".txt";
                    }
                } else if (mimeType != null && mimeType.equalsIgnoreCase("image/*")) {
                    extension = ".jpeg";
                } else {
                    if (url.contains("mp4") || url.contains("3gp") || url.contains("webm")) {
                        extension = ".mp4";
                    } else if (url.contains("mp3")){
                        extension = ".mp3";
                    } else {
                        extension = ".bin";
                    }
                }
            }
        } else {
            if (mimeType != null) {
                int lastDotIndex = filename.lastIndexOf('.');
                String typeFromExt = MimeTypeMap.getSingleton().getMimeTypeFromExtension(
                        filename.substring(lastDotIndex + 1));
                if (typeFromExt != null && !typeFromExt.equalsIgnoreCase(mimeType)) {
                    extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType);
                    if (extension != null) {
                        extension = "." + extension;
                    }
                }
            }
            if (extension == null) {
                extension = filename.substring(dotIndex);
            }
            filename = filename.substring(0, dotIndex);
        }
        return filename + extension;
    }

    private final Pattern CONTENT_DISPOSITION_PATTERN = Pattern.compile("attachment;\\s*filename\\s*=\\s*(\"?)([^\"]*)\\1\\s*$", Pattern.CASE_INSENSITIVE);

    private String parseContentDisposition(String contentDisposition) {
        try {
            Matcher m = CONTENT_DISPOSITION_PATTERN.matcher(contentDisposition);
            if (m.find()) {
                return m.group(2);
            }
        } catch (IllegalStateException ignored) {
        }
        return null;
    }
}