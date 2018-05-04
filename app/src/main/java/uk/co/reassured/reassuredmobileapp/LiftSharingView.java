package uk.co.reassured.reassuredmobileapp;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
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
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Timer;

import cz.msebera.android.httpclient.Header;

/**
 * Created by hfletcher on 19/02/2018.
 */

public class LiftSharingView extends AppCompatActivity {

    //This is the classglobals file
    ClassGlobals classGlobals = new ClassGlobals();

    //This is the relative layout (inside the scrollview which can only hold 1 child) which all the user responses will be put into.
    public static RelativeLayout FindNearMeScroller;

    //This is what gets used for the Asynchronous response of the users for carsharing
    public static int ContainerId = 1;

    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lift_sharing);

        //This is the scroller for results
        FindNearMeScroller = findViewById(R.id.FindNearMeScroller);

        //There needs to be a button so the user can go back
        RelativeLayout GoBack = (RelativeLayout) findViewById(R.id.GoBackLink);
        GoBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        //First, check if location is enabled
        CheckLocationPermissions();

        //Set the switch from preferences
        ShareSwitch();

        //now that we sent a request for devices to send us their most up-to-date location, we want to wait for a few seconds so that they have enough time to do this
        //First set a "Loading" message so the user doesn't think it's broken
        String loading = "Finding nearby colleagues. Please wait.\n \n \n This could take up to 30 seconds depending on your connection.";
        TextView LoadingText = new TextView(ReassuredMobileApp.getAppContext());
        LoadingText.setText(loading);
        LoadingText.setTextSize(20);
        FindNearMeScroller.addView(LoadingText);

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
    }

    public void DisplayAsyncResponse(final JSONObject messageData){
        //If this is the first result to come back, clear everything from the container
        if(ContainerId == 1){
            FindNearMeScroller.removeAllViews();
        }

        //A result that comes back will need a container to go in
        RelativeLayout Container = new RelativeLayout(ReassuredMobileApp.getAppContext());

        //Give that container an ID
        Container.setId(ContainerId);

        //To do size and position, we need parameters
        RelativeLayout.LayoutParams ContainerParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);

        //If this is not the first container, place it below the last one
        if(ContainerId != 1){
            ContainerParams.addRule(RelativeLayout.BELOW, ContainerId - 1);
        }

        //Within the container is a textview with the colleage name, and an icon to message them
        TextView ColleagueName = new TextView(ReassuredMobileApp.getAppContext());
        ImageView MessageIcon = new ImageView(ReassuredMobileApp.getAppContext());

        //We need to set a spannable string
        SpannableString PersonName;

        //Set up the message icon
        MessageIcon.setBackgroundResource(R.drawable.bulletin_comment_button);

        //Give the message icon width and height and position
        RelativeLayout.LayoutParams ImageParams = new RelativeLayout.LayoutParams(75,75);
        ImageParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        ImageParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        ImageParams.setMargins(0,0,10,0);
        MessageIcon.setLayoutParams(ImageParams);

        try {
            //Make the person name a good size
            PersonName = new SpannableString(messageData.getString("name"));
            PersonName.setSpan(new RelativeSizeSpan(1.5f), 0, PersonName.length(), 0);

            //Set an action for the message icon
            MessageIcon.setOnClickListener(OnMessageButtonClick(messageData.getInt("user_id"), messageData.getString("name")));
        } catch (Exception e){
            ContainerId++;
            return;
        }

        //Set the text to the textbox
        ColleagueName.setText(PersonName);

        //Add the text and the message icon to the container
        Container.addView(ColleagueName);
        Container.addView(MessageIcon);

        //Apply the parameters to the container
        Container.setLayoutParams(ContainerParams);

        //Add the container to the main view
        FindNearMeScroller.addView(Container);

        //increment to next container ID
        ContainerId++;
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
                            Toast.makeText(ReassuredMobileApp.getAppContext(), "A message has been sent!", Toast.LENGTH_SHORT).show();
                            return null;
                        }
                    },PostData);
                } catch (Exception e){
                    e.printStackTrace();
                }
            }
        };
    }

    public interface OnJSONResponseCallback {
        public JSONArray onJSONResponse(boolean success, JSONArray response);
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
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(ReassuredMobileApp.getAppContext());
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

        client.post(classGlobals.AppHost + "CarSharing.php", RequestParameters, new AsyncHttpResponseHandler() {
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
                    Toast.makeText(ReassuredMobileApp.getAppContext(), "Error: " + statusCode, Toast.LENGTH_LONG).show();
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
                    Toast.makeText(ReassuredMobileApp.getAppContext(), "You must enable location to use this feature.", Toast.LENGTH_LONG).show();
                    finish();
                }
            });
            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();
        } else {
            Intent locationService = new Intent(ReassuredMobileApp.getAppContext(), MyLocationService.class);
            startService(locationService);
        }
    }

    public SharedPreferences sharedPrefs(){
        return PreferenceManager.getDefaultSharedPreferences(ReassuredMobileApp.getAppContext());
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        CheckLocationPermissions();
    }
}
