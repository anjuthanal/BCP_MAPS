package com.bcp.bcp.beacon;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

import com.mobstac.beaconstac.core.Beaconstac;
import com.mobstac.beaconstac.core.PlaceSyncReceiver;
import com.mobstac.beaconstac.utils.MSException;
import com.mobstac.beaconstac.utils.MSLogger;

public class ScanBeacons extends Service {

    private BeaconstacReceiver receiver;
    private Beaconstac bstac;
    private BluetoothAdapter mBluetoothAdapter;

    public ScanBeacons() {
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        return 0;
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

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
