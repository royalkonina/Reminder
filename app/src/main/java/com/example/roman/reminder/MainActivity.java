package com.example.roman.reminder;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity {

  public static final String TIMEPICKER_TAG = "timePicker";
  public static final String DATEPICKER_TAG = "datePicker";
  public static final String PREFS_NAME = "PrefsData";
  public static final String YEAR_PREFS_KEY = "chosenYear";
  public static final String MONTH_PREFS_KEY = "chosenMonth";
  public static final String DAY_PREFS_KEY = "chosenDay";
  public static final String HOUR_PREFS_KEY = "chosenHour";
  public static final String MINUTE_PREFS_KEY = "chosenMinute";
  public static final String TITLE_PREFS_KEY = "title";
  public static final String DESCRIPTION_PREFS_KEY = "description";
  public static final String ALARM_IS_SET = "Alarm is set";



  private EditText dateEditText;
  private EditText timeEditText;
  private EditText titleEditText;
  private EditText descriptionEditText;
  private Button saveButton;
  private int chosenYear = -1;
  private int chosenMonth = -1;
  private int chosenDay = -1;
  private int chosenHour = -1;
  private int chosenMinute = -1;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    titleEditText = (EditText) findViewById(R.id.title_edittext);
    descriptionEditText = (EditText) findViewById(R.id.description_edittext);

    configureDateEditText();
    configureTimeEditText();
    configureSaveButton();
    restoreData();

  }

  private boolean isDataCorrect() {
    return !String.valueOf(titleEditText.getText()).equals("") && chosenYear >= 0 && chosenMonth >= 0 && chosenDay >= 0 && chosenHour >= 0 && chosenMinute >= 0;
  }

  private void restoreData() {
    SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
    chosenYear = settings.getInt(YEAR_PREFS_KEY, chosenYear);
    chosenMonth = settings.getInt(MONTH_PREFS_KEY, chosenMonth);
    chosenDay = settings.getInt(DAY_PREFS_KEY, chosenDay);
    chosenHour = settings.getInt(HOUR_PREFS_KEY, chosenHour);
    chosenMinute = settings.getInt(MINUTE_PREFS_KEY, chosenMonth);
    titleEditText.setText(settings.getString(TITLE_PREFS_KEY, ""));
    descriptionEditText.setText(settings.getString(DESCRIPTION_PREFS_KEY, ""));
    timeEditText.setText(chosenHour + ":" + chosenMinute);
    dateEditText.setText(chosenYear + "/" + (chosenMonth + 1) + "/" + chosenDay);
  }

  private void configureSaveButton() {
    saveButton = (Button) findViewById(R.id.save_button);
    saveButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        // We need an Editor object to make preference changes.
        // All objects are from android.context.Context
        if (isDataCorrect()) {
          SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
          SharedPreferences.Editor editor = settings.edit();
          editor.putInt(YEAR_PREFS_KEY, chosenYear);
          editor.putInt(MONTH_PREFS_KEY, chosenMonth);
          editor.putInt(DAY_PREFS_KEY, chosenDay);
          editor.putInt(HOUR_PREFS_KEY, chosenHour);
          editor.putInt(MINUTE_PREFS_KEY, chosenMinute);
          editor.putString(TITLE_PREFS_KEY, String.valueOf(titleEditText.getText()));
          editor.putString(DESCRIPTION_PREFS_KEY, String.valueOf(descriptionEditText.getText()));
          // Commit the edits!
          //editor.commit();
          editor.apply();
          Log.d("SAVED", "SAVED");
          long time = getTimeForNotification();
          scheduleNotification(getNotification(String.valueOf(titleEditText.getText()), String.valueOf(descriptionEditText.getText())), time);
          Toast.makeText(MainActivity.this, ALARM_IS_SET,
                  Toast.LENGTH_SHORT).show();
        }
      }
    });
  }

  private void configureTimeEditText() {
    timeEditText = (EditText) findViewById(R.id.time_edittext);
    timeEditText.setKeyListener(null);
    timeEditText.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        TimePickerFragment newFragment = new TimePickerFragment();
        newFragment.setOnTimeChosenListener(new OnTimeChosenListener() {
          @Override
          public void onTimeChosen(int hourOfDay, int minute) {
            chosenHour = hourOfDay;
            chosenMinute = minute;
            timeEditText.setText(hourOfDay + ":" + minute);
          }
        });
        newFragment.show(getFragmentManager(), TIMEPICKER_TAG);
      }
    });
  }

  private void configureDateEditText() {
    dateEditText = (EditText) findViewById(R.id.date_edittext);
    dateEditText.setKeyListener(null);
    dateEditText.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        DatePickerFragment newFragment = new DatePickerFragment();
        newFragment.setOnDateChosenListener(new OnDateChosenListener() {
          @Override
          public void onDateChosen(int year, int month, int day) {
            chosenYear = year;
            chosenMonth = month;
            chosenDay = day;
            dateEditText.setText(year + "/" + (month + 1) + "/" + day);
          }
        });
        newFragment.show(getFragmentManager(), DATEPICKER_TAG);
      }
    });
  }

  private Notification getNotification(String title, String description) {
    Notification.Builder builder = new Notification.Builder(this);
    builder.setContentTitle(title);
    builder.setContentText(description);
    builder.setSmallIcon(R.mipmap.ic_launcher);
    Intent notificationIntent = new Intent(this, MainActivity.class);
    notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
            | Intent.FLAG_ACTIVITY_SINGLE_TOP);
    PendingIntent intent = PendingIntent.getActivity(this, 0,
            notificationIntent, 0);

    builder.setContentIntent(intent);
    return builder.build();
  }

  private void scheduleNotification(Notification notification, long time) {

    Intent notificationIntent = new Intent(this, NotificationPublisher.class);
    notificationIntent.putExtra(NotificationPublisher.NOTIFICATION_ID, 1);
    notificationIntent.putExtra(NotificationPublisher.NOTIFICATION, notification);
    PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

    AlarmManager alarmManager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
    alarmManager.set(AlarmManager.RTC_WAKEUP, time, pendingIntent);

  }

  public long getTimeForNotification() {
    Calendar calendar = Calendar.getInstance();
    calendar.set(Calendar.YEAR, chosenYear);
    calendar.set(Calendar.MONTH, chosenMonth);
    calendar.set(Calendar.DAY_OF_MONTH, chosenDay);
    calendar.set(Calendar.HOUR_OF_DAY, chosenHour);
    calendar.set(Calendar.MINUTE, chosenMinute);
    return calendar.getTimeInMillis();
  }
}
