package uk.co.reassured.reassuredmobileapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.Image;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

/**
 * Created by hfletcher on 24/01/2018.
 */

public class ReassuredTravel extends AppCompatActivity {

    //Where is the app API hosted?
    private String AppHost = "http://82.10.188.99/api/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //Load the reassured travel layout
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reassured_travel);

        //This is the "Go Back" link
        final TextView go_back = findViewById(R.id.GoBackLink);

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
                sendLateAlert();
            }
        });

        //This is the button to open the lift sharing view
        ImageView LiftSharing = (ImageView)findViewById(R.id.carSharingButton);
        LiftSharing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ReassuredTravel.this, LiftSharingView.class);
                startActivity(intent);
            }
        });

        //Run the API call
        getTrafficMethod();
    }

    public void onResume(){
        super.onResume();

        Intent LocationService = new Intent(ReassuredTravel.this, MyLocationService.class);
        stopService(LocationService);
    }

    public void sendLateAlert(){
        int user_id = get_user_id(ReassuredTravel.this);
        int team_id = getTeamId(ReassuredTravel.this);

        String url = AppHost + "notifications.php?email=" + getEmail(ReassuredTravel.this) + "&password=" + getPassword(ReassuredTravel.this) + "&to_group=team&notification_type=late&user_id=" + user_id + "&team_id=" + team_id;
        System.out.println(url);

        try{
            AsyncHttpClient client = new AsyncHttpClient();

            client.get(url, new AsyncHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                    //Store the response
                    String response = new String(responseBody);

                    try{
                        JSONArray LateStatusResponseArray = new JSONArray(response);
                        JSONObject LateStatusResponse = LateStatusResponseArray.getJSONObject(0);

                        if(LateStatusResponse.getString("status").matches("200")){
                            Toast.makeText(ReassuredTravel.this, "Your team has been informed. Thanks.", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(ReassuredTravel.this, "There was an unexpected error: " + LateStatusResponse.getString("status"), Toast.LENGTH_LONG).show();
                        }

                    } catch (Exception e){
                        e.printStackTrace();
                        //Display a error message
                        Toast.makeText(ReassuredTravel.this, "There was an error with the late notification.", Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                    //Display a failure message
                    Toast.makeText(ReassuredTravel.this, "There was an error with the late notification.", Toast.LENGTH_LONG).show();
                }
            });

        } catch (Exception e){
            e.printStackTrace();
        }

    };

    public void getTrafficMethod(){
        //Where is the traffic information?
        String url = AppHost + "traffic.txt";

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
                        Toast.makeText(ReassuredTravel.this, "There was an error with getting traffic info.", Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                    //Display a welcome message
                    Toast.makeText(ReassuredTravel.this, "There was an error with getting traffic info.", Toast.LENGTH_LONG).show();
                }
            });
        } catch (Exception e){
            e.printStackTrace();
        }

    };

    public static SharedPreferences getSharedPreferences(Context ctx){
        return PreferenceManager.getDefaultSharedPreferences(ctx);
    }

    public static int get_user_id(Context ctx)
    {
        return getSharedPreferences(ctx).getInt("id", 0);
    }

    public static String getEmail(Context ctx)
    {
        return getSharedPreferences(ctx).getString("Email", "");
    }

    public static String getPassword(Context ctx)
    {
        return getSharedPreferences(ctx).getString("Password", "");
    }
    public static String getFirstName(Context ctx)
    {
        return getSharedPreferences(ctx).getString("firstname", "");
    }

    public static String getLastName(Context ctx)
    {
        return getSharedPreferences(ctx).getString("lastname", "");
    }

    public static int getTeamId(Context ctx)
    {
        return getSharedPreferences(ctx).getInt("team_id", 0);
    }

    public static String getLocationId(Context ctx)
    {
        return getSharedPreferences(ctx).getString("location_id", "");
    }
}