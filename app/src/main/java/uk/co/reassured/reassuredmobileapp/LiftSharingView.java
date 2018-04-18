package uk.co.reassured.reassuredmobileapp;

import android.*;
import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.media.Image;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.SpannableString;
import android.text.style.RelativeSizeSpan;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
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

import java.text.SimpleDateFormat;
import java.util.Date;
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
        RelativeLayout GoBack = (RelativeLayout) findViewById(R.id.GoBackLink);
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

        timer.schedule(new timedTask(),15000,15000);
    }

    public void DisplayNearbyColleagues(JSONArray response){
        FindNearMeScroller.removeAllViews();

        try{
            JSONArray Results = response;

            if(Results.length() > 0){
                Toast.makeText(LiftSharingView.this, "Tap the icon to message a colleague.", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(LiftSharingView.this, "There are no colleagues nearby.", Toast.LENGTH_LONG).show();
            }

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
                params.setMargins(20,10,20, 10);

                //Apply the params to the container
                container.setLayoutParams(params);

                //Make the person name a good size
                String Name = Results.getJSONObject(i).getString("firstname") + " " + Results.getJSONObject(i).getString("lastname");
                SpannableString PersonName = new SpannableString(Name);
                PersonName.setSpan(new RelativeSizeSpan(2f),0, PersonName.length(),0);

                //The container contains a textview
                TextView resultText = new TextView(LiftSharingView.this);
                resultText.setText(PersonName);

                //Add an icon
                ImageView MessageIcon = new ImageView(LiftSharingView.this);
                MessageIcon.setBackgroundResource(R.drawable.bulletin_comment_button);

                //measure resulttext
                resultText.measure(0,0);

                //Give the message icon width and height and position
                RelativeLayout.LayoutParams ImageParams = new RelativeLayout.LayoutParams(75,75);
                ImageParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                ImageParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                ImageParams.setMargins(0,0,10,0);
                MessageIcon.setLayoutParams(ImageParams);

                //Set an action for the message icon
                MessageIcon.setOnClickListener(OnMessageButtonClick(Results.getJSONObject(i).getInt("id"), Name));

                //Add the text to the container
                container.addView(MessageIcon);
                container.addView(resultText);

                //Add the view so it can be seen
                FindNearMeScroller.addView(container);
            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    View.OnClickListener OnMessageButtonClick(final int id, final String user_name){
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try{
                    JSONObject PostData = new JSONObject();
                    PostData.put("action", "SendNewJourneyRequest");
                    PostData.put("to_user", id);
                    PerformPostRequest(new OnJSONResponseCallback() {
                        @Override
                        public JSONArray onJSONResponse(boolean success, JSONArray response) {
                            Toast.makeText(LiftSharingView.this, "A message has been sent!", Toast.LENGTH_SHORT).show();
                            return null;
                        }
                    },PostData);

                    //We need to save the message to the device so it shows up in messages and the conversation can be started
                    MyFirebaseMessagingService FCM = new MyFirebaseMessagingService();
                    SimpleDateFormat SDF = new SimpleDateFormat("H:m");
                    FCM.saveNewMessage(LiftSharingView.this, id, user_name, "Lift share conversation started!", SDF.format(new Date()), 0, 1);

                } catch (Exception e){
                    e.printStackTrace();
                }
            }
        };
    }

    public interface OnJSONResponseCallback {
        public JSONArray onJSONResponse(boolean success, JSONArray response);
    }

    public class timedTask extends TimerTask{
        @Override
        public void run() {
            runOnUiThread(new Runnable() {
                @SuppressLint("MissingPermission")
                @Override
                public void run() {
                    //get a list of users nearby
                    try{
                        //Get the device last known location
                        LocationManager myLocation = (LocationManager)getSystemService(LOCATION_SERVICE);
                        Location location = myLocation.getLastKnownLocation(myLocation.getBestProvider(new Criteria(), true));

                        JSONObject PostData = new JSONObject();
                        PostData.put("action", "FindNearMe");
                        PostData.put("latitude", location.getLatitude());
                        PostData.put("longitude", location.getLongitude());
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
                    alertDialogBuilder.setMessage("By selecting this, you agree to share your location, upon request from the server, with the server and our database.\n\nYour exact location will never be shared directly with other employees, but they will see you in a list of employees if you are within 5 miles of their location.");
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
