package com.krude.jakob.main;


import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;


public class AlertReceiver extends BroadcastReceiver{
    private final String TAG = "AlertReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "Alarm received");
        startJob(context.getApplicationContext());
        cancelAlarm(context.getApplicationContext());
    }

    private void startJob(Context context){
        JobScheduler jobScheduler =
                (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        ComponentName componentName = new ComponentName(context, PdfJobService.class);
        SharedPreferences prefs = context.getSharedPreferences(
                "com.krude.jakob.vertretungsplan", Context.MODE_PRIVATE);
        int PDF_JOB_ID = prefs.getInt("PDF_JOB_ID", 1);

        JobInfo jobInfo = new JobInfo.Builder(PDF_JOB_ID, componentName)
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                .setPeriodic(86400000 )
                .setPersisted(true)
                .build();
        jobScheduler.schedule(jobInfo);
        Log.d(TAG, "scheduled Job");
    }

    public void cancelAlarm(Context context){
        boolean alarmUp = (PendingIntent.getBroadcast(context, 1,
                new Intent(context,AlertReceiver.class),
                PendingIntent.FLAG_NO_CREATE) != null);
        if(!alarmUp){
            return;
        }
        Log.d(TAG, "Alarm has to be canceled");
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context,AlertReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 1, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        alarmManager.cancel(pendingIntent);
        pendingIntent.cancel();
        //unregisterReceiver();

        Intent intent1 = new Intent(context,AlertReceiver.class);
        alarmUp = (PendingIntent.getBroadcast(context, 1, intent1, PendingIntent.FLAG_NO_CREATE) != null);
        String out = "alarm is " + (alarmUp ? "not" : "") + " canceled.";
        Log.d(TAG, out);

    }
}
