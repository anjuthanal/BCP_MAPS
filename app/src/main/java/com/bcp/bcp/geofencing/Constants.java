/**
 * Copyright 2014 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.bcp.bcp.geofencing;

import com.google.android.gms.maps.model.LatLng;

import java.util.HashMap;

/**
 * Constants used in this sample.
 */
public final class Constants {

    private Constants() {
    }

    /**
     * Used to set an expiration time for a geofence. After this amount of time Location Services
     * stops tracking the geofence.
     */
    public static final long GEOFENCE_EXPIRATION_IN_HOURS = 12;

    /**
     * For this sample, geofences expire after twelve hours.
     */
    public static final long GEOFENCE_EXPIRATION_IN_MILLISECONDS =
            GEOFENCE_EXPIRATION_IN_HOURS * 60 * 60 * 1000;
    public static final float GEOFENCE_RADIUS_IN_METERS = 50; // 1 mile, 1.6 km

    /**
     * Map for storing information about CTS Premises
     */
    public static final HashMap<String, LatLng> BAY_AREA_LANDMARKS = new HashMap<String, LatLng>();
    static {
        // San Francisco International Airport.
        BAY_AREA_LANDMARKS.put("Google Office", new LatLng(17.4596285, 78.372763));
        BAY_AREA_LANDMARKS.put("Cognizant PKN office", new LatLng(12.948016, 80.209280));
        BAY_AREA_LANDMARKS.put("Cognizant CKC office", new LatLng(12.913377, 80.219067));

    }
}