package com.betosoft.databases26;

import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.SystemClock;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.location.LocationManagerCompat;
import androidx.core.util.Consumer;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements LocationListener {
    private TextView txtLatitud, txtLongitud, txtProveedor, txtAccuracy, txtTTFF, txtChProveedor;
    private Button btnShow,btnProvider;
    private LocationManager locationManager;
    private String lat, lon;
    private long uptimeToResume;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txtLatitud = findViewById(R.id.txtLatitud);
        txtLongitud = findViewById(R.id.txtLongitud);
        txtProveedor = findViewById(R.id.txtProveedor);
        txtAccuracy = findViewById(R.id.txtAccuracy);
        txtTTFF = findViewById(R.id.txtTTFF);
        txtChProveedor = findViewById(R.id.txtChProveedor);

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        btnShow = findViewById(R.id.btnShow);
        btnShow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Uri uri = Uri.parse("geo:" + lat + "," + lon);
                startActivity(new Intent(Intent.ACTION_VIEW, uri));
            }
        });
        btnProvider = findViewById(R.id.btnProvider);
        btnProvider.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
            }
        });
    }

    @Override
    public void onProviderEnabled(@NonNull String provider) {
        LocationListener.super.onProviderEnabled(provider);
    }


    @Override
    public void onLocationChanged(@NonNull Location location) {
        Locale locale = Locale.getDefault();
        lat = String.format(locale, "%.5f", location.getLatitude());
        lon = String.format(locale, "%.5f", location.getLongitude());
        txtLatitud.setText(lat);
        txtLongitud.setText(lon);
        txtProveedor.setText( String.valueOf( location.getProvider() ) );
        txtAccuracy.setText( String.valueOf(location.getAccuracy() ) + " " + getResources().getString(R.string.metersUnit) );
        long timeToFix = (SystemClock.uptimeMillis() - uptimeToResume);
        txtTTFF.setText(String.format(
                locale,
                "%.3f %s",
                timeToFix / 1000.0,
                getString(R.string.secondsUnit)
        ));
    }

    @Override
    protected void onResume() {
        super.onResume();
        StringBuffer stringBuffer = new StringBuffer();
        List<String> enabledProviders = new ArrayList<>();
        uptimeToResume = SystemClock.uptimeMillis();
        txtTTFF.setText(R.string.secondsUnit);
        if(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            enabledProviders.add(LocationManager.GPS_PROVIDER);
        }
        if(locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            enabledProviders.add(LocationManager.NETWORK_PROVIDER);
        }
        if (enabledProviders == null) {
            txtProveedor.setText("");
        } else {
            for (String enabledProvider : enabledProviders) {
                stringBuffer.append(enabledProvider).append(" ");
                try {
                    LocationManagerCompat.getCurrentLocation(locationManager, enabledProvider, (CancellationSignal) null, ContextCompat.getMainExecutor(this), new Consumer<Location>() {
                        @Override
                        public void accept(Location location) {
                            if (location  != null) {
                                MainActivity.this.onLocationChanged(location);
                            }
                        }
                    });
                } catch (SecurityException | IllegalArgumentException ex) {
                    ex.printStackTrace();
                }

            }
            txtProveedor.setText(stringBuffer);
            txtLatitud.setText("");
            txtLongitud.setText("");
            txtAccuracy.setText(R.string.metersUnit);
            long timetoFix = SystemClock.uptimeMillis() - uptimeToResume;

        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        try {
            locationManager.removeUpdates(this);
        } catch (SecurityException ex) {
            throw new RuntimeException(ex);
        }
    }
}