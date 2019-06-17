package com.krude.jakob.test;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class PdfServiceReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Util.scheduleJob(context);
    }
}