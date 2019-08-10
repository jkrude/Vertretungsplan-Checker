package com.krude.jakob.main;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.app.job.JobScheduler;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import androidx.fragment.app.DialogFragment;
import androidx.core.app.NotificationManagerCompat;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import java.util.Calendar;


public class MainActivity extends AppCompatActivity implements TimePickerDialog.OnTimeSetListener {

    public static final String CHANNEL_ID = "CHANNEL_0";
    private static final int PDF_JOB_ID = 1;
    private static final String TAG = "MainActivity";

    public static NotificationManagerCompat notificationManager;
    private TextView textView;
    public static String downloadedText = "";


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

    public void startAlarm(){
        DialogFragment timePickerFragment = new TimePickerFragment();
        timePickerFragment.show(getSupportFragmentManager(), "time picker");
    }

    public void showSettings(View view){
        startActivity(new Intent(MainActivity.this, SettingsActivity.class));
    }


    public void loadText(View view){
       SharedPreferences prefs = getSharedPreferences(
               "com.krude.jakob.vertretungsplan", Context.MODE_PRIVATE);
       String date = prefs.getString("date", "date not available");
       String body = prefs.getString("downloadedText", "failed");
       String out = date+ "\n"+ body;
       textView.setText(out);
    }

    public void loadAdditionalInfo(View view){
        SharedPreferences prefs = getSharedPreferences(
                "com.krude.jakob.vertretungsplan", Context.MODE_PRIVATE);
        textView.setText(prefs.getString("additionalInformation", "failed"));
    }


    public void setAlarm(int hour, int minute){

        boolean alarmUp = (PendingIntent.getBroadcast(getApplicationContext(), 1,
                new Intent(this,AlertReceiver.class),
                PendingIntent.FLAG_NO_CREATE) != null);
        if(alarmUp){
            Log.d(TAG, "Alarm already active");

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


    private void cancelJob(){
        JobScheduler jobScheduler =
                (JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE);
        jobScheduler.cancel(PDF_JOB_ID);
        Log.d(TAG, "canceled Job");
    }

}
