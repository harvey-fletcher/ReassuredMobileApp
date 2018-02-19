package uk.co.reassured.reassuredmobileapp;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;

/**
 * Created by hfletcher on 19/02/2018.
 */

public class MyLocationService extends Service {

    public LocationManager locationManager;
    public LocationListener mLocationListener;


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @SuppressLint("MissingPermission")
    public int onStartCommand(Intent intent, int flags, int startId){
        locationManager =  (LocationManager)getSystemService(LOCATION_SERVICE);
        mLocationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(MyLocationService.this).edit();
                editor.putString("latitude", Double.toString(location.getLatitude()));
                editor.putString("longitude", Double.toString(location.getLongitude()));
                editor.commit();
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(String s) {

            }

            @Override
            public void onProviderDisabled(String s) {

            }
        };

        try {
            locationManager.requestLocationUpdates(locationManager.getBestProvider(new Criteria(), true), 10000, 0, mLocationListener);
        } catch (Exception e){
            e.printStackTrace();
        }

        return START_STICKY;
    }

    @Override
    public void onDestroy(){
        locationManager.removeUpdates(mLocationListener);
    }
}