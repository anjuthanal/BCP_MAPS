package com.bcp.bcp.beacon;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.android.volley.NetworkResponse;
import com.bcp.bcp.Credentials;
import com.bcp.bcp.MainActivity;
import com.bcp.bcp.R;
import com.bcp.bcp.database.DatabaseHandler;
import com.bcp.bcp.database.FenceTiming;
import com.bcp.bcp.geofencing.Constants;
import com.mobstac.beaconstac.core.Beaconstac;
import com.mobstac.beaconstac.core.MSConstants;
import com.mobstac.beaconstac.core.PlaceSyncReceiver;
import com.mobstac.beaconstac.core.Webhook;
import com.mobstac.beaconstac.models.MSAction;
import com.mobstac.beaconstac.models.MSCard;
import com.mobstac.beaconstac.models.MSMedia;
import com.mobstac.beaconstac.utils.MSException;
import com.mobstac.beaconstac.utils.MSLogger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.regex.Pattern;

public class ScanBeacons extends Service implements BeaconstacReceiver.OnRuleTriggered {

    private static final String TAG = ScanBeacons.class.getSimpleName();
    private Credentials credentials;
    private SharedPreferences mSharedPreferences;
    private BeaconstacReceiver receiver;
    private Beaconstac bstac;
    private BluetoothAdapter mBluetoothAdapter;
    private Handler handler;
    private Runnable runnable;

    public ScanBeacons() {
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.e("Service stopped", "Service stopped");
        try {
            handler.removeCallbacks(runnable);
        } catch (Exception e) {
            e.printStackTrace();
        }
        ;
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        mSharedPreferences = getSharedPreferences("Shared", Context.MODE_PRIVATE);
        credentials = new Credentials();
        handler = new Handler();

        runnable = new Runnable() {
            @Override
            public void run() {
                handler.postDelayed(this, 12000);
                scanForBeacons();
            }
        };

        handler.post(runnable);

        return super.onStartCommand(intent, flags, startId);
    }

    public void scanForBeacons() {
        Log.e(TAG, "scanForBeacons: ");
        receiver = new BeaconstacReceiver();
        receiver.setOnOnRuleTriggeredListener(this);

        // Initializes a Bluetooth adapter.  For API level 18 and above, get a reference to
        // BluetoothAdapter through BluetoothManager.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            BluetoothManager mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            mBluetoothAdapter = mBluetoothManager.getAdapter();
        }

        // Checks if Bluetooth is supported on the device.
        if (mBluetoothAdapter != null) {
            if (mBluetoothAdapter.isEnabled()) {
                bstac = Beaconstac.getInstance(this);
                bstac.setRegionParams("F94DBB23-2266-7822-3782-57BEAC0952AC", "com.bcp.bcp");
                bstac.syncRules();


//         if location is enabled
                LocationManager locManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                if (locManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                        locManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {

                    bstac.syncPlaces();


                    IntentFilter intentFilter = new IntentFilter();
                    intentFilter.addAction(MSConstants.BEACONSTAC_INTENT_PLACE_SYNC_SUCCESS);
                    intentFilter.addAction(MSConstants.BEACONSTAC_INTENT_PLACE_SYNC_FAILURE);

                    registerReceiver(placeSyncReceiver, intentFilter);

                } else {

                    try {
                        bstac.startRangingBeacons();
                    } catch (MSException e) {
                        e.printStackTrace();
                    }
                }
                registerBroadcast();
            }
        }
    }

