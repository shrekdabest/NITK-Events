package com.example.nitk_events;

import android.Manifest;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.SystemClock;
import android.provider.CalendarContract;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

public class SyncService extends Service {
    private NotificationManager mNotificationManager;
DatabaseReference ref;
    long eventID;
    ValueEventListener lmao;
    private static String DEFAULT_CHANNEL_ID = "default_channel";
    private static String DEFAULT_CHANNEL_NAME = "Default";


    @Override
    public IBinder onBind(Intent intent) {
        return null;

    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        new Thread(new Runnable() {
            public void run() {
                Log.d("Thread onstartcommand",Thread.currentThread().getName());
                sendnotification();

                callingsomeclass();
            }
        }).start();
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            public void run() {
                Toast.makeText(getApplicationContext(),"Syncing has started",Toast.LENGTH_SHORT).show();
            }
        });
     return START_STICKY;
    }

    public void callingsomeclass()
        {
        ref= FirebaseDatabase.getInstance().getReferenceFromUrl("https://nitk-calendar.firebaseio.com/");
           lmao = ref.addValueEventListener(new ValueEventListener() {

                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    deleteallevents();
                    getSharedPreferences("destroyer", Context.MODE_PRIVATE).edit().clear().apply();
                    for (DataSnapshot child : dataSnapshot.getChildren()) {
                        for (DataSnapshot childd : child.getChildren()) {
                           addevents(String.valueOf(child.getKey()),String.valueOf(childd.getKey()),String.valueOf(childd.child("Description").getValue()),String.valueOf(childd.child("Event Name").getValue()));
                        }
                    }



                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }


            });


    }

    public void sendnotification()
    {  Log.d("send notification",Thread.currentThread().getName());
        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        createNotificationChannel(mNotificationManager);
        Intent notificationIntent = new Intent(getApplicationContext(), MainActivity.class);


        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);


        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);


        Notification notification = new NotificationCompat.Builder(this, DEFAULT_CHANNEL_ID)
                .setContentTitle("NITK SYNC")   //Set the title of Notification
                .setContentText("We are on the lookout for new events")    //Set the text for notification
                .setSmallIcon(R.mipmap.ic_launcher)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(contentIntent)//Set the icon
                .build();

        //Send the notification.
        mNotificationManager.notify(1, notification);
    }



    public static void createNotificationChannel(NotificationManager notificationManager) {
        Log.d("createnotifichannel",Thread.currentThread().getName());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //Create channel only if it is not already created
            if (notificationManager.getNotificationChannel(DEFAULT_CHANNEL_ID) == null) {
                notificationManager.createNotificationChannel(new NotificationChannel(
                        DEFAULT_CHANNEL_ID, DEFAULT_CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH
                ));
            }
        }
    }


    public void addevents(String month,String day,String description,String name)
    {
        Log.d("add events",Thread.currentThread().getName());

                SharedPreferences prefs = getSharedPreferences("Calendar_ID", MODE_PRIVATE);
        String restoredText = prefs.getString("calendarid", null);
        String targetCalendarId =restoredText;
        long calendarId = Long.parseLong(targetCalendarId);
        long currentTimeMillis = System.currentTimeMillis();
        long endTimeMillis = currentTimeMillis + 900000;

       // String targetTitle = ((EditText) findViewById(R.id.title)).getText().toString();
        Calendar beginTime = Calendar.getInstance();
        beginTime.set(2019, Integer.parseInt(month), Integer.parseInt(day)+1, 0, 00);
        Calendar endTime = Calendar.getInstance();
        endTime.set(2019, Integer.parseInt(month), Integer.parseInt(day)+1,23, 59);
        ContentResolver cr = getContentResolver();
        ContentValues values = new ContentValues();
        values.put(CalendarContract.Events.DTSTART, beginTime.getTimeInMillis());
        values.put(CalendarContract.Events.DTEND, endTime.getTimeInMillis());
        values.put(CalendarContract.EXTRA_EVENT_ALL_DAY,true);
        values.put(CalendarContract.Events.TITLE, name);
        values.put(CalendarContract.Events.DESCRIPTION, description);
        values.put(CalendarContract.Events.CALENDAR_ID, calendarId);
        values.put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().getDisplayName());
        values.put(CalendarContract.Events.HAS_ALARM, true);
        int permissionCheck = ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.WRITE_CALENDAR);

        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
            Uri uri = cr.insert(CalendarContract.Events.CONTENT_URI, values);
            if (uri != null) {
                 eventID = Long.parseLong(uri.getLastPathSegment());


            }
        }
        ContentValues reminders = new ContentValues();
        reminders.put(CalendarContract.Reminders.EVENT_ID, eventID);
        reminders.put(CalendarContract.Reminders.METHOD, CalendarContract.Reminders.METHOD_ALERT);
        reminders.put(CalendarContract.Reminders.MINUTES, -450);

        Uri uri2 = cr.insert(CalendarContract.Reminders.CONTENT_URI, reminders);
saveeventID(eventID,day,name);

    }
public void saveeventID(Long eid,String day,String name)
{  Log.d("save event id",Thread.currentThread().getName());
    SharedPreferences.Editor deer = getSharedPreferences("destroyer", MODE_PRIVATE).edit();
    Log.d("Sometag", String.valueOf(eid));
    deer.putLong(name+""+day, eid );
    deer.apply();


}

    public void delete_event(Long x) {
        Log.d(" values", String.valueOf(x));
        /*ContentResolver cr = getContentResolver();
        int permissionCheck = ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.WRITE_CALENDAR);
        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
            Uri uri = ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, x);
            cr.delete(uri, null, null);
        }*/
        ContentResolver cr = getApplicationContext().getContentResolver();

        Uri deleteUri = null;
        deleteUri = ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, x);
        int rows = cr.delete(deleteUri, null, null);
        Log.d("ROWS",String.valueOf(rows));

    }
    public void deleteallevents()
    {
        SharedPreferences prefs = getSharedPreferences("destroyer", MODE_PRIVATE);
        Map<String,?> keys = prefs.getAll();

        for(Map.Entry<String,?> entry : keys.entrySet()){
            Log.d("map values",entry.getKey() + ": " +
                    entry.getValue().toString());
            delete_event(Long.parseLong(entry.getValue().toString()));
        }
        Log.d("Thread deleteallevents",Thread.currentThread().getName());
    }
    @Override
    public void onTaskRemoved(Intent rootIntent){
        Intent restartServiceTask = new Intent(getApplicationContext(),this.getClass());
        restartServiceTask.setPackage(getPackageName());
        PendingIntent restartPendingIntent =PendingIntent.getService(getApplicationContext(), 1,restartServiceTask, PendingIntent.FLAG_ONE_SHOT);
        AlarmManager myAlarmService = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        myAlarmService.set(
                AlarmManager.ELAPSED_REALTIME,
                SystemClock.elapsedRealtime() + 1000,
                restartPendingIntent);

        super.onTaskRemoved(rootIntent);
    }


}
