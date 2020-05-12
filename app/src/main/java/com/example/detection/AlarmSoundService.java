package com.example.detection;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.widget.Toast;

import androidx.annotation.Nullable;

public class AlarmSoundService extends Service {

    MediaPlayer mMediaplayer;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        mMediaplayer = MediaPlayer.create(this, R.raw.alarmsound);
        mMediaplayer.setLooping(true);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Toast.makeText(this, "알람이 울립니다.", Toast.LENGTH_SHORT).show();
        mMediaplayer.start();

        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        mMediaplayer.stop();
        mMediaplayer.release();
        super.onDestroy();
    }
}