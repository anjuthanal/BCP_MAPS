package com.bcp.bcp.beacon;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.bcp.bcp.MainActivity;
import com.bcp.bcp.R;
import com.mobstac.beaconstac.core.MSPlace;
import com.mobstac.beaconstac.models.MSAction;
import com.mobstac.beaconstac.models.MSBeacon;

import java.util.ArrayList;


public class BeaconstacReceiver extends com.mobstac.beaconstac.core.BeaconstacReceiver {

    NotificationManager notificationManager;

    @Override
    public void exitedBeacon(Context context, MSBeacon beacon) {
        Log.v(BeaconstacReceiver.class.getName(), "exited called " + beacon.getBeaconKey());
//        sendNotification(context, "Exited " + beacon.getMajor() + " : " + beacon.getMinor());
    }

    public interface OnRuleTriggered {
        public void onTriggeredRule(Context context, String ruleName, ArrayList<MSAction> actions);
    }

    private OnRuleTriggered listener = null;

    public void setOnOnRuleTriggeredListener(Context context) {
        this.listener = (OnRuleTriggered) context;
    }

    @Override
    public void rangedBeacons(Context context, ArrayList<MSBeacon> beacons) {
        Log.v(BeaconstacReceiver.class.getName(), "Ranged called " + beacons.size());
        sendNotification(context, "Ranged " + beacons.size() + " beacons");
    }

    @Override
    public void campedOnBeacon(Context context, MSBeacon beacon) {
        Log.v(BeaconstacReceiver.class.getName(), "camped on called " + beacon.getBeaconKey());
//        sendNotification(context, "Camped " + beacon.getMajor() + " : " + beacon.getMinor());
    }

    @Override
    public void triggeredRule(Context context, String ruleName, ArrayList<MSAction> actions) {
        if (listener != null) {
            Log.e(BeaconstacReceiver.class.getName(), "triggered rule called " + ruleName + " with " + actions.size() + " actions");
            listener.onTriggeredRule(context, ruleName, actions);
//            sendNotification(context, ruleName);
        }
    }

    @Override
    public void enteredRegion(Context context, String region) {
        Log.v(BeaconstacReceiver.class.getName(), "Entered region " + region);
    }

    @Override
    public void exitedRegion(Context context, String region) {
        notificationManager = (NotificationManager)
                context.getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancelAll();
        Log.v(BeaconstacReceiver.class.getName(), "Exited region " + region);
    }

    @Override
    public void enteredGeofence(Context context, ArrayList<MSPlace> places) {
        notificationManager = (NotificationManager)
                context.getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancelAll();
        Log.v(BeaconstacReceiver.class.getName(), "Entered geofence " + places.get(0).getName() + "");
    }

    @Override
    public void exitedGeofence(Context context, ArrayList<MSPlace> places) {
        notificationManager = (NotificationManager)
                context.getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancelAll();
        Log.v(BeaconstacReceiver.class.getName(), "Exited geofence " + places.get(0).getName() + "");

    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
    }

    private void sendNotification(Context context, String text) {
        if (context != null) {
            Intent activityIntent = new Intent(context.getApplicationContext(), MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(
                    context.getApplicationContext(),
                    0,
                    activityIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT
            );

            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context.getApplicationContext())
                    .setContentTitle(text)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentIntent(pendingIntent);
            notificationManager = (NotificationManager)
                    context.getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(1, mBuilder.build());
        }
    }
}
