package com.example.jzhou.assigment3;

import android.Manifest;
import android.app.IntentService;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Vibrator;
import android.provider.CalendarContract;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.Toast;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;



/**
 * Created by aku on 21/01/16.
 */
public class CalendarService extends IntentService {
    private static final String LOG = "CalendarService";
    public android.os.Handler mHandler;
    public Runnable runable;
    public AudioManager audioMan;
    public static int mode;
    public static Vibrator v;
    public static final String[] INSTANCE_PROJECTION = new String[]{
            CalendarContract.Events.DTSTART,      // 1
            CalendarContract.Events.DTEND,     // 2
            CalendarContract.Events.TITLE,     // 3

    };

    // The indices for the projection array above.
    private static final int PROJECTION_DTSTART_INDEX = 0;
    private static final int PROJECTION_DTEND_INDEX = 1;
    private static final int PROJECTION_TITLE_INDEX = 2;
    long startMillis;
    Calendar endTime;
    long endMillis;

    Cursor cur = null;
    public ContentResolver contentResolver;
    public String selection;
    public String[] selectionArgs;


    public CalendarService() {
        super("CalendarService");

        mHandler = new android.os.Handler();
        startMillis = System.currentTimeMillis();
        //startMillis = beginTime.getTimeInMillis();

        endTime = Calendar.getInstance();
        //endTime.set(2016, 2, 31, 23, 0, 0);
        //endMillis = endTime.getTimeInMillis();
        endMillis = startMillis+270000000;
        Log.d(LOG, "111111");
        selection = CalendarContract.Events.DTSTART + " >= ?";
        selection = "(("+CalendarContract.Events.DTSTART + " >= ?)AND("+CalendarContract.Events.DTEND+"<=?))";
        //selection = "(( " + CalendarContract.Events.DTSTART + " >= " + startMillis + " ) AND ( " + CalendarContract.Events.DTEND + " <= " + endMillis + " ))";
        //selection = CalendarContract.Events.TITLE + "= ?";
        selectionArgs = new String[]{Long.toString(startMillis),Long.toString(endMillis)};
        //selectionArgs = new String[]{Long.toString(startMillis)};
        //selectionArgs = new String[]{"Jintian"};
    }


    @Override
    public void onCreate() {
        super.onCreate();
        v = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        audioMan = (AudioManager) getSystemService(AUDIO_SERVICE);
        mode = audioMan.getRingerMode();

    }

    @Override
    protected void onHandleIntent(Intent intent) {
        // this method is called when the service starts
        Log.d(LOG, "running");

        runable = new Runnable() {
            @Override
            public void run() {
                // ContentResolver is used to access the CalendarProvider
                contentResolver = getContentResolver();
                // Construct the query with the desired date range.
                audioMan.setRingerMode(AudioManager.RINGER_MODE_SILENT);
                Uri uri = CalendarContract.Events.CONTENT_URI;
                Log.d(LOG, startMillis + " next " + endMillis + " heihei ");
                Log.d(LOG, CalendarContract.Events.DTSTART + "123123" +      // 0
                        CalendarContract.Events.DTEND);         // 1)
                // Submit the query
                if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                cur = contentResolver.query(uri, INSTANCE_PROJECTION, selection, selectionArgs , null);
                Log.d(LOG,"kankanquery "+cur);
                Log.d(LOG, cur.moveToFirst() + "");


                if (cur.moveToFirst())
                {
                    //v.vibrate(500);
                    audioMan.setRingerMode(AudioManager.VIBRATE_SETTING_ONLY_SILENT);
                    Log.d(LOG, "Qquerying data");
                    String title = null;
                    long dtstart = 0;
                    long dtend = 0;

                    // Get the field values
                    dtstart = cur.getLong(PROJECTION_DTSTART_INDEX);
                    dtend = cur.getLong(PROJECTION_DTEND_INDEX);
                    title = cur.getString(PROJECTION_TITLE_INDEX);

                    // Do something with the values.
                    Log.i(LOG, "Event:  " + title);
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTimeInMillis(dtstart);
                    DateFormat formatter = new SimpleDateFormat("MM/dd/hh//yyyy");
                    Log.i(LOG, "Date: " + formatter.format(calendar.getTime()));
                    Toast.makeText(getApplicationContext(), "Event: "+title+" happens at "+formatter.format(calendar.getTime()), Toast.LENGTH_SHORT).show();
                    mHandler.postDelayed(runable, 10000);
                }
                while (cur.moveToNext()) {
                    Log.d(LOG, "querying data");
                    String title = null;
                    long dtstart = 0;
                    long dtend = 0;

                    // Get the field values
                    dtstart = cur.getLong(PROJECTION_DTSTART_INDEX);
                    dtend = cur.getLong(PROJECTION_DTEND_INDEX);
                    title = cur.getString(PROJECTION_TITLE_INDEX);

                    // Do something with the values.
                    Log.i(LOG, "Event:  " + title);
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTimeInMillis(dtstart);
                    DateFormat formatter = new SimpleDateFormat("MM/dd/hh//yyyy");
                    Log.i(LOG, "Date: " + formatter.format(calendar.getTime()));
                    Toast.makeText(getApplicationContext(), "Event: "+title+" happens at "+formatter.format(calendar.getTime()), Toast.LENGTH_SHORT).show();
                    mHandler.postDelayed(runable, 10000);
                }
                cur.close();
            }
        };
        mHandler.postDelayed(runable, 10000);
        audioMan.setRingerMode(mode);
      //  rc.returnRingMode();
        /*
            Set service to run every 5 mins to query the CalendarProvider for events.
                use e.g. Handler().postDelayed() function.

            Order them by timestamp to get the next one.
            Set your app to go to silentmode when System.getCurrentTimeMillis() hits the next
            calendar event.
                use RingerController.DeviceToSilent() and RingerController.returnRingMode()

            Don't query all calendars! (test first to see which ones you want)

        */

    }


}

