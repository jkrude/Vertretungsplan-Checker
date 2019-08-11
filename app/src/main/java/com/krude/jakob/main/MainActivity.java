package com.krude.jakob.main;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.app.job.JobScheduler;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;

import androidx.fragment.app.DialogFragment;
import androidx.core.app.NotificationManagerCompat;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import java.util.Calendar;


public class MainActivity extends AppCompatActivity implements TimePickerDialog.OnTimeSetListener, AdapterView.OnItemSelectedListener {

    public static final String CHANNEL_ID = "CHANNEL_0";
    private static final int PDF_JOB_ID = 1;
    private static final String TAG = "MainActivity";

    public NotificationManagerCompat notificationManager;
    private TextView textView;
    public String relevantChanges;

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        String selected = parent.getItemAtPosition(position).toString();

        // see res/strings/options for all possibilities
        switch(selected){
            case "Relevante Änderungen":
                loadRelChanges();
                break;
            case "Alle Änderungen":
                loadAllChanges();
                break;
            case "Zusätzliche Informationen":
                loadAdditionalInfo();
                break;
            default:
                throw new IllegalArgumentException();

        }
    }


    @Override
    public void onNothingSelected(AdapterView<?> parent) {
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_settings) {// User chose the "Settings" item, show the app settings UI...
            showSettings();
            return true;
        }// If we got here, the user's action was not recognized.
        // Invoke the superclass to handle it.
        return super.onOptionsItemSelected(item);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.actionbar, menu);
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SharedPreferences prefs = this.getSharedPreferences(
                "com.krude.jakob.vertretungsplan", Context.MODE_PRIVATE);

        // ------ show tutorial on first Opening ------
        if(prefs.getBoolean("showTutorial",true)) {
            showTutorial();
            prefs.edit().putBoolean("showTutorial",false).apply();
        }


        prefs.edit().putInt("PDF_JOB_ID", PDF_JOB_ID).apply();

        // ------ all views and widgets ------
        textView = findViewById(R.id.textView);
        textView.setMovementMethod(new ScrollingMovementMethod());
        // spinner with options witch text should be loaded
        Spinner spinner = findViewById(R.id.choose_what_to_show);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.options,R.layout.spinner_text);
        adapter.setDropDownViewResource(R.layout.spinner_text);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);

        relevantChanges = prefs.getString("relevantChanges","Noch nicht verfügbar");
        textView.setText(relevantChanges);

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

    private void showTutorial(){
        new AlertDialog.Builder(this)
                .setTitle("Willkommen!")
                .setMessage("Gebe zuerst deine Klasse in den Einstellungen ein" +
                        " und aktiviere dann den Updater.\n" +
                        "Wähle dazu einfach die Zeit, wann der Vertretungsplan geckeckt werden soll")
                .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        showSettings();
                    }
                })
                .create().show();
    }


    public void startAlarm(){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        String schoolClass = prefs.getString("class",null);
        if(schoolClass == null || schoolClass.isEmpty()){
            textView.setText(getText(R.string.set_class_first));
            return;
        }
        DialogFragment timePickerFragment = new TimePickerFragment();
        timePickerFragment.show(getSupportFragmentManager(), "time picker");
    }


    public void showSettings(){
        startActivity(new Intent(MainActivity.this, SettingsActivity.class));
    }


    public void loadRelChanges(){
       SharedPreferences prefs = getSharedPreferences(
               "com.krude.jakob.vertretungsplan", Context.MODE_PRIVATE);
       String date = prefs.getString("date", "");
       relevantChanges = prefs.getString("relevantChanges", "Noch nicht verfügbar");

       String out = date+ "\n"+ relevantChanges;
       textView.setText(out);
    }


    public void loadAllChanges(){
        SharedPreferences prefs = getSharedPreferences(
                "com.krude.jakob.vertretungsplan", Context.MODE_PRIVATE);
        String date = prefs.getString("date", "");
        relevantChanges = prefs.getString("allChanges", "Noch nicht verfügbar");

        String out = date+ "\n"+ relevantChanges;
        textView.setText(out);
    }


    public void loadAdditionalInfo(){
        SharedPreferences prefs = getSharedPreferences(
                "com.krude.jakob.vertretungsplan", Context.MODE_PRIVATE);
        String addInfo = prefs.getString("additionalInfo", "Noch nicht verfügbar");

        textView.setText(addInfo);
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
