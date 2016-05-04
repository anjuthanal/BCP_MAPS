package com.bcp.bcp;

import android.Manifest;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.annotation.TargetApi;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.SwitchCompat;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.view.Window;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import com.bcp.bcp.beacon.BeaconstacReceiver;
import com.bcp.bcp.database.DatabaseHandler;
import com.bcp.bcp.database.FenceTiming;
import com.bcp.bcp.database.LocationData;
import com.bcp.bcp.gcm.QuickstartPreferences;
import com.bcp.bcp.gcm.RegistrationIntentService;
import com.bcp.bcp.geofencing.Constants;
import com.bcp.bcp.geofencing.GeofenceErrorMessages;
import com.bcp.bcp.geofencing.GeofenceTransitionsIntentService;
import com.bcp.bcp.recyclerview.LocationFenceTrackDetails;
import com.bcp.bcp.recyclerview.ViewLocationActivity;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.mobstac.beaconstac.core.Beaconstac;
import com.mobstac.beaconstac.core.MSConstants;
import com.mobstac.beaconstac.core.PlaceSyncReceiver;
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
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public class MainActivity extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener, LocationListener,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, ResultCallback<Status>, BeaconstacReceiver.OnRuleTriggered {
    private SwitchCompat switchCompat;
    private GoogleMap gmap;

    LocationManager locationManager;
    Criteria criteria;
    String bestProvider, timeValue;
    Location location;
    double latitude;
    double longitude;
    long millisValue;

    TextView timeText, textadd, textadd2, texttime, texttime2, nodata;
    GPSTracker gps;
    Credentials credentials;
    private SharedPreferences.Editor mEditor;
    CardView cardView;

    private static final int REQUEST_ENABLE_BT = 1;
    private static final int REQUEST_LOCATION = 2;
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private static final String TAG = "MainActivity";

    private BroadcastReceiver mRegistrationBroadcastReceiver;
    private boolean isReceiverRegistered;
    DatabaseHandler databaseHandler;
    private ArrayList<Geofence> mGeofenceList;
    private PendingIntent mGeofencePendingIntent;

    private BluetoothAdapter mBluetoothAdapter;
    private BeaconstacReceiver receiver;
    private Beaconstac bstac;

    List<FenceTiming> fenceTimingList = new ArrayList<FenceTiming>();
    List<LocationData> locationDataList = new ArrayList<LocationData>();
    List<LocationFenceTrackDetails> samplelocFenDetailses = new ArrayList<LocationFenceTrackDetails>();
    List<LocationFenceTrackDetails> locFenDetailsesforDisplay = new ArrayList<LocationFenceTrackDetails>();

    private SimpleDateFormat format;
    LocationFenceTrackDetails trackDetails;
    private SharedPreferences mSharedPreferences;


    static final long DAY = 24 * 60 * 60 * 1000;
    List<LocationFenceTrackDetails> diplayList = new ArrayList<LocationFenceTrackDetails>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        switchCompat = (SwitchCompat) findViewById(R.id.Switch);


        gps = new GPSTracker(MainActivity.this);
        mSharedPreferences = getSharedPreferences("Shared", Context.MODE_PRIVATE);
        mEditor = mSharedPreferences.edit();
        gps = new GPSTracker(this);


        cardView = (CardView) findViewById(R.id.carddb);


        textadd = (TextView) findViewById(R.id.textaddress);
        textadd2 = (TextView) findViewById(R.id.textaddress2);
        texttime = (TextView) findViewById(R.id.texttime);
        texttime2 = (TextView) findViewById(R.id.texttime2);
        nodata = (TextView) findViewById(R.id.nodata);

        samplelocFenDetailses = prepareCardDetails();
        if (samplelocFenDetailses != null && samplelocFenDetailses.size() > 0) {
            textadd.setText(samplelocFenDetailses.get(0).getAddress());
            texttime.setText(samplelocFenDetailses.get(0).getTime());
            if (samplelocFenDetailses.size() > 1) {
                textadd2.setText(samplelocFenDetailses.get(1).getAddress());
                texttime2.setText(samplelocFenDetailses.get(1).getTime());
            }

            cardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(MainActivity.this, ViewLocationActivity.class);
                    startActivity(intent);

                }
            });
        } else {

            nodata.setVisibility(View.VISIBLE);
            textadd.setVisibility(View.INVISIBLE);
            textadd2.setVisibility(View.INVISIBLE);
            texttime.setVisibility(View.INVISIBLE);
            texttime2.setVisibility(View.INVISIBLE);
        }

        if (gps.canGetLocation()) {
            latitude = gps.getLatitude();
            longitude = gps.getLongitude();

            Log.e("latitude", "" + latitude);
            Log.e("longitude", "" + longitude);

        }
        timeText = (TextView) findViewById(R.id.timeText);
        //   String timeInterval = mSharedPreferences.getString("Time_Interval", "120000");//24 hrs/1 day by default
        String timeInterval = mSharedPreferences.getString("Time_Interval", "86399999");//24 hrs/1 day by default
        millisValue = Long.parseLong(timeInterval);
        timeValue = convert(millisValue);
        timeText.setText("Refresh Time Interval : " + timeValue);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }

        switchCompat.setChecked(mSharedPreferences.getBoolean("SWITCH", false));

        credentials = new Credentials();
        // Empty list for storing geofences.
        mGeofenceList = new ArrayList<Geofence>();
        // Initially set the PendingIntent used in addGeofences() and removeGeofences() to null.
        mGeofencePendingIntent = null;

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        criteria = new Criteria();
        bestProvider = locationManager.getBestProvider(criteria, true);
        location = locationManager.getLastKnownLocation(bestProvider);
        if (location != null) {
            latitude = location.getLatitude();
            longitude = location.getLongitude();
        }


        switchCompat.setOnCheckedChangeListener(this);

        //  mRegistrationProgressBar = (ProgressBar) findViewById(R.id.registrationProgressBar);
        mRegistrationBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
