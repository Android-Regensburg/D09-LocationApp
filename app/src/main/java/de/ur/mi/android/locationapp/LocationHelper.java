package de.ur.mi.android.locationapp;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;

public class LocationHelper implements LocationListener {

    private static final int LOCATION_UPDATE_INTERVAL_IN_MS = 5000;
    private static final int LOCATION_UPDATE_THRESHOLD_DISTANCE_IN_METERS = 1;
    private static final int MAX_GEO_CODING_RESULTS = 1;

    private Context context;
    private LocationHelperListener listener;
    private LocationManager locationManager;
    private boolean useGeoCoding = false;

    public LocationHelper(Context context, LocationHelperListener listener) {
        this.context = context;
        this.listener = listener;
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
    }

    private String getLocationProviderID(LocationManager locationManager) {
        // Android sucht sich nach diesen Kriterien den passenden Provider für die Location-Updates aus
        // Wenn Sie hier "falsche" Angaben machen, erhalten Sie später ggf. nicht die notwendigen oder nur ungenaue Positionen
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        criteria.setPowerRequirement(Criteria.POWER_HIGH);
        criteria.setAltitudeRequired(true);
        criteria.setCostAllowed(true);
        return locationManager.getBestProvider(criteria, true);
    }

    public void enableLocationTracking() {
        String locationProvide = getLocationProviderID(locationManager);
        // Diesen Permission-Check können Sie automatisch von Android Studio generieren lassen, in dem Sie die Funktion des
        // LocationManager im Code nutzen und anschließend automatisch den Fehler korrigieren lassen, den die IDE anzeigt
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        locationManager.requestLocationUpdates(locationProvide, LOCATION_UPDATE_INTERVAL_IN_MS, LOCATION_UPDATE_THRESHOLD_DISTANCE_IN_METERS, this);
    }

    private boolean permissionsWereGranted() {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return false;
        }
        return true;
    }

    public void disableLocationTracking() {
        locationManager.removeUpdates(this);
    }

    public void enableGeoCoding() {
        useGeoCoding = true;
    }

    public void disableGeoCoding() {
        useGeoCoding = false;
    }

    @Override
    public void onLocationChanged(Location location) {
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();
        listener.onLocationChanged(latitude, longitude);
        if(useGeoCoding) {
            GetGeoCodedLocationTask task = new GetGeoCodedLocationTask(location, listener);
            Executors.newSingleThreadExecutor().submit(task);
        }
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

    private class GetGeoCodedLocationTask implements Runnable {

        private final Location location;
        private final LocationHelperListener listener;

        public GetGeoCodedLocationTask(Location location, LocationHelperListener listener) {
            this.location = location;
            this.listener = listener;
        }

        @Override
        public void run() {
            List<Address> addresses = null;
            Geocoder geocoder = new Geocoder(context, Locale.getDefault());
            try {
                addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), MAX_GEO_CODING_RESULTS);
                Address firstResult = addresses.get(0);
                StringBuilder builder = new StringBuilder();
                for(int i = 0; i <= firstResult.getMaxAddressLineIndex(); i++ ) {
                    builder.append(firstResult.getAddressLine(i));
                    if(i < firstResult.getMaxAddressLineIndex()) {
                        builder.append(System.lineSeparator());
                    }
                }
                listener.onLocationAddressChanged(builder.toString());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
