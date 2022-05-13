package com.hindbyte.velocity.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

import com.hindbyte.velocity.R;

import static com.hindbyte.velocity.util.Variables.firstStart;

public class SplashActivity extends AppCompatActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        Intent mainIntent = new Intent(SplashActivity.this, BrowserActivity.class);
        if (firstStart) {
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                startActivity(mainIntent);
                finish();
            }, 1000);
        } else {
            startActivity(mainIntent);
            finish();
        }
    }
}