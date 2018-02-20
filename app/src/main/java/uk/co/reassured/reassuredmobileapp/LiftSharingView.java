package uk.co.reassured.reassuredmobileapp;

import android.*;
import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;
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

public class LiftSharingView extends AppCompatActivity {

    public String AppHost = "http://rmobileapp.co.uk/";

    public RelativeLayout FindNearMeScroller;

    public Timer timer = new Timer();

    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lift_sharing);

        //There needs to be a button so the user can go back
        TextView GoBack = (TextView)findViewById(R.id.GoBackLink);
        GoBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        //This is the scroller for results
        FindNearMeScroller = findViewById(R.id.FindNearMeScroller);

        //First, check if location is enabled
        CheckLocationPermissions();

        //Set the switch from preferences
        ShareSwitch();

        //Request all other device locations to send to the server
        try{
            JSONObject PostData = new JSONObject();
            PostData.put("action","RequestAll");
            PerformPostRequest(new OnJSONResponseCallback() {
                @Override
                public JSONArray onJSONResponse(boolean success, JSONArray response) {
                    return null;
                }
            },PostData);
        } catch (Exception e){
            e.printStackTrace();
        }

        //now that we sent a request for devices to send us their most up-to-date location, we want to wait for a few seconds so that they have enough time to do this
        //First set a "Loading" message so the user doesn't think it's broken
        String loading = "Finding nearby colleagues. Please wait.\n \n \n This could take up to 30 seconds depending on your connection.";
        TextView LoadingText = new TextView(LiftSharingView.this);
        LoadingText.setText(loading);
        LoadingText.setTextSize(20);
        FindNearMeScroller.addView(LoadingText);

        timer.schedule(new timedTask(),7500,7500);
    }

    public void DisplayNearbyColleagues(JSONArray response){
        FindNearMeScroller.removeAllViews();

        try{
            JSONArray Results = new JSONArray(response.getJSONObject(0).getString("results"));
            for(int i=0;i<Results.length();i++){
                //Each result needs a container
                RelativeLayout container = new RelativeLayout(LiftSharingView.this);

                //All the containers have parameters
                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);

                //The container will need an ID
                container.setId(i+1);

                if(i>0){
                    params.addRule(RelativeLayout.BELOW, i);
                }

                //Space the containers apart
                params.setMargins(10,10,10, 10);

                //Apply the params to the container
                container.setLayoutParams(params);

                //The container contains a textview
                TextView resultText = new TextView(LiftSharingView.this);
                resultText.setText(Results.getJSONObject(i).toString());

                //Add the text to the container
                container.addView(resultText);

                //Add the view so it can be seen
                FindNearMeScroller.addView(container);
            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public class timedTask extends TimerTask{
        @Override
        public void run() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    //get a list of users nearby
                    try{
                        JSONObject PostData = new JSONObject();
                        PostData.put("action", "FindNearMe");
                        PostData.put("latitude", sharedPrefs().getString("latitude",""));
                        PostData.put("longitude", sharedPrefs().getString("longitude",""));
                        PerformPostRequest(new OnJSONResponseCallback() {
                            @Override
                            public JSONArray onJSONResponse(boolean success, JSONArray response) {
                                DisplayNearbyColleagues(response);
                                return null;
                            }
                        },PostData);
                    } catch (Exception e){
                        e.printStackTrace();
                    }
                    timer.cancel();
                }
            });
        }
    }

    public void ShareSwitch(){
        //This is the switch
        final Switch SharingSwitch = (Switch)findViewById(R.id.OfferCarShareSwitch);

        //Load from prefs
        boolean isset = false;
        try{
            isset = sharedPrefs().getBoolean("ShareLocation", false);
        } catch (Exception e){
            e.printStackTrace();
        }

        //Set the default state
        if(isset){
            SharingSwitch.setChecked(true);
        }

        //When the switch is clicked, update to the right state in the shared prefs
        SharingSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final SharedPreferences.Editor editor = sharedPrefs().edit();

                if(SharingSwitch.isChecked()){
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(LiftSharingView.this);
                    alertDialogBuilder.setTitle("Information");
                    alertDialogBuilder.setMessage("By selecting this, you agree to share your location, upon request from the server, with the server and the google maps location service.\n\nYour exact location will never be shared directly with other employees, but they will see you in a list of employees within 5 miles.");
                    alertDialogBuilder.setPositiveButton("I agree.", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            return;
                        }
                    });
                    alertDialogBuilder.setNegativeButton("I do not agree.", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            SharingSwitch.setChecked(false);
                        }
                    });
                    AlertDialog alertDialog = alertDialogBuilder.create();
                    alertDialog.show();
                }
                editor.putBoolean("ShareLocation", SharingSwitch.isChecked());
                editor.commit();
            }
        });
    }

    public interface OnJSONResponseCallback {
        public JSONArray onJSONResponse(boolean success, JSONArray response);
    }

    public void PerformPostRequest(final OnJSONResponseCallback callback, JSONObject PostData) {
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
                    JSONArray ResponseArray = new JSONArray(ResponseString);
                    callback.onJSONResponse(true, ResponseArray);
                } catch (Exception e) {
                    Log.e("Exception", "JSONException on success: " + e.toString());
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                try {
                    Toast.makeText(LiftSharingView.this, "Error: " + statusCode, Toast.LENGTH_LONG).show();
                } catch (Exception e) {
                    Log.e("Exception", "JSONException on failure: " + e.toString());
                }
            }
        });
    }

    public void CheckLocationPermissions(){
        boolean has_permission = (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED);

        //If we don't have the permission, request it.
        if(!has_permission){
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
            alertDialogBuilder.setTitle("Permission Required");
            alertDialogBuilder.setMessage("This feature requires access to your location in order to find nearby lift sharing.\n \nYour exact location will not be shared with other employees, however they will be able to see you in a list of employees within 5 miles, your location will also be shared with our server and the google location service.\n\nYou will now be asked for permission.");
            alertDialogBuilder.setPositiveButton("Proceed", new DialogInterface.OnClickListener(){
                public void onClick(DialogInterface dialogInterface, int id){
                    ActivityCompat.requestPermissions(LiftSharingView.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 100);
                }
            });
            alertDialogBuilder.setNegativeButton("No Thanks", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    Toast.makeText(LiftSharingView.this, "You must enable location to use this feature.", Toast.LENGTH_LONG).show();
                    finish();
                }
            });
            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();
        } else {
            Intent locationService = new Intent(LiftSharingView.this, MyLocationService.class);
            startService(locationService);
        }
    }

    public SharedPreferences sharedPrefs(){
        return PreferenceManager.getDefaultSharedPreferences(LiftSharingView.this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        CheckLocationPermissions();
    }
}
