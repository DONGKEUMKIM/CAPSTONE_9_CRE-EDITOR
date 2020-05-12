package com.example.detection;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

/**
 * splash activity
 */
public class SplashActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            Thread.sleep(3000); //milli-sec
            startActivity(new Intent(SplashActivity.this, MainActivity.class));
            SplashActivity.this.finish();
        } catch (Exception e) {

        }
    }
}