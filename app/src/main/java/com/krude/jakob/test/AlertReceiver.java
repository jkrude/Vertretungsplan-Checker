package com.krude.jakob.test;


import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;


public class AlertReceiver extends BroadcastReceiver implements AsyncResponse{
    private final String TAG = "AlertReceiver";
    public static DownloadFile asyncTask =new DownloadFile();
    Context gl_context;
    public static PendingResult pendingResult;


    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "Alarm received");
        startJob(context.getApplicationContext());
        cancelAlarm(context.getApplicationContext());
        /*
        asyncTask.delegate = this;
        gl_context = context;
        Toast.makeText(context, "Received",Toast.LENGTH_SHORT).show();
        pendingResult = goAsync();
        startProcess();
        */
    }

    private void startJob(Context context){
        JobScheduler jobScheduler =
                (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        ComponentName componentName =   new ComponentName(context, PdfJobService.class);
        SharedPreferences prefs = context.getSharedPreferences(
                "com.krude.jakob.vertretungsplan", Context.MODE_PRIVATE);
        int PDF_JOB_ID = prefs.getInt("PDF_JOB_ID", 1);

        JobInfo jobInfo = new JobInfo.Builder(PDF_JOB_ID, componentName)
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                //.setPeriodic(60000,60000)
                //.setPeriodic(86400000 )
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




    private void startProcess(){
        String fileUrl ="https://www.graues-kloster.de/files/ovp_1.pdf";   // -> http://maven.apache.org/maven-1.x/maven.pdf
        String fileName = "ovp_1.pdf";  // -> maven.pdf

        File pdfFile = new File(MainActivity.directory, fileName);
        try {
            boolean createNewFileWorked = pdfFile.createNewFile();
            //TODO
            MainActivity.fileLocation = pdfFile.getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //FileDownloader.downloadFile("https://www.graues-kloster.de/files/ovp_1.pdf", pdfFile);
        asyncTask.execute(fileUrl, MainActivity.fileLocation);
        Toast.makeText(gl_context, "started Execute",Toast.LENGTH_SHORT).show();

    }

    public void showNotification(Context context, String body) {
         NotificationManager notificationManager= (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);

        Notification notification = new NotificationCompat.Builder(context, MainActivity.CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setContentTitle("TestTitle")
                .setContentText(body)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                .build();

        notificationManager.notify(1, notification);
    }

    private void writeToFile(String output){
        File lastSchedule = new File(MainActivity.directory, "lastSchedule");
        try {
            boolean createNewFileWorked = lastSchedule.createNewFile();
            //TODO
            MainActivity.fileLocation = lastSchedule.getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            FileOutputStream outputStream = new FileOutputStream(lastSchedule);
            outputStream.write(output.getBytes());
            outputStream.close();
            MainActivity.lastSchedule = lastSchedule.getAbsolutePath();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            MainActivity.lastSchedule = "FileNotFound";
        } catch (IOException e) {
            MainActivity.lastSchedule = "InputOutput";
            e.printStackTrace();
        }

    }

    @Override
    public void processFinished(String output, boolean success) {
        Toast.makeText(gl_context, "ProcessFinish",Toast.LENGTH_SHORT).show();

        String notification_message;
        if(output.equals("Class 11 is not affected.")){
            notification_message = "Vertretungsplan morgen betrifft dich nicht";
        }else{
            notification_message = "Im Vertretungsplan könnte was relevantes stehen";
        }
        showNotification(gl_context, notification_message);
        //writeToFile(output);

        SharedPreferences prefs = gl_context.getSharedPreferences(
                "com.krude.jakob.vertretungsplan", Context.MODE_PRIVATE);
        prefs.edit().putString("downloadedText",output).apply();
        MainActivity.downloadedText = output;

        gl_context = null;
    }
}
