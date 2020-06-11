package de.ur.mi.android.locationapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import java.security.Permission;
import java.text.DecimalFormat;

public class LocationActivity extends AppCompatActivity implements LocationHelperListener {

    private static final int REQUEST_LOCATION_PERMISSIONS = 10;
    private LocationHelper locationHelper;
    private TextView currentPositionText;
    private TextView currentAddressText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initUI();
        initLocationHelper();
    }

    private void initLocationHelper() {
        locationHelper = new LocationHelper(this, this);
        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
        // Hier fragen wir explizit die NutzerInnen nach der Erlaubnis, auf die Location-Daten zuzugreifen
        // Android blendet darauf hin einen Dialog ein, in dem die NutzerInnen die Anfrage bestätigen oder ablehnen können
        // Das Ergebnis erhalten wir über die Callback-Methode on onRequestPermissionsResult übergeben
        requestPermissions(permissions, REQUEST_LOCATION_PERMISSIONS);
    }

    private void initUI() {
        setContentView(R.layout.activity_location);
        currentPositionText = findViewById(R.id.current_position_text);
        currentAddressText = findViewById(R.id.current_address_text);
        Switch toggleLocationUpdatesSwitch = findViewById(R.id.postion_switch);
        Switch toggleAddressUpdatesSwitch = findViewById(R.id.geocoding_switch);
        toggleLocationUpdatesSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                onLocationFeatureToggled(isChecked);
            }
        });
        toggleAddressUpdatesSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                onGeoCodingFeatureToggled(isChecked);
            }
        });
        toggleLocationUpdatesSwitch.setEnabled(false);
        toggleAddressUpdatesSwitch.setEnabled(false);
    }

    private void enableSwitchButtons() {
        Switch toggleLocationUpdatesSwitch = findViewById(R.id.postion_switch);
        Switch toggleAddressUpdatesSwitch = findViewById(R.id.geocoding_switch);
        toggleLocationUpdatesSwitch.setEnabled(true);
        toggleAddressUpdatesSwitch.setEnabled(true);
    }

    private void onLocationFeatureToggled(boolean isNowEnabled) {
        if(isNowEnabled) {
            locationHelper.enableLocationTracking();
        } else {
            locationHelper.disableLocationTracking();
        }
    }

    private void onGeoCodingFeatureToggled(boolean isNowEnabled) {
        if(isNowEnabled) {
            locationHelper.enableGeoCoding();
        } else {
            locationHelper.disableGeoCoding();
        }
    }

    private void setLocation(double latitude, double longitude) {
        DecimalFormat df = new DecimalFormat("#.###");
        String locationText = String.format("Sie befinden sich an %s Breite und %s Länge.", df.format(latitude), df.format(longitude));
        currentPositionText.setText(locationText);
    }

    private void setAddress(String address) {
        String addressText = String.format("Sie befinden sich ungefähr an diesem Ort:%s%s", System.lineSeparator(), address);
        currentAddressText.setText(addressText);
    }

    @Override
    public void onLocationChanged(final double latitude, final double longitude) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                setLocation(latitude, longitude);
            }
        });
    }

    @Override
    public void onLocationAddressChanged(final String address) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                setAddress(address);
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch(requestCode) {
            // Wir prüfen, ob unserer Bitte, auf die Location-Daten zuzugreifen erfolgreich war
            case REQUEST_LOCATION_PERMISSIONS:
                // Wenn die NutzerInnen den Dialog verwendet haben und uns die angefordertenRechte eingeräumt haben ...
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // ... aktivieren wir die Schaltflächen, mit denen die Location-Funktionen eingeschaltet werden können
                    enableSwitchButtons();
                }
                break;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}
