package com.krude.jakob.main;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Context;
import android.content.SharedPreferences;
import androidx.core.app.NotificationCompat;

import android.preference.PreferenceManager;
import android.util.Log;

import java.io.File;
import java.io.IOException;

/**
 * JobService to be scheduled by the JobScheduler.
 * start another service
 */
public class PdfJobService extends JobService implements AsyncResponse{
    private static final String TAG = "JobService";
    private static DownloadFile asyncTask;
    private JobParameters parameters;

    @Override
    public boolean onStartJob(JobParameters params) {
        Log.d(TAG, "started Job");
        parameters = params;
        asyncTask = new DownloadFile();
        asyncTask.delegate = this;

        startProcess();
        //Intent service = new Intent(getApplicationContext(), PdfService.class);
        //getApplicationContext().startService(service);
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

    private void startProcess(){
        Log.d(TAG, "started Process");

        String fileUrl ="https://www.graues-kloster.de/files/ovp_1.pdf";   // -> https://www.graues-kloster.de/files/ovp_1.pdf
        String fileName = "ovp_1.pdf";  // -> ovp_1.pdf


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
        asyncTask.execute(fileUrl, fileLocation,"11");
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

    @Override
    public void processFinished(String[] output, boolean success) {
        boolean hasAdditionalInfo = false;
        boolean hasDownloadedText = false;

        Log.d(TAG, "Process finished");
        if(success) {
            String notification_message;
            switch (output[0]) {
                case FileScanner.outOfDate:
                    notification_message = "Der Vertretungsplan ist noch nicht aktuell";
                    hasAdditionalInfo = true;
                    break;
                case FileScanner.notAffected:
                    notification_message = "Vertretungsplan morgen betrifft dich nicht";
                    hasAdditionalInfo = true;
                    break;
                case FileScanner.badLayout:
                    notification_message = "Fehler: Das Pdf ist falsch formatiert";

                    break;
                case FileScanner.ioException:
                    notification_message = "Fehler: Auf das Pdf konnte nicht zugegriffen werden";
                    break;
                default:
                    notification_message = "Der Vertretungsplan ist relevant morgen";
                    hasAdditionalInfo = true;
                    hasDownloadedText = true;
                    break;
            }
            SharedPreferences prefs = getApplicationContext().getSharedPreferences(
                    "com.krude.jakob.vertretungsplan", Context.MODE_PRIVATE);
            if(hasDownloadedText){
                prefs.edit().putString("downloadedText", output[0]).apply();
            }
            if(hasAdditionalInfo){
                prefs.edit().putString("additionalInformation", output[1]).apply();
            }
            if(output.length == 3){
                prefs.edit().putString("date",output[2]).apply();
            }
            showNotification(getApplicationContext(), notification_message);

            Log.d(TAG, "updated prefs->downloadedText, additionalInformation");
        }else{
            Log.d(TAG, "Job unsuccessful");
        }
        jobFinished(parameters, !success);
        Log.d(TAG, "Job finished");
    }

}
