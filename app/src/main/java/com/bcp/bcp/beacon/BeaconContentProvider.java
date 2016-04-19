package com.bcp.bcp.beacon;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

import com.mobstac.beaconstac.provider.MSContentProvider;

public class BeaconContentProvider extends MSContentProvider {
    public BeaconContentProvider() {
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return super.delete(uri, selection, selectionArgs);
    }

    @Override
    public String getType(Uri uri) {
        return super.getType(uri);
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        return super.insert(uri, values);
    }

    @Override
    public boolean onCreate() {
        return super.onCreate();
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        return super.query(uri, projection, selection, selectionArgs, sortOrder);
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        return super.update(uri, values, selection, selectionArgs);
    }
}
