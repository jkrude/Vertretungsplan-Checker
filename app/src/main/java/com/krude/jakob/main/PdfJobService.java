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
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

/**
 * JobService to be scheduled by the JobScheduler.
 * start another service
 */
public class PdfJobService extends JobService implements AsyncResponse{
    private static final String TAG = "JobService";
    private static DownloadFile asyncTask;
    private JobParameters parameters;
    private String fileLocation;

    @Override
    public boolean onStartJob(JobParameters params) {
        Log.d(TAG, "started Job");
        parameters = params;
        asyncTask = new DownloadFile();
        asyncTask.delegate = this;

        startProcess();
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
        fileLocation = "";
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
    public void downloadFinished(boolean success) {
        // this is called from onPostExecute in DownloadFile

        if(fileLocation == null || fileLocation.isEmpty())
            throw new IllegalArgumentException();

        SharedPreferences defPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        String schoolClass = defPrefs.getString("class", null);
        if(schoolClass == null)
            throw new NullPointerException(); // no class specified
        String [] visitedCourses = null;
        if(defPrefs.getBoolean("hasCourses",false)){
            String tmpVisitedCourses = defPrefs.getString("chosenCourses", null);
            if(tmpVisitedCourses == null)
                throw new NullPointerException();
            tmpVisitedCourses = tmpVisitedCourses.replace(" ", "").replace("\n", "");
            visitedCourses = tmpVisitedCourses.split(",");
            Log.d(TAG, "visitedCourses: "+tmpVisitedCourses);
        }

        // scan the downloaded pdf for relevance
        ScanedPdf scanedPdf = FileScanner.scanPdf(fileLocation,schoolClass,visitedCourses);

        Log.d(TAG, "Process finished");
        if(success) {
            String notification_message = "Unknown Error";
            switch (scanedPdf.getState()) {
                case OUT_OF_DATE:
                    notification_message = "Der Vertretungsplan ist noch nicht aktuell";
                    break;

                case NOT_AFFECTED:
                    notification_message = "Vertretungsplan morgen betrifft dich nicht";
                    break;

                case BAD_LAYOUT:
                    notification_message = "Fehler: Das Pdf ist falsch formatiert";
                    break;

                case IO_EXCEPTION:
                    notification_message = "Fehler: Auf das Pdf konnte nicht zugegriffen werden";
                    break;

                case AFFECTED:
                    notification_message = "Der Vertretungsplan ist relevant morgen";
                    break;
            }
            SharedPreferences prefs = getApplicationContext().getSharedPreferences(
                    "com.krude.jakob.vertretungsplan", Context.MODE_PRIVATE);

            List<String> allChanges = scanedPdf.getAllChanges();
            if(allChanges != null)
                prefs.edit().putString("allChanges", transformListToString(allChanges)).apply();

            List<String> relChanges = scanedPdf.getRelevantChanges();
            if(relChanges != null)
                prefs.edit().putString("relevantChanges", transformListToString(relChanges)).apply();

            List<String> addInfo = scanedPdf.getAdditionalInfo();
            if(addInfo != null)
                prefs.edit().putString("additionalInfo", transformListToString(addInfo)).apply();
            String date = scanedPdf.getDate();
            if(date != null)
                prefs.edit().putString("date",date).apply();

            showNotification(getApplicationContext(), notification_message);

            Log.d(TAG, "updated prefs->relevantChanges, additionalInformation");
        }else{
            Log.d(TAG, "Job unsuccessful");
        }
        jobFinished(parameters, !success);
        Log.d(TAG, "Job finished");
    }

    private String transformListToString(List<String> list){
        StringBuilder stringBuilder = new StringBuilder();
        for(String string : list){
            stringBuilder.append(string).append("\n");
        }
        return stringBuilder.toString();
    }

}