    private void registerBroadcast() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(MSConstants.BEACONSTAC_INTENT_RANGED_BEACON);
        intentFilter.addAction(MSConstants.BEACONSTAC_INTENT_CAMPED_BEACON);
        intentFilter.addAction(MSConstants.BEACONSTAC_INTENT_EXITED_BEACON);
        intentFilter.addAction(MSConstants.BEACONSTAC_INTENT_RULE_TRIGGERED);
        intentFilter.addAction(MSConstants.BEACONSTAC_INTENT_ENTERED_REGION);
        intentFilter.addAction(MSConstants.BEACONSTAC_INTENT_EXITED_REGION);
        registerReceiver(receiver, intentFilter);
    }

    PlaceSyncReceiver placeSyncReceiver = new PlaceSyncReceiver() {

        @Override
        public void onSuccess(Context context) {
            bstac.enableGeofences(true);

            // start ranging
            try {
                bstac.startRangingBeacons();
            } catch (MSException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onFailure(Context context) {
            MSLogger.error("Error syncing geofence");
        }

    };

    private void sendNotification(Context context, String text, String title) {
        if (context != null) {
            Intent activityIntent = new Intent(context.getApplicationContext(), MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(
                    context.getApplicationContext(),
                    0,
                    activityIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT
            );

            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context.getApplicationContext())
                    .setContentTitle(title)
                    .setContentText(text)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentIntent(pendingIntent);
            NotificationManager notificationManager = (NotificationManager)
                    context.getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
            Notification notification = mBuilder.build();
            notification.flags = Notification.DEFAULT_LIGHTS | Notification.FLAG_AUTO_CANCEL;
            notificationManager.notify(1, notification);
        }
    }

    private void handleBeaconData(String title, String text, ArrayList<String> url) {
        Log.e(TAG, "handleBeaconData: Servicew");
        sendNotification(this, text, title);

        DatabaseHandler databaseHandler = new DatabaseHandler(this);
        FenceTiming previousFenceEntry = databaseHandler.getFenceTimingByAddress(title + ", " + text + "(B)");

        Date currentEntryDate = new Date();
        SimpleDateFormat format = new SimpleDateFormat(Constants.TIME_FORMAT);
        String bEntryDate = format.format(currentEntryDate);
        Date previousEntryDate = null;
        long timeStampDifference = 0;
        String geofenceAddress = title + " " + text + "(B)";

        if (previousFenceEntry != null) {
            try {
                previousEntryDate = format.parse(TextUtils.isEmpty(previousFenceEntry.getDatetime()) ? "" : previousFenceEntry.getDatetime());
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        if (previousEntryDate != null) {
            timeStampDifference = currentEntryDate.getTime() - previousEntryDate.getTime();
        }

        if (previousFenceEntry != null && previousFenceEntry.getStatus().contains("Exited")) {
            timeStampDifference = 0;
        }

        String bstatus = "";
        String geoFenceState;
        String gemail = "";

        Pattern gmailPattern = Patterns.EMAIL_ADDRESS;
        Account[] accounts = AccountManager.get(this).getAccounts();

        for (Account account : accounts) {
            if (gmailPattern.matcher(account.name).matches()) {
                gemail = account.name;
            }
        }

        if (timeStampDifference < Constants.TIMESTAMP_DIFF) {
            bstatus = "Entered: " + title + ", " + text;
            databaseHandler.addFenceTiming(new FenceTiming(title + ", " + text + "(B)", bstatus, bEntryDate));
            geoFenceState = "Entered " ;

            if (mSharedPreferences.getBoolean("SWITCH", false)) {
                InsertFutionTable asyncFT = new InsertFutionTable(geofenceAddress, geoFenceState, bEntryDate, gemail);
                // google office(B), 2nd floor, entered/exited, time, mail
                asyncFT.execute();
            }
        } else {
            geoFenceState = "Exited ";
            bstatus = "Exited: " + title + ", " + text;
            databaseHandler.updateFenceEntryStatus(previousFenceEntry.getDatetime(), bstatus);

            bstatus = "Entered: " + title + ", " + text;
            databaseHandler.addFenceTiming(new FenceTiming(title + ", " + text + "(B)", bstatus, bEntryDate));

            bEntryDate = format.format(currentEntryDate.getTime() + 1000);
            bstatus = "Exited: " + title + ", " + text;
            databaseHandler.addFenceTiming(new FenceTiming(title + ", " + text + "(B)", bstatus, bEntryDate));

            if (mSharedPreferences.getBoolean("SWITCH", false)) {
                InsertFutionTable asyncFT = new InsertFutionTable(geofenceAddress, geoFenceState, bEntryDate, gemail);
                // google office(B), 2nd floor, entered/exited, time, mail
                asyncFT.execute();
            }
        }


        try {
            bstac.stopRangingBeacons();
        } catch (MSException e) {
            e.printStackTrace();
        }
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
        Log.e("saveGeoFile ", textToSave);

        return myFile;
    }

    class InsertFutionTable extends AsyncTask<String, Void, Void> {
        String bstatus;
        String geoFenceState;
        String bEntryDate;
        String gemail;

        public InsertFutionTable(String bstatus, String geoFenceState, String bEntryDate, String gemail) {
            this.bstatus = bstatus;
            this.geoFenceState = geoFenceState;
            this.bEntryDate = bEntryDate;
            this.gemail = gemail;
        }

        @Override
        protected Void doInBackground(String... params) {
            credentials.insertIntoGeoFusionTables(saveGeoFile(bstatus, geoFenceState, bEntryDate, gemail, "geofile"));
            return null;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onTriggeredRule(Context context, String ruleName, ArrayList<MSAction> actions) {
        HashMap<String, Object> messageMap;
        for (MSAction action : actions) {
            messageMap = action.getMessage();
            switch (action.getType()) {
                case MSActionTypePopup:
                    handleBeaconData(ruleName, (String) messageMap.get("text"), null);
                    break;
                case MSActionTypeCard:
                    MSCard card = (MSCard) messageMap.get("card");
                    MSMedia m;
                    String src;
                    android.app.AlertDialog.Builder dialog;

                    String title = ruleName;

                    switch (card.getType()) {
                        case MSCardTypePhoto:
                            ArrayList<String> urls = new ArrayList<>();
                            for (int i = 0; i < card.getMediaArray().size(); i++) {
                                m = card.getMediaArray().get(i);
                                src = m.getMediaUrl().toString();
                                urls.add(src);
                            }
                            handleBeaconData(title, null, urls);
                            break;
                        case MSCardTypeSummary:
                            ArrayList<String> cardUrls = new ArrayList<>();
                            for (int i = 0; i < card.getMediaArray().size(); i++) {
                                m = card.getMediaArray().get(i);
                                src = m.getMediaUrl().toString();
                                cardUrls.add(src);
                            }
                            handleBeaconData(card.getTitle(), card.getBody(), cardUrls);
                            break;
                        case MSCardTypeMedia:
                            break;
                    }
                    break;
                case MSActionTypeWebpage:
                    final android.app.AlertDialog.Builder webDialog = new android.app.AlertDialog.Builder(context);

                    final WebView webView = new WebView(context);
                    webView.getSettings().setJavaScriptEnabled(true);
                    webView.setWebViewClient(new WebViewClient());
                    webView.loadUrl(messageMap.get("url").toString());

                    webDialog.setView(webView);
                    webDialog.setPositiveButton("Close", null);
                    webDialog.show();
                    break;

                case MSActionTypeCustom:
                    MSLogger.log("Card id: " + action.getActionID());
                    break;
            }
        }
    }
}
