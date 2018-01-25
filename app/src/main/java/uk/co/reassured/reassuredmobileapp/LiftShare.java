package uk.co.reassured.reassuredmobileapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Observable;
import java.util.Timer;
import java.util.TimerTask;

import cz.msebera.android.httpclient.Header;

/**
 * Created by hfletcher on 24/01/2018.
 */

public class LiftShare extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lift_share);

        final TextView go_back = findViewById(R.id.GoBackLink);

        go_back.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                finish();
            }
        });

        getTrafficMethod();
    }

    public void getTrafficMethod(){
        String url = "http://e-guestlist.co.uk/api/traffic.txt";

        try{
            AsyncHttpClient client = new AsyncHttpClient();

            client.get(url, new AsyncHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                    String response = new String(responseBody);

                    try{
                        JSONArray TrafficInformation = new JSONArray(response);

                        int TrafficEvents = TrafficInformation.length();
                        System.out.println(TrafficEvents);

                        int TrafficEvent = 0;

                        //JSONObject CurrentEvent = new JSONObject();

                        final TextView road = (TextView)findViewById(R.id.disrupted_route);
                        final TextView severity = (TextView)findViewById(R.id.disruption_scale);
                        final TextView description = (TextView)findViewById(R.id.disruption_description);

                        int delay = -5000;

                        if(TrafficEvents > 0){
                            do{
                                final JSONObject CurrentEvent = new JSONObject(TrafficInformation.getString(TrafficEvent));

                                System.out.println(CurrentEvent);

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
                                TrafficEvent++;
                            } while (TrafficEvent < TrafficEvents);
                        } else {
                            String message1 = "Have a nice journey!";
                            String message2 = "There are no reported issues today.";

                            severity.setText(message1);
                            road.setText(message2);
                            description.setText("");
                        }



                    } catch (Exception e){
                        e.printStackTrace();
                        //Display a welcome message
                        Toast.makeText(LiftShare.this, "There was an error with getting traffic info.", Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                    //Display a welcome message
                    Toast.makeText(LiftShare.this, "There was an error with getting traffic info.", Toast.LENGTH_LONG).show();
                }
            });
        } catch (Exception e){
            e.printStackTrace();
        }

    };

    public class tasker extends AsyncTask {
        @Override
        protected Object[] doInBackground(Object[] Objects) {
            TextView affected_route = (TextView)findViewById(R.id.disrupted_route);

            return null;
        };
    }

    static SharedPreferences getSharedPreferences(Context ctx){
        return PreferenceManager.getDefaultSharedPreferences(ctx);
    }

    public static void sign_out(Context ctx){
        SharedPreferences.Editor editor = getSharedPreferences(ctx).edit();
        editor.remove("Email");
        editor.remove("Password");
        editor.commit();
    }
}