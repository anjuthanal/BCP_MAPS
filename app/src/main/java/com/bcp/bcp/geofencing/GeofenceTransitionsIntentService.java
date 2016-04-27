/**
 * Copyright 2014 Google Inc. All Rights Reserved.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.bcp.bcp.geofencing;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;

import com.bcp.bcp.Credentials;
import com.bcp.bcp.MainActivity;
import com.bcp.bcp.MyLocationService;
import com.bcp.bcp.R;
import com.bcp.bcp.database.DatabaseHandler;
import com.bcp.bcp.database.FenceTiming;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Listener for geofence transition changes.
 *
 * Receives geofence transition events from Location Services in the form of an Intent containing
 * the transition type and geofence id(s) that triggered the transition. Creates a notification
 * as the output.
 */
public class GeofenceTransitionsIntentService extends IntentService {

    protected static final String TAG = "GeofenceTransitionsIS";
    DatabaseHandler databaseHandler;
    public String gaddress,gstatus,gEntryDate;
    private Credentials credentials;
    String gemail = "";
    private SharedPreferences mPref;
    private SharedPreferences.Editor mEditor;

    /**
     * This constructor is required, and calls the super IntentService(String)
     * constructor with the name for a worker thread.
     */
    public GeofenceTransitionsIntentService() {
        // Use the TAG to name the worker thread.
        super(TAG);
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    /**
     * Handles incoming intents.
     * @param intent sent by Location Services. This Intent is provided to Location
     *               Services (inside a PendingIntent) when addGeofences() is called.
     */
    @Override
    protected void onHandleIntent(Intent intent) {
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        if (geofencingEvent.hasError()) {
            String errorMessage = GeofenceErrorMessages.getErrorString(this,
                    geofencingEvent.getErrorCode());
            Log.e(TAG, errorMessage);
            return;
        }

        databaseHandler = new DatabaseHandler(this);
        mPref = getApplicationContext().getSharedPreferences("Shared", MODE_PRIVATE);
        mEditor = mPref.edit();
        credentials = new Credentials();

        // Get the transition type.
        int geofenceTransition = geofencingEvent.getGeofenceTransition();

        // Test that the reported transition was of interest.
        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER || geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {

            // Get the geofences that were triggered. A single event can trigger multiple geofences.
            List<Geofence> triggeringGeofences = geofencingEvent.getTriggeringGeofences();

            // Get the transition details as a String.
            String geofenceTransitionDetails = getGeofenceTransitionDetails(this, geofenceTransition, triggeringGeofences);

            // Send notification and log the transition details.
            if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) {
                if (!mPref.getBoolean(geofenceTransitionDetails, false)) {
                    sendNotification(geofenceTransitionDetails);
                    mEditor.putBoolean(geofenceTransitionDetails, true);
                    String [] geoFenceState = geofenceTransitionDetails.split(": ");
                    mEditor.putBoolean(getString(R.string.geofence_transition_exited) + ": " + geoFenceState[1], false);
                    mEditor.commit();
                }
            }

            if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {
                if (!mPref.getBoolean(geofenceTransitionDetails, false)) {
                    sendNotification(geofenceTransitionDetails);
                    mEditor.putBoolean(geofenceTransitionDetails, true);
                    String [] geoFenceState = geofenceTransitionDetails.split(": ");
                    mEditor.putBoolean(getString(R.string.geofence_transition_entered) + ": " + geoFenceState[1], false);
                    mEditor.commit();
                }
            }

            Log.i(TAG, geofenceTransitionDetails);
        } else {
            // Log the error.
            Log.e(TAG, getString(R.string.geofence_transition_invalid_type, geofenceTransition));
        }
    }

