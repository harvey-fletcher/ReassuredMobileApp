package uk.co.reassured.reassuredmobileapp;

import android.annotation.SuppressLint;
import android.app.Activity;
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
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Timer;
import java.util.TimerTask;

import cz.msebera.android.httpclient.Header;

/**
 * Created by hfletcher on 19/02/2018.
 */

public class MyLocationService extends Service {

    public String AppHost = "http://rmobileapp.co.uk/";

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
            public void onLocationChanged(Location location){
                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(MyLocationService.this).edit();
                String latitude = Double.toString(location.getLatitude());
                String longitude = Double.toString(location.getLongitude());
                editor.commit();

                JSONObject PostData = new JSONObject();

                try{
                    PostData.put("action", "SendLocation");
                    PostData.put("latitude", latitude);
                    PostData.put("longitude", longitude);

                    Boolean ShareRealLocation = sharedPrefs().getBoolean("ShareLocation", false);

                    if(ShareRealLocation) {
                        PostData.put("show", 1);
                    } else {
                        PostData.put("show", 0);
                    }
                } catch (Exception e){
                    e.printStackTrace();
                }

                PerformPostRequest(new OnJSONResponseCallback() {
                    @Override
                    public JSONObject onJSONResponse(boolean success, JSONObject response) {
                        return null;
                    }
                }, PostData);

                locationManager.removeUpdates(this);
                MyLocationService.this.stopSelf();
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

        System.out.println("Location got changed");

        try {
            locationManager.requestLocationUpdates(locationManager.getBestProvider(new Criteria(), true), 0, 0, mLocationListener);
        } catch (Exception e){
            e.printStackTrace();
        }

        return START_STICKY;
    }

    public interface OnJSONResponseCallback {
        public JSONObject onJSONResponse(boolean success, JSONObject response);
    }

    public SharedPreferences sharedPrefs(){
        return PreferenceManager.getDefaultSharedPreferences(MyLocationService.this);
    }

    public void PerformPostRequest(final MyLocationService.OnJSONResponseCallback callback, JSONObject PostData) {
        //To authenticate against the API we need the user's credentials
        String Email = sharedPrefs().getString("Email","");
        String Password = sharedPrefs().getString("Password","");

        //Add the credentials to post data
        try{
            PostData.put("email", Email);
            PostData.put("password", Password);
        } catch (Exception e){
            e.printStackTrace();
        }

        //Then we need to put the post data into request parameters so we can send them in the call.
        RequestParams RequestParameters = new RequestParams();
        RequestParameters.put("data", PostData);

        //This is the client we will use to make the request.
        AsyncHttpClient client = new AsyncHttpClient();

        client.post(AppHost + "CarSharing.php", RequestParameters, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                try {
                    String ResponseString = new String(responseBody);
                    System.out.println("RESPONSE STRING \n \n" + ResponseString + "\n \n");
                    JSONObject ResponseObject = new JSONObject(ResponseString);
                    callback.onJSONResponse(true, ResponseObject);
                } catch (Exception e) {
                    Log.e("Exception", "JSONException on success: " + e.toString());
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                try {
                    Toast.makeText(MyLocationService.this, "Error: " + statusCode, Toast.LENGTH_LONG).show();
                } catch (Exception e) {
                    Log.e("Exception", "JSONException on failure: " + e.toString());
                }
            }
        });
    }
}