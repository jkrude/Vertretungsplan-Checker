package com.krude.jakob.test;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.io.File;
import java.io.IOException;

/**
 * JobService to be scheduled by the JobScheduler.
 * start another service
 */
public class PdfJobService extends JobService implements AsyncResponse{
    private static final String TAG = "JobService";
    private static DownloadFile asyncTask = new DownloadFile();
    private JobParameters parameters;

    @Override
    public boolean onStartJob(JobParameters params) {
        Log.d(TAG, "started Job");
        parameters = params;
        asyncTask.delegate = this;

        startProcess();
        //Intent service = new Intent(getApplicationContext(), PdfService.class);
        //getApplicationContext().startService(service);
        //Util.scheduleJob(getApplicationContext()); // reschedule the job
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params)
    {
        if (asyncTask != null) {
            asyncTask.cancel(true);
        }
        Log.d(TAG, "stopped Job");
        return true;
    }

    @Override
    public void processFinished(String output, boolean success) {
        Log.d(TAG, "Process finished");
        String notification_message;
        if(output.equals("Class 11 is not affected.")){
            notification_message = "Vertretungsplan morgen betrifft dich nicht";
        }else{
            notification_message = "Im Vertretungsplan könnte was relevantes stehen";
        }
        showNotification(getApplicationContext(), notification_message);
        //writeToFile(output);

        SharedPreferences prefs = getApplicationContext().getSharedPreferences(
                "com.krude.jakob.vertretungsplan", Context.MODE_PRIVATE);
        prefs.edit().putString("downloadedText",output).apply();
        Log.d(TAG, "edited prefs");
        jobFinished(parameters, !success);
        Log.d(TAG, "Job finished");
    }


    private void startProcess(){
        Log.d(TAG, "started Process");

        String fileUrl ="https://www.graues-kloster.de/files/ovp_1.pdf";   // -> http://maven.apache.org/maven-1.x/maven.pdf
        String fileName = "ovp_1.pdf";  // -> maven.pdf


        SharedPreferences prefs = getApplicationContext().getSharedPreferences(
                "com.krude.jakob.vertretungsplan", Context.MODE_PRIVATE);


        final File directory = getApplicationContext().getFilesDir();
        String fileLocation = "";
        File pdfFile = new File(directory, fileName);
        try {
            boolean createNewFileWorked = pdfFile.createNewFile();
            //TODO

            fileLocation = pdfFile.getAbsolutePath();
            prefs.edit().putString("fileLocation", fileLocation).apply();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //FileDownloader.downloadFile("https://www.graues-kloster.de/files/ovp_1.pdf", pdfFile);
        asyncTask.execute(fileUrl, fileLocation);
        Log.d(TAG, "started async Task");
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
        Log.d(TAG, "ready to show notification");
        notificationManager.notify(1, notification);
        Log.d(TAG, "showed notification");
    }



}
