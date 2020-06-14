package com.example.detection;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;

import androidx.annotation.IdRes;
import androidx.appcompat.app.AppCompatActivity;

public class SettingActivity extends AppCompatActivity {
    IntentFilter intentFilter;
    AlarmReceiver alarmReceiver;
    AlarmSoundService mAlarmsoundservice;
    String ALARMSTART = "ALARMSTART";
    String ALARMEND = "ALARMEND";

    private Button btn;
    private RadioButton r_btn1, r_btn2, r_btn3;
    private RadioGroup radioGroup;

    MediaPlayer mMediaplayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.schedule_edit_popup);
        setContentView(R.layout.setting_main);
        alarmReceiver = new AlarmReceiver();
        intentFilter = new IntentFilter();
        mAlarmsoundservice = new AlarmSoundService();
        intentFilter.addAction(ALARMSTART);
        intentFilter.addAction(ALARMEND);

        r_btn1 = (RadioButton) findViewById(R.id.rg_btn1);
        r_btn2 = (RadioButton) findViewById(R.id.rg_btn2);
        r_btn3 = (RadioButton) findViewById(R.id.rg_btn3);
        r_btn1.setOnClickListener(radioButtonClickListener);
        r_btn2.setOnClickListener(radioButtonClickListener);
        r_btn3.setOnClickListener(radioButtonClickListener);

        radioGroup = (RadioGroup) findViewById(R.id.radioGroup);
        radioGroup.setOnCheckedChangeListener(radioGroupButtonChangeListener);

    }

    RadioButton.OnClickListener radioButtonClickListener = view -> {
    };
    RadioGroup.OnCheckedChangeListener radioGroupButtonChangeListener = (radioGroup, i) -> {
        if(i == R.id.rg_btn1){
            registerReceiver(alarmReceiver, intentFilter);
            Intent sendIntent1 = new Intent(ALARMEND);
            sendBroadcast(sendIntent1);
            TestDetection.alarmType = 1;
            Toast myToast = Toast.makeText(getApplicationContext(),"알람1로 설정", Toast.LENGTH_SHORT);
            myToast.show();
            registerReceiver(alarmReceiver, intentFilter);
            Intent sendIntent = new Intent(ALARMSTART);
            sendBroadcast(sendIntent);
        }
        else if(i == R.id.rg_btn2){
            registerReceiver(alarmReceiver, intentFilter);
            Intent sendIntent1 = new Intent(ALARMEND);
            sendBroadcast(sendIntent1);
            TestDetection.alarmType = 2;
            Toast myToast = Toast.makeText(getApplicationContext(),"알람2로 설정", Toast.LENGTH_SHORT);
            myToast.show();
            registerReceiver(alarmReceiver, intentFilter);
            Intent sendIntent = new Intent(ALARMSTART);
            sendBroadcast(sendIntent);
        }
        else if(i == R.id.rg_btn3){
            registerReceiver(alarmReceiver, intentFilter);
            Intent sendIntent1 = new Intent(ALARMEND);
            sendBroadcast(sendIntent1);
            TestDetection.alarmType = 3;
            Toast myToast = Toast.makeText(getApplicationContext(),"알람3으로 설정", Toast.LENGTH_SHORT);
            myToast.show();
            registerReceiver(alarmReceiver, intentFilter);
            Intent sendIntent = new Intent(ALARMSTART);
            sendBroadcast(sendIntent);
        }else if(i==R.id.rg_btn4){
            registerReceiver(alarmReceiver, intentFilter);
            Intent sendIntent1 = new Intent(ALARMEND);
            sendBroadcast(sendIntent1);
            TestDetection.alarmType = 4;
            Toast myToast = Toast.makeText(getApplicationContext(),"무음으로 설정", Toast.LENGTH_SHORT);
            myToast.show();
            registerReceiver(alarmReceiver, intentFilter);
            Intent sendIntent = new Intent(ALARMSTART);
            sendBroadcast(sendIntent);
        }
    };

    public class AlarmReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String act = intent.getAction();
            if(act.equals(ALARMSTART))
            {
                System.out.println("방송을 받았습니다.");
                Intent mServiceintent = new Intent (context, AlarmSoundService.class);
                //알람 시작 방송을 받았을때
                context.startService(mServiceintent);
            }
            else if(act.equals(ALARMEND))
            {
                //알람 종료 방송을 받았을때
                System.out.println("방송을 받았습니다.");
                Intent mServiceintent = new Intent (context, AlarmSoundService.class);
                context.stopService(mServiceintent);

                unregisterReceiver(alarmReceiver);
            }
            //unregisterReceiver(alarmReceiver);
        }
    }

}
