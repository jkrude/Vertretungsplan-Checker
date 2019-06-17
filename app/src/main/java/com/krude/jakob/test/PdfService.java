package com.krude.jakob.test;

import android.app.Service;

import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

public class PdfService extends Service {
    private final IBinder mBinder = new MyBinder();
    private int counter = 1;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //TODO *function to call*
        return Service.START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        //TODO *the function to call*
        return mBinder;
    }

    public class MyBinder extends Binder {
        PdfService getService() {
            return PdfService.this;
        }
    }

}
