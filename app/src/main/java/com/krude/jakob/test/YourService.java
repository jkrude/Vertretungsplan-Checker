package com.krude.jakob.test;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class YourService extends Service
{

    public void onCreate()
    {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        //  MainActivity.AlarmGetInstance().setAlarm(this);
        return START_STICKY;
    }

    @Override
    public void onStart(Intent intent, int startId)
    {
        //MainActivity.AlarmGetInstance().setAlarm(this);
    }

    @Override
    public IBinder onBind(Intent intent)
    {
        return null;
    }
}
