package com.example.nitk_events;

import android.Manifest;
import android.accounts.AccountManager;
import android.app.ActionBar;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CalendarContract;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.common.AccountPicker;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

import static android.app.Activity.RESULT_OK;
import static android.content.Context.MODE_PRIVATE;

public class MainActivity extends AppCompatActivity {



    String accountName;
    String calendarID;
    static final int GET_ACCOUNT_NAME_REQUEST = 1;
DatabaseReference ref;
TextView emailshow,calendarnameshow;








    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
emailshow=findViewById(R.id.emailshow);
calendarnameshow=findViewById(R.id.calendarnameshow);

        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.abs_layout);

        SharedPreferences prefs = getSharedPreferences("Account_Name", MODE_PRIVATE);
        String restoredText = prefs.getString("email", null);
emailshow.setText(restoredText);
        SharedPreferences prefos = getSharedPreferences("Calendar_ID", MODE_PRIVATE);
        String enoondu = prefos.getString("calendarid", null);
        calendarnameshow.setText(enoondu);
      /*  if(restoredText!=null && enoondu!=null)
        {
LinearLayout ll=findViewById(R.id.llll);
ll.setGravity(Gravity.CENTER);

            findViewById(R.id.emailshow).setVisibility(View.GONE);
        findViewById(R.id.calendarnameshow).setVisibility(View.GONE);
            findViewById(R.id.email).setVisibility(View.GONE);
            findViewById(R.id.calendarname).setVisibility(View.GONE);

        }*/





        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.READ_CALENDAR,
                        Manifest.permission.WRITE_CALENDAR}, 1);//Here we ask the students for read and write permissions to their calendar

    }

    public void addaccount(View view)
    {

        if(ActivityCompat.checkSelfPermission(getApplicationContext(),Manifest.permission.READ_CALENDAR)!= PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(),Manifest.permission.WRITE_CALENDAR)!= PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.READ_CALENDAR,Manifest.permission.WRITE_CALENDAR},1);
        }
        else
        {

            try {
                Intent intent = AccountPicker.newChooseAccountIntent(null, null,
                        new String[] { GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE }, true, "We need you to pick a google account so that we can sync the NITK calendar with the calendar associated with it", null, null, null);
                startActivityForResult(intent, GET_ACCOUNT_NAME_REQUEST ); //This asks the user to choose the google account so we can add events to his google calendar
            } catch (ActivityNotFoundException e) {
                Toast.makeText(this,"It seems we are unable to detect the accounts in your device,try again later", Toast.LENGTH_LONG).show();

            }

        }



    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == GET_ACCOUNT_NAME_REQUEST && resultCode == RESULT_OK) {
            accountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
           emailshow.setText(accountName);
            SharedPreferences.Editor editor = getSharedPreferences("Account_Name", MODE_PRIVATE).edit();
            editor.putString("email", accountName);
            editor.apply();
            findViewById(R.id.calendarname).setEnabled(true);

        }
    }

    public void calendarselector(View view) {

        String[] EVENT_PROJECTION = new String[]{
                CalendarContract.Calendars._ID,
                CalendarContract.Calendars.ACCOUNT_NAME,
                CalendarContract.Calendars.CALENDAR_DISPLAY_NAME,
                CalendarContract.Calendars.OWNER_ACCOUNT,
                CalendarContract.Calendars.CALENDAR_ACCESS_LEVEL,
        };
        int PROJECTION_ID_INDEX = 0;
        int PROJECTION_ACCOUNT_NAME_INDEX = 1;
        int PROJECTION_DISPLAY_NAME_INDEX = 2;
        int PROJECTION_OWNER_ACCOUNT_INDEX = 3;
        int PROJECTION_CALENDAR_ACCESS_LEVEL = 4;
        String targetAccount = accountName;
        // now we query the list of calendars under the email id user signed up for using the tables returned by api  from cursor class
        Cursor cur;
        ContentResolver cr = getContentResolver();
        Uri uri = CalendarContract.Calendars.CONTENT_URI;

        //Define query criteria to find calendars that belong to the above Google account and have full control
        String selection = "((" + CalendarContract.Calendars.ACCOUNT_NAME + " = ?) AND ("
                + CalendarContract.Calendars.ACCOUNT_TYPE + " = ?) AND ("
                + CalendarContract.Calendars.CALENDAR_ACCESS_LEVEL + " = ?))";
        String[] selectionArgs = new String[]{targetAccount,
                "com.google",
                Integer.toString(CalendarContract.Calendars.CAL_ACCESS_OWNER)};

        int permissionCheck = ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.READ_CALENDAR);
        // this temporarily stores the details of query
        final List<String> accountNameList = new ArrayList<>();
        final List<Integer> calendarIdList = new ArrayList<>();

        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
            cur = cr.query(uri, EVENT_PROJECTION, selection, selectionArgs, null);
            if (cur != null) {
                while (cur.moveToNext()) {
                    long calendarId = 0;
                    String accountName = null;
                    String displayName = null;
                    String ownerAccount = null;
                    int accessLevel = 0;

                    calendarId = cur.getLong(PROJECTION_ID_INDEX);
                    accountName = cur.getString(PROJECTION_ACCOUNT_NAME_INDEX);
                    displayName = cur.getString(PROJECTION_DISPLAY_NAME_INDEX);
                    ownerAccount = cur.getString(PROJECTION_OWNER_ACCOUNT_INDEX);
                    accessLevel = cur.getInt(PROJECTION_CALENDAR_ACCESS_LEVEL);

                    accountNameList.add(displayName);
                    calendarIdList.add((int) calendarId);
                }
                cur.close();
            }
            if (calendarIdList.size() != 0) {
                AlertDialog.Builder adb = new AlertDialog.Builder(this);
                adb.setTitle("Please click on the calendar you want to sync NITK calendar with");
                CharSequence items[] = accountNameList.toArray(new CharSequence[accountNameList.size()]);
                adb.setSingleChoiceItems(items, 0, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        calendarnameshow.setText(String.valueOf(calendarIdList.get(which)));
                        SharedPreferences.Editor editor = getSharedPreferences("Calendar_ID", MODE_PRIVATE).edit();
                        editor.putString("calendarid",  String.valueOf(calendarIdList.get(which)));
                        editor.apply();

                        dialog.dismiss();
                    }
                });
                adb.setNegativeButton("CANCEL", null);
                adb.show();
            }
            else {
                Toast toast = Toast.makeText(this, "Calendar not found", Toast.LENGTH_LONG);
                toast.show();
            }
        }
        else {
            Toast toast = Toast.makeText(this, "We don't have the required permissions", Toast.LENGTH_LONG);
            toast.show();
        }

    }

    public void startservice(View view) {
makethemalert();

    }


    public void visitcalendar(View view) {

        PackageManager pm = getApplicationContext().getPackageManager();
        Intent appStartIntent = pm.getLaunchIntentForPackage("com.google.android.calendar");
        if (null != appStartIntent)
        {
            getApplicationContext().startActivity(appStartIntent);
        }


    }

    public void makethemalert()
    {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this,R.style.MyDialogTheme);
        builder.setTitle("Welcome to NITK Events");
        builder.setMessage("Once you click this button we will start adding all the events to your google calendar." +
                "You need not open this app again as we will be doing all the work in the background. You are, however, free " +
                "to visit this app to change your google account or the calendar.");
        builder.setCancelable(true);
        builder.setPositiveButton("Start syncing", new DialogInterface.OnClickListener() {
            public void onClick( DialogInterface dialog,  int id) {
                startService(new Intent(MainActivity.this,SyncService.class));

            }
        });
        builder.setNegativeButton("No thanks", new DialogInterface.OnClickListener() {
            public void onClick( DialogInterface dialog,  int id) {
                dialog.cancel();
            }
        });
        android.app.AlertDialog alert = builder.create();
        alert.show();


    }
}