//                mRegistrationProgressBar.setVisibility(ProgressBar.GONE);
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
                boolean sentToken = sharedPreferences.getBoolean(QuickstartPreferences.SENT_TOKEN_TO_SERVER, false);
                if (sentToken) {
//                    mInformationTextView.setText(getString(R.string.gcm_send_message));
                } else {
                    //                  mInformationTextView.setText(getString(R.string.token_error_message));
                }
            }
        };

        // Registering BroadcastReceiver
        registerReceiver();

        if (checkPlayServices()) {
            // Start IntentService to register this application with GCM.
            Intent intent = new Intent(this, RegistrationIntentService.class);
            startService(intent);
        }


        //DELETE 24 HRS PAST DATA FROM LOCAL DB
        databaseHandler.deletePastFenceTiming();
        databaseHandler.deletePastLocationData();

    }

    @Override
    protected void onResume() {
        super.onResume();
        receiver = new BeaconstacReceiver();
        receiver.setOnOnRuleTriggeredListener(this);

        populateGeofenceList();
        buildGoogleApiClient();

        getBluetoothAdapter();//beacons

        if (mGoogleApiClient != null && !mGoogleApiClient.isConnected())
            mGoogleApiClient.connect();
    }

    /**
     * This sample hard codes geofence data. A real app might dynamically create geofences based on
     * the user's location.
     */
    public void populateGeofenceList() {
        for (Map.Entry<String, LatLng> entry : Constants.BAY_AREA_LANDMARKS.entrySet()) {

            mGeofenceList.add(new Geofence.Builder()
                    // Set the request ID of the geofence. This is a string to identify this
                    // geofence.
                    .setRequestId(entry.getKey())

                    // Set the circular region of this geofence.
                    .setCircularRegion(
                            entry.getValue().latitude,
                            entry.getValue().longitude,
                            Constants.GEOFENCE_RADIUS_IN_METERS
                    )

                    // Set the expiration duration of the geofence. This geofence gets automatically
                    // removed after this period of time.
                    .setExpirationDuration(Constants.GEOFENCE_EXPIRATION_IN_MILLISECONDS)

                    // Set the transition types of interest. Alerts are only generated for these
                    // transition. We track entry and exit transitions in this sample.
                    .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER |
                            Geofence.GEOFENCE_TRANSITION_EXIT)

                    // Create the geofence.
                    .build());
        }
    }

    /**
     * Provides the entry point to Google Play services.
     */
    protected GoogleApiClient mGoogleApiClient;

    /**
     * Builds a GoogleApiClient. Uses the {@code #addApi} method to request the LocationServices API.
     */
    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    private void registerReceiver() {
        if (!isReceiverRegistered) {
            LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                    new IntentFilter(QuickstartPreferences.REGISTRATION_COMPLETE));
            isReceiverRegistered = true;
        }
    }

    /**
     * Check the device to make sure it has the Google Play Services APK. If
     * it doesn't, display a dialog that allows users to download the APK from
     * the Google Play Store or enable it in the device's system settings.
     */
    private boolean checkPlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(this, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST)
                        .show();
            } else {
                Log.i(TAG, "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()) {
            case R.id.Switch:
                if (isChecked) {
                    //new UploadToFTAsync(UploadToFTAsync.getConfigTime, null, this).execute();
                    mEditor.putBoolean("SWITCH", true);
                    mEditor.commit();
                } else {
                    try {
                        Intent intent = new Intent(this, MyLocationService.class);
                        stopService(intent);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    Toast.makeText(getApplicationContext(), "Your Location is default", Toast.LENGTH_LONG).show();
                    mEditor.putBoolean("SWITCH", false);
                    mEditor.commit();
                }
                break;
        }
    }


    @Override
    public void onLocationChanged(Location location) {
        LatLng latLng = new LatLng(latitude, longitude);
        gmap.addMarker(new MarkerOptions().position(latLng).title("My Location").snippet("Track Me"));
        gmap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        gmap.animateCamera(CameraUpdateFactory.zoomTo(15));

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    /**
     * Builds and returns a GeofencingRequest. Specifies the list of geofences to be monitored.
     * Also specifies how the geofence notifications are initially triggered.
     */
    private GeofencingRequest getGeofencingRequest() {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();

        // The INITIAL_TRIGGER_ENTER flag indicates that geofencing service should trigger a
        // GEOFENCE_TRANSITION_ENTER notification when the geofence is added and if the device
        // is already inside that geofence.
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);

        // Add the geofences to be monitored by geofencing service.
        builder.addGeofences(mGeofenceList);

        // Return a GeofencingRequest.
        return builder.build();
    }

    /**
     * Gets a PendingIntent to send with the request to add or remove Geofences. Location Services
     * issues the Intent inside this PendingIntent whenever a geofence transition occurs for the
     * current list of geofences.
     *
     * @return A PendingIntent for the IntentService that handles geofence transitions.
     */
    private PendingIntent getGeofencePendingIntent() {
        // Reuse the PendingIntent if we already have it.
        if (mGeofencePendingIntent != null) {
            return mGeofencePendingIntent;
        }
        Intent intent = new Intent(this, GeofenceTransitionsIntentService.class);
        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when calling
        // addGeofences() and removeGeofences().
        return PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private void logSecurityException(SecurityException securityException) {
        Log.e(TAG, "Invalid location permission. " +
                "You need to use ACCESS_FINE_LOCATION with geofences", securityException);
    }

    /**
     * Adds geofences, which sets alerts to be notified when the device enters or exits one of the
     * specified geofences. Handles the success or failure results returned by addGeofences().
     */
    public void initGeofencesHandler() {
        if (!mGoogleApiClient.isConnected()) {
            Toast.makeText(this, getString(R.string.not_connected), Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            LocationServices.GeofencingApi.addGeofences(
                    mGoogleApiClient,
                    // The GeofenceRequest object.
                    getGeofencingRequest(),
                    // A pending intent that that is reused when calling removeGeofences(). This
                    // pending intent is used to generate an intent when a matched geofence
                    // transition is observed.
                    getGeofencePendingIntent()
            ).setResultCallback(this); // Result processed in onResult().
        } catch (SecurityException securityException) {
            // Catch exception generated if the app does not use ACCESS_FINE_LOCATION permission.
            logSecurityException(securityException);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mGoogleApiClient != null)
            mGoogleApiClient.connect();
    }

   /* @Override
    protected void onStop() {
        super.onStop();
        mGoogleApiClient.disconnect();
    }*/

    /**
     * Runs when a GoogleApiClient object successfully connects.
     */
    @Override
    public void onConnected(Bundle connectionHint) {
        Log.i(TAG, "Connected to GoogleApiClient");
        initGeofencesHandler();
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        // Refer to the javadoc for ConnectionResult to see what error codes might be returned in
        // onConnectionFailed.
        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = " + result.getErrorCode());
    }

    @Override
    public void onConnectionSuspended(int cause) {
        // The connection to Google Play services was lost for some reason.
        Log.i(TAG, "Connection suspended");

        // onConnected() will be called again automatically when the service reconnects
    }

    /**
     * Runs when the result of calling addGeofences() and removeGeofences() becomes available.
     * Either method can complete successfully or with an error.
     * <p/>
     * Since this activity implements the {@link ResultCallback} interface, we are required to
     * define this method.
     *
     * @param status The Status returned through a PendingIntent when addGeofences() or
     *               removeGeofences() get called.
     */
    public void onResult(Status status) {
        if (status.isSuccess()) {
        } else {
            // Get the status code for the error and log it using a user-friendly message.
            String errorMessage = GeofenceErrorMessages.getErrorString(this,
                    status.getStatusCode());
            Log.e(TAG, errorMessage);
        }
    }

    public String convert(long miliSeconds) {
        int hrs = (int) TimeUnit.MILLISECONDS.toHours(miliSeconds) % 24;
        int min = (int) TimeUnit.MILLISECONDS.toMinutes(miliSeconds) % 60;
        int sec = (int) TimeUnit.MILLISECONDS.toSeconds(miliSeconds) % 60;
        return String.format("%02d:%02d:%02d", hrs, min, sec);
    }


    private void getBluetoothAdapter() {
        // Initializes a Bluetooth adapter.  For API level 18 and above, get a reference to
        // BluetoothAdapter through BluetoothManager.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            BluetoothManager mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            mBluetoothAdapter = mBluetoothManager.getAdapter();
        }

        // Checks if Bluetooth is supported on the device.
        if (mBluetoothAdapter == null) {
            Log.e(TAG, "Unable to obtain a BluetoothAdapter.");
            Toast.makeText(this, "Unable to obtain a BluetoothAdapter", Toast.LENGTH_LONG).show();

        } else {
            if (!mBluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }
        }

        checkLocationPermission();
    }

    /**
     * This method is called when "Scanning for beacons".
     * This method checks API version and prompts to grant camera permission if API version is above ANDROID M
     */
    @TargetApi(Build.VERSION_CODES.M)
    public void checkLocationPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            syncBeacon();
            return;
        }
        if (checkSelfPermission(ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            syncBeacon();
            return;
        }
        if (shouldShowRequestPermissionRationale(ACCESS_FINE_LOCATION)) {
            Snackbar.make(switchCompat, R.string.location_permission_rationale, Snackbar.LENGTH_INDEFINITE)
                    .setAction(android.R.string.ok, new View.OnClickListener() {
                        @Override
                        @TargetApi(Build.VERSION_CODES.M)
                        public void onClick(View v) {
                            requestPermissions(new String[]{ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
                        }
                    }).show();
        } else {
            requestPermissions(new String[]{ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
        }
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

    public void syncBeacon() {
        Toast.makeText(this, "Scanning for beacons", Toast.LENGTH_LONG).show();
        bstac = Beaconstac.getInstance(this);
        bstac.setRegionParams("F94DBB23-2266-7822-3782-57BEAC0952AC", "com.bcp.bcp");
        bstac.syncRules();
        // bstac.setActiveScanDuration(12000);

        LocationManager locManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (locManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {

            bstac.syncPlaces();

            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(MSConstants.BEACONSTAC_INTENT_PLACE_SYNC_SUCCESS);
            intentFilter.addAction(MSConstants.BEACONSTAC_INTENT_PLACE_SYNC_FAILURE);
            registerReceiver(placeSyncReceiver, intentFilter);
        } else {
            try {
                bstac.syncBeacons();
                bstac.startRangingBeacons();
            } catch (MSException e) {
                e.printStackTrace();
            }
        }
        registerBroadcast();
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

    boolean isInserted;

    /**
     * Opens a dialogFragment to display offers
     *
     * @param title Title of dialog (pass null to hide title)
     * @param text  Summary of dialog (pass null to hide summary)
     * @param url   ArrayList containing URLs of images (pass null to hide images)
     */
    String[] geoFenceState;
    String bstatus = "";
    String btitle = "";
    String bEntryDate = "";
    String gemail = "";

    private void handleBeaconData(String title, String text, ArrayList<String> url) {
        sendNotification(this, text, title);

        databaseHandler = new DatabaseHandler(this);
        FenceTiming previousFenceEntry = databaseHandler.getFenceTimingByAddress(title + ", " + text + "(B)");

        Date currentEntryDate = new Date();
        SimpleDateFormat format = new SimpleDateFormat(Constants.TIME_FORMAT);
        bEntryDate = format.format(currentEntryDate);
        Date previousEntryDate = null;
        long timeStampDifference = 0;

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

        if (timeStampDifference < Constants.TIMESTAMP_DIFF) {
            bstatus = "Entered: " + title + ", " + text;
            btitle = title;

            isInserted = databaseHandler.addFenceTiming(new FenceTiming(title + ", " + text + "(B)", bstatus, bEntryDate));
            if (isInserted) {
                Log.e("GeofenceonsIS : ", "inserted to local fence db");
            }

            Pattern gmailPattern = Patterns.EMAIL_ADDRESS;
            Account[] accounts = AccountManager.get(this).getAccounts();

            for (Account account : accounts) {
                if (gmailPattern.matcher(account.name).matches()) {
                    gemail = account.name;
                }
            }

            if (switchCompat.isChecked()) {
                InsertFutionTable asyncFT = new InsertFutionTable();
                asyncFT.execute();
            }
        } else {
            Log.e(TAG, "false: " + bstatus);
            bstatus = "Exited: " + title + ", " + text;
            databaseHandler.updateFenceEntryStatus(previousFenceEntry.getDatetime(), bstatus);
        }
    }

    class InsertFutionTable extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            credentials.insertIntoGeoFusionTables(MainActivity.saveGeoFile(btitle, geoFenceState[0], bEntryDate, gemail, "geofile"));
            return null;
        }
    }

    public static File saveGeoFile(String address, String status, String date, String mail, String geofile) {

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

    public void setIsPopupVisible(boolean isPopupVisible) {
        this.isPopupVisible = isPopupVisible;
    }

    private boolean isPopupVisible;

    public static String getYoutubeVideoId(String videoUrl) {
        if (videoUrl == null || videoUrl.trim().length() <= 0)
            return null;

        Pattern pattern = Pattern.compile(Constants.youtubeURLPattern, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(videoUrl);

        if (matcher.find())
            return matcher.group(1);
        return null;
    }

    public void stopScanning() {
        /*
        if (bstac != null) {
            try {
                bstac.stopRangingBeacons();
            } catch (MSException e) {
                e.printStackTrace();
            }
        }
        */
    }

    @Override
    public void onTriggeredRule(Context context, String ruleName, ArrayList<MSAction> actions) {

        {
            HashMap<String, Object> messageMap;
            for (MSAction action : actions) {
                messageMap = action.getMessage();
                switch (action.getType()) {
                    case MSActionTypePopup:
                        if (!isPopupVisible) {
                            handleBeaconData(ruleName, (String) messageMap.get("text"), null);
                        }
                        break;

                    case MSActionTypeCard:
                        if (!isPopupVisible) {
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
                                    m = card.getMediaArray().get(0);
                                    src = m.getMediaUrl().toString();

                                    // handle custom url types
                                    String ytId = getYoutubeVideoId(src);
                                    if (ytId != null) {
//                                    showYoutubePopup(ytId, ok_label, ok_action);
                                    } else {
                                        dialog = new android.app.AlertDialog.Builder(context);
                                        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                                            @Override
                                            public void onDismiss(DialogInterface dialog) {
                                                isPopupVisible = false;
                                            }
                                        });
                                        final WebView webView = new WebView(context);
                                        webView.getSettings().setJavaScriptEnabled(true);
                                        webView.setWebViewClient(new WebViewClient());
                                        webView.loadUrl(src);
                                        dialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                dialog.dismiss();
                                                try {
                                                    Uri uri = Uri.parse("http://"); // missing 'http://' will cause crashed
                                                    Intent openUrl = new Intent(Intent.ACTION_VIEW, uri);
                                                    openUrl.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                                    startActivity(openUrl);
                                                } catch (Exception e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                        });

                                        dialog.setView(webView);
                                        dialog.setNeutralButton(getString(R.string.button_close), null);
                                        dialog.show();
                                    }

                                    break;
                            }
                        }
                        break;

                    case MSActionTypeWebpage:
                        if (!isPopupVisible) {
                            final android.app.AlertDialog.Builder dialog = new android.app.AlertDialog.Builder(context);
                            dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                                @Override
                                public void onDismiss(DialogInterface dialog) {
                                    isPopupVisible = false;
                                }
                            });

                            final WebView webView = new WebView(context);
                            webView.getSettings().setJavaScriptEnabled(true);
                            webView.setWebViewClient(new WebViewClient());
                            webView.loadUrl(messageMap.get("url").toString());

                            dialog.setView(webView);
                            dialog.setPositiveButton("Close", null);
                            dialog.show();
                        }
                        break;

                    case MSActionTypeCustom:
                        MSLogger.log("Card id: " + action.getActionID());
                        break;
                }
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopScanning();
    }

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
            notificationManager.notify(1, mBuilder.build());
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            if (receiver != null) unregisterReceiver(receiver);
            if (placeSyncReceiver != null) unregisterReceiver(placeSyncReceiver);
        } catch (IllegalArgumentException e) {

        }
        stopScanning();
    }


    class CallsComp implements Comparator<LocationFenceTrackDetails> {

        @Override
        public int compare(LocationFenceTrackDetails lhs, LocationFenceTrackDetails rhs) {
            try {
                Date lhsDate = format.parse(lhs.getTime());
                Date rhsDate = format.parse(rhs.getTime());
                return lhsDate.compareTo(rhsDate);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return 0;
        }
    }

    long dbmilli = 0;
    long cyurrDatemilli = 0;

    public List<LocationFenceTrackDetails> getDataToDisplay(List<LocationFenceTrackDetails> samplelocFenDetailses) {
        diplayList = new ArrayList<>();

        for (LocationFenceTrackDetails fenceTrackDetails : samplelocFenDetailses) {
            try {
                Date dateFromDb = format.parse(fenceTrackDetails.getTime());
                dbmilli = dateFromDb.getTime();
                cyurrDatemilli = new Date().getTime();
                if (dbmilli > cyurrDatemilli - DAY) {

                    diplayList.add(fenceTrackDetails);
                } else {

                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        Collections.reverse(diplayList);
        Log.e("dbmilli", ":" + ":" + dbmilli);
        Log.e("cyurrDatemilli", ":" + cyurrDatemilli);
        return diplayList;
    }


    private List<LocationFenceTrackDetails> prepareCardDetails() {

        databaseHandler = new DatabaseHandler(this);
        fenceTimingList.addAll(databaseHandler.getAllFenceTiming());
        locationDataList.addAll(databaseHandler.getAllLocationData());

        if (fenceTimingList != null || locationDataList != null) {
            for (FenceTiming fenceTiming : fenceTimingList) {
                trackDetails = new LocationFenceTrackDetails();
                trackDetails.setAddress(fenceTiming.getFenceAddress());
                trackDetails.setStatus(fenceTiming.getStatus());
                trackDetails.setTime(fenceTiming.getDatetime());
                samplelocFenDetailses.add(trackDetails);
            }
            for (LocationData locationData : locationDataList) {
                trackDetails = new LocationFenceTrackDetails();
                trackDetails.setAddress(locationData.getLocAddress());
                trackDetails.setStatus("");
                trackDetails.setTime(locationData.getLocDatetime());
                samplelocFenDetailses.add(trackDetails);
            }
            if (samplelocFenDetailses != null) {
                Collections.sort(samplelocFenDetailses, new CallsComp());

            }
        }
        //Log.e("samplelocFenDetailses size" ,":"+samplelocFenDetailses.size());
        locFenDetailsesforDisplay = getDataToDisplay(samplelocFenDetailses);
        // Log.e("locFenDetailsesforDisplay size" ,":"+locFenDetailsesforDisplay.size());
        Collections.reverse(samplelocFenDetailses);
        return samplelocFenDetailses;
    }
}
