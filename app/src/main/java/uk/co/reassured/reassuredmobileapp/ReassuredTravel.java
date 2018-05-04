package uk.co.reassured.reassuredmobileapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.Image;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

/**
 * Created by hfletcher on 24/01/2018.
 */

public class ReassuredTravel extends AppCompatActivity {

    //ClassGlobals variables
    ClassGlobals classGlobals = new ClassGlobals();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //Load the reassured travel layout
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reassured_travel);

        //This is the "Go Back" link
        final RelativeLayout go_back = findViewById(R.id.GoBackLink);

        //Finish this activity when the "Go Back" link is clicked.
        go_back.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                finish();
            }
        });

        //This is the "I'm Late' button
        ImageView RunningLate = (ImageView)findViewById(R.id.runningLateButton);

        //What happens when the "I'm Late" button is clicked?
        RunningLate.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                JSONObject PostData = new JSONObject();

                //Send the new servicedesk request
                PerformPostRequest(new OnJSONResponseCallback() {
                    @Override
                    public JSONObject onJSONResponse(boolean success, JSONObject response) {
                        try{
                            Toast.makeText(ReassuredMobileApp.getAppContext(), response.getString("info"), Toast.LENGTH_LONG).show();
                        } catch (Exception e){
                            e.printStackTrace();
                        }
                        return null;
                    }

                }, PostData);
            }
        });

        //This is the button to open the lift sharing view
        ImageView LiftSharing = (ImageView)findViewById(R.id.carSharingButton);
        LiftSharing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ReassuredMobileApp.getAppContext(), LiftSharingView.class);
                startActivity(intent);
            }
        });

        //Run the API call
        getTrafficMethod();
    }

    public void onResume(){
        super.onResume();

        Intent LocationService = new Intent(ReassuredMobileApp.getAppContext(), MyLocationService.class);
        stopService(LocationService);
    }

    public interface OnJSONResponseCallback{
        public JSONObject onJSONResponse(boolean success, JSONObject response);
    }

    //This function performs post requests to the server
    public void PerformPostRequest(final OnJSONResponseCallback callback, JSONObject PostData) {
        //To authenticate against the API we need the user's credentials
        String Email = classGlobals.sharedPrefs().getString("Email","");
        String Password = classGlobals.sharedPrefs().getString("Password","");

        //Add the credentials to post data
        try{
            PostData.put("action", "LateNotification");
            PostData.put("email", Email);
            PostData.put("password", Password);
        } catch (Exception e){
            e.printStackTrace();
        }

        System.out.println(PostData);

        //Then we need to put the post data into request parameters so we can send them in the call.
        RequestParams RequestParameters = new RequestParams();
        RequestParameters.put("", PostData);

        //This is the client we will use to make the request.
        AsyncHttpClient client = new AsyncHttpClient();

        client.post(classGlobals.AppHost + "CarSharing.php", RequestParameters, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                try {
                    String ResponseString = new String(responseBody);
                    JSONObject ResponseObject = new JSONObject(ResponseString);
                    callback.onJSONResponse(true, ResponseObject);
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

    public void getTrafficMethod(){
        //Where is the traffic information?
        String url = classGlobals.AppHost + "traffic.txt";

        try{
            AsyncHttpClient client = new AsyncHttpClient();

            client.get(url, new AsyncHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                    //Store the response
                    String response = new String(responseBody);

                    try{
                        JSONArray TrafficInformation = new JSONArray(response);

                        //How many traffic events are there in total?
                        int TrafficEvents = TrafficInformation.length();

                        //The current event that we are displaying
                        int TrafficEvent = 0;

                        //The three areas that are populated on the traffic information
                        final TextView road = (TextView)findViewById(R.id.disrupted_route);
                        final TextView severity = (TextView)findViewById(R.id.disruption_scale);
                        final TextView description = (TextView)findViewById(R.id.disruption_description);

                        //This needs to be set to -5000 to display the first event immediately as there is a postdelay of 5000
                        int delay = -5000;

                        if(TrafficEvents > 0){
                            do{
                                //Make the current traffic event into a JSON object so that we can extract the items from it.
                                final JSONObject CurrentEvent = new JSONObject(TrafficInformation.getString(TrafficEvent));

                                //Display the next event 5 seconds after the last one was displayed.
                                road.postDelayed(new Runnable() {
                                    public void run() {
                                        try {
                                            String detail = CurrentEvent.getString("3") + " (" + CurrentEvent.getString("2") + ")";

                                            severity.setText(CurrentEvent.getString("0"));
                                            road.setText(CurrentEvent.getString("1"));
                                            description.setText(detail);
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }
                                },delay+=5000);

                                //Move to the next event
                                TrafficEvent++;
                            } while (TrafficEvent < TrafficEvents);
                        } else {
                            //Display a "Everything OK" message
                            String message1 = "Have a nice journey!";
                            String message2 = "There are no reported issues today.";

                            severity.setText(message1);
                            road.setText(message2);
                            description.setText("");
                        }



                    } catch (Exception e){
                        e.printStackTrace();
                        //Display a welcome message
                        Toast.makeText(ReassuredMobileApp.getAppContext(), "There was an error with getting traffic info.", Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                    //Display a welcome message
                    Toast.makeText(ReassuredMobileApp.getAppContext(), "There was an error with getting traffic info.", Toast.LENGTH_LONG).show();
                }
            });
        } catch (Exception e){
            e.printStackTrace();
        }

    };
}