    /**
     * Gets transition details and returns them as a formatted string.
     *
     * @param context               The app context.
     * @param geofenceTransition    The ID of the geofence transition.
     * @param triggeringGeofences   The geofence(s) triggered.
     * @return The transition details formatted as String.
     */
    private String getGeofenceTransitionDetails(Context context, int geofenceTransition, List<Geofence> triggeringGeofences) {

        String geofenceTransitionString = getTransitionString(geofenceTransition);

        // Get the Ids of each geofence that was triggered.
        ArrayList triggeringGeofencesIdsList = new ArrayList();
        for (Geofence geofence : triggeringGeofences) {
            triggeringGeofencesIdsList.add(geofence.getRequestId());
        }
        String triggeringGeofencesIdsString = TextUtils.join(", ", triggeringGeofencesIdsList);
        Date curDate = new Date();
        SimpleDateFormat format = new SimpleDateFormat("dd-M-yyyy hh:mm:ss");
        gEntryDate = format.format(curDate);
        boolean isInserted;

        gaddress = triggeringGeofencesIdsList.toString();
        gstatus = geofenceTransitionString;
        String geoFenceDetailString = geofenceTransitionString + ": " + triggeringGeofencesIdsString;

        Pattern gmailPattern = Patterns.EMAIL_ADDRESS;
        Account[] accounts = AccountManager.get(this).getAccounts();

        for (Account account : accounts) {
            if (gmailPattern.matcher(account.name).matches()) {
                gemail = account.name;
            }
        }

        //insert into new fence breach fusion table for each entry exit //no conditions

        //When switch is ON  and we are breaching a fence (entering) we will write that data to FT When switch is ON and we are breaching fence(exiting) we will write that data to FT as well.

        //status:exit
        if (geofenceTransitionString.equalsIgnoreCase("Exited")) {
            if (mPref.getBoolean("SWITCH", false)) {
              //Switch : ON
                //if switch is ON
                String timeValue = mPref.getString("Time_Interval", "60000");
                long configurableTime = Long.parseLong(timeValue);
                mEditor.putLong("CONFIG TIME", configurableTime);
                mEditor.commit();
                Intent intent = new Intent(this, MyLocationService.class);
                startService(intent);
                if (!mPref.getBoolean(geoFenceDetailString, false)) {
                    credentials.insertIntoGeoFusionTables(this.saveGeoFile(gaddress, gstatus, gEntryDate, gemail, "geofile"));
                }


            } else {
            }

        } else if(geofenceTransitionString.equalsIgnoreCase("Entered")){//status :Entry

            if (mPref.getBoolean("SWITCH", false)) {

                if (!mPref.getBoolean(geoFenceDetailString, false)) {
                    credentials.insertIntoGeoFusionTables(this.saveGeoFile(gaddress, gstatus, gEntryDate, gemail, "geofile"));
                }

            }else{//Switch : OFF


            }
        }



        if (!mPref.getBoolean(geoFenceDetailString, false)) {
            isInserted = databaseHandler.addFenceTiming(new FenceTiming(triggeringGeofencesIdsList.toString(), geofenceTransitionString, gEntryDate));
            if (isInserted) {
                Log.e("GeofenceonsIS : ", "inserted to db");
            }
        }

        return geoFenceDetailString;
    }


    public File saveGeoFile(String address, String status, String date, String mail, String geofile) {

        String textToSave = address + "," + status + "," + date + "," + mail;
        File myFile = null;
        try {
            myFile = new File("/sdcard/" + geofile);
            myFile.createNewFile();
            FileOutputStream fOut = new FileOutputStream(myFile);
            OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);
            myOutWriter.append(textToSave);
            myOutWriter.close();
            fOut.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.e("saveGeoFile " ,textToSave);

        return myFile;
    }

    /**
     * Posts a notification in the notification bar when a transition is detected.
     * If the user clicks the notification, control goes to the MainActivity.
     */
    private void sendNotification(String notificationDetails) {
        // Create an explicit content Intent that starts the main Activity.
        Intent notificationIntent = new Intent(getApplicationContext(), MainActivity.class);

        // Construct a task stack.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);

        // Add the main Activity to the task stack as the parent.
        stackBuilder.addParentStack(MainActivity.class);

        // Push the content Intent onto the stack.
        stackBuilder.addNextIntent(notificationIntent);

        // Get a PendingIntent containing the entire back stack.
        PendingIntent notificationPendingIntent =
                stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        // Get a notification builder that's compatible with platform versions >= 4
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);

        // Define the notification settings.
        builder.setSmallIcon(R.mipmap.ic_launcher)
                // In a real app, you may want to use a library like Volley
                // to decode the Bitmap.
                .setLargeIcon(BitmapFactory.decodeResource(getResources(),
                        R.mipmap.ic_launcher))
                .setColor(Color.RED)
                .setContentTitle(notificationDetails)
                .setContentText(getString(R.string.geofence_transition_notification_text))
                .setContentIntent(notificationPendingIntent);

        // Dismiss notification once the user touches it.
        builder.setAutoCancel(true);

        // Get an instance of the Notification manager
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Issue the notification
        mNotificationManager.notify(0, builder.build());
    }

    /**
     * Maps geofence transition types to their human-readable equivalents.
     *
     * @param transitionType    A transition type constant defined in Geofence
     * @return A String indicating the type of transition
     */
    private String getTransitionString(int transitionType) {
        switch (transitionType) {
            case Geofence.GEOFENCE_TRANSITION_ENTER:
                return getString(R.string.geofence_transition_entered);
            case Geofence.GEOFENCE_TRANSITION_EXIT:
                return getString(R.string.geofence_transition_exited);
            default:
                return getString(R.string.unknown_geofence_transition);
        }
    }
}
