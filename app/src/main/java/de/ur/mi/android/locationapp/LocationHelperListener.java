package de.ur.mi.android.locationapp;

public interface LocationHelperListener {

    void onLocationChanged(double latitude, double longitude);

    void onLocationAddressChanged(String address);

}
