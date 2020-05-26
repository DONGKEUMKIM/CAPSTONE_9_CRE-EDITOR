package com.example.detection;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

/**
 * splash activity
 */
public class SplashActivity extends Activity {

    SQLiteManager sqLiteManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //setContentView();
        sqLiteManager = SQLiteManager.getInstance(this);
        //sqLiteManager.init();

        Handler hd = new Handler();
        hd.postDelayed(new splashhandler(), 3000);
        /*
        try {
            Thread.sleep(3000); //milli-sec
            startActivity(new Intent(SplashActivity.this, MainActivity.class));
            SplashActivity.this.finish();
        } catch (Exception e) {

        }
        */
    }

    private class splashhandler implements Runnable{
        public void run(){
            Intent intent = new Intent(getApplication(),MainActivity.class);
            //intent.putExtra("Exercise", "레프트사이드라이즈");
            //intent.putExtra("Set", 1);
            //intent.putExtra("Number", 10);
            startActivity(intent); //로딩이 끝난후 이동할 Activity
            SplashActivity.this.finish();   //로딩페이지 Activity Stack에서 제거
        }
    }
}