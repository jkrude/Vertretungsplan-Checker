package com.krude.jakob.test;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import java.io.File;
import java.util.Calendar;


public class MainActivity extends AppCompatActivity implements TimePickerDialog.OnTimeSetListener {

    public static final String CHANNEL_ID = "CHANNEL_0";
    private static final int PDF_JOB_ID = 1;
    private static final String TAG = "MainActivity";

    //public static DownloadFile asyncTask =new DownloadFile();
    public static NotificationManagerCompat notificationManager;
    public static File directory;
    public static String fileLocation;
    public static String lastSchedule = "";
    private TextView textView;
    public static String downloadedText = "";

    //private BroadcastReceiver receiver = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SharedPreferences prefs = this.getSharedPreferences(
                "com.krude.jakob.vertretungsplan", Context.MODE_PRIVATE);

        prefs.edit().putInt("PDF_JOB_ID", PDF_JOB_ID).apply();
        textView = findViewById(R.id.textView);
        textView.setMovementMethod(new ScrollingMovementMethod());

        downloadedText = prefs.getString("downloadedText","No current schedule");
        textView.setText(downloadedText);

        Switch switchWidget = findViewById(R.id.switchWidget);
        boolean state;
        state = prefs.getBoolean("switchState", false);

        switchWidget.setChecked(state);


        switchWidget.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    //startJob();
                    startAlarm();
                } else {
                    cancelJob();
                    //cancelAlarm();
                }
            }
        });

        directory = this.getFilesDir();

        //registerReceiver();
        //asyncTask.delegate = this;
        notificationManager = NotificationManagerCompat.from(this);
        createNotificationChannels();
    }

    @Override
    protected void onStop() {
        super.onStop();
        SharedPreferences prefs = this.getSharedPreferences(
                "com.krude.jakob.vertretungsplan", Context.MODE_PRIVATE);
        Switch switchWidget = findViewById(R.id.switchWidget);
        prefs.edit().putBoolean("switchState", switchWidget.isChecked()).apply();
    }


    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        setAlarm(hourOfDay, minute);
    }

    /*
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
        */
    public void startAlarm(){
        DialogFragment timePickerFragment = new TimePickerFragment();
        timePickerFragment.show(getSupportFragmentManager(), "time picker");
    }


    public void loadText(View view){
        /*
        if(!lastSchedule.isEmpty()){
            if(!lastSchedule.equals("FileNotFound") && !lastSchedule.equals("InputOutput")){
                FileInputStream fis;
                try {
                    fis = openFileInput("test.txt");

                StringBuffer fileContent = new StringBuffer("");

                byte[] buffer = new byte[1024];
                int n;
                while ((n = fis.read(buffer)) != -1)
                {
                    fileContent.append(new String(buffer, 0,n));
                }

                textView.setText(fileContent);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        */
        if(!downloadedText.isEmpty()){
            textView.setText(downloadedText);
        } else{
            textView.setText("Sorry there is no current version.");
        }

    }


    public void setAlarm(int hour, int minute){

        boolean alarmUp = (PendingIntent.getBroadcast(getApplicationContext(), 1,
                new Intent(this,AlertReceiver.class),
                PendingIntent.FLAG_NO_CREATE) != null);
        if(alarmUp){
            return;
        }
        Calendar c = Calendar.getInstance();

        c.set(Calendar.HOUR_OF_DAY, hour); // For 1 PM or 2 PM
        c.set(Calendar.MINUTE, minute);
        c.set(Calendar.SECOND, 0);
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(getApplicationContext(), AlertReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 1, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        alarmManager.setExact(AlarmManager.RTC_WAKEUP,c.getTimeInMillis(),pendingIntent);

        Toast.makeText(this,"set Alarm to "+hour+": "+ minute,Toast.LENGTH_SHORT).show();

    }


    public void cancelAlarm(){
        boolean alarmUp = (PendingIntent.getBroadcast(getApplicationContext(), 1,
                new Intent(this,AlertReceiver.class),
                PendingIntent.FLAG_NO_CREATE) != null);
        if(!alarmUp){
            return;
        }
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(getApplicationContext(),AlertReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 1, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        alarmManager.cancel(pendingIntent);
        pendingIntent.cancel();
        //unregisterReceiver();

        Intent intent1 = new Intent(getApplicationContext(),AlertReceiver.class);
        alarmUp = (PendingIntent.getBroadcast(getApplicationContext(), 1, intent1, PendingIntent.FLAG_NO_CREATE) != null);
        String out = "alarm is " + (alarmUp ? "not" : "") + " canceled.";
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


    private void startJob(){
        JobScheduler jobScheduler =
                (JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE);
        ComponentName componentName =   new ComponentName(this, PdfJobService.class);
        JobInfo jobInfo = new JobInfo.Builder(PDF_JOB_ID, componentName)
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                //.setPeriodic(60000,60000)
                //.setPeriodic(86400000 )
                .setPersisted(true)
                .build();
        jobScheduler.schedule(jobInfo);
        Log.d(TAG, "scheduled Job");

    }


    private void cancelJob(){
        JobScheduler jobScheduler =
                (JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE);
        jobScheduler.cancel(PDF_JOB_ID);
        Log.d(TAG, "canceled Job");
    }

}
