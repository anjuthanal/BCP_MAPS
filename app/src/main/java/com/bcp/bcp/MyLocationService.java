package com.bcp.bcp;


import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.bcp.bcp.database.DatabaseHandler;
import com.bcp.bcp.database.LocationData;
import com.bcp.bcp.geofencing.Constants;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by anjup on 3/10/16.
 */
public class MyLocationService extends Service {


    private static final String TAG = "MyLocationService";

    private long delay = 2000;
    private Handler handler;
    private GPSTracker gps;
    private double latitude = 0, longitude = 0;
    private Credentials credentials;
    private Runnable runnable;
    DatabaseHandler databaseHandler;
    String locEntryDate;
    Geocoder geocoder;
    List<Address> addresses;
    StringBuilder adstrng;

    @Override
    public void onCreate() {
        Log.i(TAG, "Service onCreate");
    }

    @Override
    public int onStartCommand(final Intent intent, int flags, int startId) {
        Log.i(TAG, "Service onStartCommand");
        Toast.makeText(getApplicationContext(), "Tracking Started", Toast.LENGTH_LONG).show();

        handler = new Handler();
        credentials = new Credentials();
        final SharedPreferences mSharedPreferences = getApplicationContext().getSharedPreferences("Shared", Context.MODE_PRIVATE);

        runnable = new Runnable() {
            @Override
            public void run() {
                delay = mSharedPreferences.getLong("CONFIG TIME", 60000);
                handler.postDelayed(this, delay);
                gpsTracker();
                Log.e("MyService", "delay = " + delay);
            }
        };

        handler.post(runnable);

        return super.onStartCommand(intent, flags, startId);
    }



    public void gpsTracker() {
        gps = new GPSTracker(MyLocationService.this);

        if (gps.canGetLocation()) {
            latitude = gps.getLatitude();
            longitude = gps.getLongitude();

            Log.e("latitude", "" + latitude);
            Log.e("longitude", "" + longitude);
            if(latitude != 0 && longitude != 0){
                File file = credentials.saveFile(latitude, longitude,getApplicationContext());
                new UploadToFTAsync(UploadToFTAsync.uploadFile, file, getApplicationContext()).execute();

                //insert lat/long as address in local db
                Date curDate = new Date();
                SimpleDateFormat format = new SimpleDateFormat(Constants.TIME_FORMAT);
                locEntryDate = format.format(curDate);
                boolean isInserted;

                databaseHandler = new DatabaseHandler(this);
                String addToInsert = getAddress(latitude,longitude);
                isInserted = databaseHandler.addLocation(new LocationData(addToInsert, locEntryDate));
                if (isInserted) {
                    Log.e("Location : ", "inserted to local db" );
                    Log.e("Location : ",addToInsert +" "+locEntryDate);
                }

            }
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.e("Service stopped", "Service stopped");
        try {
            handler.removeCallbacks(runnable);
        } catch (Exception e) {
            e.printStackTrace();
        };
    }


    public  String getAddress(double lat, double lng) {

        try {
            geocoder = new Geocoder(this, Locale.ENGLISH);
            addresses = geocoder.getFromLocation(lat, lng, 1);
            adstrng = new StringBuilder();
            if (geocoder.isPresent()) {
                Address returnAddress = addresses.get(0);
                String street = "";
                String address = "";
                int maxindex = returnAddress.getMaxAddressLineIndex();
                for(int i=0;i<maxindex;i++){

                    street = returnAddress.getAddressLine(i);
                    address = address+ ", "+ street;
                }

                adstrng.append(address + " ");



            } else {
                Toast.makeText(getApplicationContext(),
                        "geocoder not present", Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
// TODO Auto-generated catch block

            Log.e("tag", e.getMessage());
        }

        return adstrng.toString();
    }
}
