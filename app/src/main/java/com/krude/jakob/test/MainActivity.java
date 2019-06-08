package com.krude.jakob.test;

import android.Manifest;
import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
//import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import java.io.File;
import java.io.IOException;
import java.util.Calendar;


public class MainActivity extends AppCompatActivity implements AsyncResponse {

    public static final String CHANNEL_ID = "CHANNEL_0";

    public static DownloadFile asyncTask =new DownloadFile();
    public static NotificationManagerCompat notificationManager;
    public static File directory;
    public static String fileLocation;
    private TextView textView;

    //private BroadcastReceiver receiver = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView = findViewById(R.id.textView);
        textView.setMovementMethod(new ScrollingMovementMethod());
        directory = this.getFilesDir();
        //registerReceiver();
        asyncTask.delegate = this;
        notificationManager = NotificationManagerCompat.from(this);
        createNotificationChannels();
    }


    @Override
    public void processFinish(String output){
        textView.setText(output);
        //Here you will receive the result fired from async class
        //of onPostExecute(result) method.
    }

    public void downloadPdf (){
        Context context = MainActivity.this;
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.INTERNET)
                != PackageManager.PERMISSION_GRANTED) {
            System.err.println("Permission Missing");
        } else{
            System.out.println("Permission granted");
        }
        String fileUrl ="https://www.graues-kloster.de/files/ovp_1.pdf";   // -> http://maven.apache.org/maven-1.x/maven.pdf
        String fileName = "ovp_1.pdf";  // -> maven.pdf

        File pdfFile = new File(directory, fileName);
        try {
            boolean createNewFileWorked = pdfFile.createNewFile();
            //TODO
            fileLocation = pdfFile.getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //FileDownloader.downloadFile("https://www.graues-kloster.de/files/ovp_1.pdf", pdfFile);
        Toast.makeText(context, "Downloading file",
                Toast.LENGTH_LONG).show();
        asyncTask.execute(fileUrl, fileLocation);
    }

    
    public void setAlarm(View view){

        boolean alarmUp = (PendingIntent.getBroadcast(MainActivity.this, 1,
                new Intent(this,AlertReceiver.class),
                PendingIntent.FLAG_NO_CREATE) != null);
        if(alarmUp){
            return;
        }
        Calendar c = Calendar.getInstance();

        c.set(Calendar.HOUR_OF_DAY, 15); // For 1 PM or 2 PM
        c.set(Calendar.MINUTE,11);
        c.set(Calendar.SECOND, 0);
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this,AlertReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 1, intent, 0);
        alarmManager.setExact(AlarmManager.RTC_WAKEUP,c.getTimeInMillis(),pendingIntent);
        Toast.makeText(this,"set Alarm to"+c.get(Calendar.HOUR),Toast.LENGTH_SHORT).show();
        //downloadPdf();
    }

    public void cancelAlarm(View view){
        boolean alarmUp = (PendingIntent.getBroadcast(MainActivity.this, 1,
                new Intent(this,AlertReceiver.class),
                PendingIntent.FLAG_NO_CREATE) != null);
        if(!alarmUp){
            return;
        }
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this,AlertReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 1, intent, 0);
        alarmManager.cancel(pendingIntent);
        //unregisterReceiver();

        String out = "Alarm canceled";
        textView.setText(out);

    }

    private void createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel1 = new NotificationChannel(
                    CHANNEL_ID,
                    "Channel 0",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel1.setDescription("This is Channel 1");

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel1);
        }
    }

/*
    public void registerReceiver(){

        if(receiver == null){
            IntentFilter intentFilter = new IntentFilter("action_name");
            intentFilter.addAction("Alarm_Intent");
            receiver = new AlertReceiver();
            this.registerReceiver(receiver,intentFilter);
        }
    }

    public void unregisterReceiver(){
        if(receiver != null){
            this.unregisterReceiver(receiver);
            receiver = null;
        }
    }
*/

}
