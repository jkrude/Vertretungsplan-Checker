package com.krude.jakob.test;


import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;


public class AlertReceiver extends BroadcastReceiver implements AsyncResponse{
    public static DownloadFile asyncTask =new DownloadFile();
    Context gl_context;


    @Override
    public void onReceive(Context context, Intent intent) {
        asyncTask.delegate = this;
        gl_context = context;
        Toast.makeText(context, "Received",Toast.LENGTH_SHORT).show();
        startProcess();
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
    public void processFinish(String output) {
        String notification_message;
        if(output.equals("Class 11 is not affected.")){
            notification_message = "Vertretungsplan morgen betrifft dich nicht";
        }else{
            notification_message = "Im Vertretungsplan könnte was relevantes stehen";
        }
        Toast.makeText(gl_context, "Worked",Toast.LENGTH_SHORT).show();
        showNotification(gl_context, notification_message);
        //writeToFile(output);

        MainActivity.downloadedText = output;
        gl_context = null;
    }

}