package com.krude.jakob.test;


import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;


public class AlertReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Toast.makeText(context, "Received",Toast.LENGTH_SHORT).show();
        //showNotification(context);
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
        MainActivity.asyncTask.execute(fileUrl, MainActivity.fileLocation);
    }
    public void showNotification(Context context) {
        Notification notification = new NotificationCompat.Builder(context, MainActivity.CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setContentTitle("TestTitle")
                .setContentText("Bling")
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                .build();

        MainActivity.notificationManager.notify(1, notification);
    }


}
