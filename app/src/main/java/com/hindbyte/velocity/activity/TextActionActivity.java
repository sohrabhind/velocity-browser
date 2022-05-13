package com.hindbyte.velocity.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

public class TextActionActivity extends AppCompatActivity {

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final CharSequence searchTextCharSequence = getIntent().getCharSequenceExtra(Intent.EXTRA_PROCESS_TEXT);
        assert searchTextCharSequence != null;
        final String searchText = searchTextCharSequence.toString();
        final Intent searchIntent = new Intent(this, BrowserActivity.class);
        searchIntent.setAction(Intent.ACTION_VIEW);
        searchIntent.setData(Uri.parse(searchText));
        startActivity(searchIntent);
        finish();
    }
}