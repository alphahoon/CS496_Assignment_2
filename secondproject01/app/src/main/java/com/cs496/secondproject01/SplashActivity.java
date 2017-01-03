package com.cs496.secondproject01;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class SplashActivity extends AppCompatActivity {
    private static int SPLASH_TIME_OUT = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //TextView msg = (TextView) findViewById(R.id.splash_msg);
        //Typeface typeFace= Typeface.createFromAsset(getAssets(), "fonts/pen.ttf");
        //msg.setTypeface(typeFace);
        setContentView(R.layout.activity_splash);
        TextView txt = (TextView) findViewById(R.id.splash_msg);
        //Typeface font = Typeface.createFromAsset(getAssets(), "fonts/pen.ttf");
        txt.setTypeface(App.myFont);


        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent mainIntent = new Intent(SplashActivity.this, MainActivity.class);
                startActivity(mainIntent);
                finish();
            }
        },SPLASH_TIME_OUT);
    }
}
