package uk.co.reassured.reassuredmobileapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.json.JSONObject;

import java.net.URLEncoder;

import cz.msebera.android.httpclient.Header;

/**
 * Created by Harvey on 27/01/2018.
 */

public class AddCalendarEvent extends AppCompatActivity {

    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_calendar_event);

        //Set up items on the page.
        final TextView go_back = findViewById(R.id.GoBackLink);
        final Button addEventButton = findViewById(R.id.addEventButton);

        go_back.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                finish();
            }
        });

        addEventButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                addEventButton.setVisibility(View.INVISIBLE);
                CheckForm();
            }
        });

    }

    public void CheckForm(){
        Button addEventButton = findViewById(R.id.addEventButton);
        EditText EventName = (EditText)findViewById(R.id.eventName);
        EditText EventLocation = (EditText)findViewById(R.id.eventLocation);
        EditText EventStart = (EditText)findViewById(R.id.eventStart);
        EditText EventEnd = (EditText)findViewById(R.id.eventEnd);
        EditText EventInformation = (EditText)findViewById(R.id.eventInformation);

        String reason = "";

        if(EventName.getText().toString().length() == 0){
            reason = "Event name can't be blank.";
        } else if(EventLocation.getText().toString().length() == 0){
            reason = "Event Location can't be blank.";
        } else if(EventInformation.getText().toString().length() == 0){
            reason = "Event information can't be blank.";
        } else if(EventStart.getText().toString().length() == 0){
            reason = "Event start date can't be blank.";
        } else if(EventEnd.getText().toString().length() == 0){
            reason = "Event end date can't be blank.";
        } else if(
                (!EventStart.getText().toString().substring(2,3).contains(".") || !EventStart.getText().toString().substring(5,6).contains("."))
                        && (!EventStart.getText().toString().substring(2,3).contains("/") || !EventStart.getText().toString().substring(5,6).contains("/"))
                        && (!EventStart.getText().toString().substring(2,3).contains("-") || !EventStart.getText().toString().substring(5,6).contains("-"))
                        && (!EventStart.getText().toString().substring(2,3).contains(".") || !EventStart.getText().toString().substring(5,6).contains("."))
                ){
            reason = "Start date is not a valid date";
        }
        else if(
                (!EventEnd.getText().toString().substring(2,3).contains(".") || !EventEnd.getText().toString().substring(5,6).contains("."))
                        && (!EventEnd.getText().toString().substring(2,3).contains("/") || !EventEnd.getText().toString().substring(5,6).contains("/"))
                        && (!EventEnd.getText().toString().substring(2,3).contains("-") || !EventEnd.getText().toString().substring(5,6).contains("-"))
                        && (!EventEnd.getText().toString().substring(2,3).contains(".") || !EventEnd.getText().toString().substring(5,6).contains("."))
                ){
            reason = "End date is not a valid date";
        }

        if(reason.length() == 0){
            EventCleanup("event_name=" + EventName.getText().toString() + "&event_location=" + EventLocation.getText().toString() + "&event_organiser=" + get_user_id(AddCalendarEvent.this) + "&event_start=" + EventStart.getText().toString() + "&event_end=" + EventEnd.getText().toString() + "&event_information=" + EventInformation.getText().toString());
        } else {
            Toast.makeText(AddCalendarEvent.this, reason, Toast.LENGTH_LONG).show();
        }

        addEventButton.setVisibility(View.VISIBLE);
    }

    public void EventCleanup(String details){
        String sendTo = "http://e-guestlist.co.uk/api/calendar.php?add=true&email=" + getEmail(AddCalendarEvent.this) + "&password=" + getPassword(AddCalendarEvent.this) + "&" + details ;
        sendTo = URLEncoder.encode(sendTo);
        try{
            addNewEvent(sendTo);
        } catch (Exception e){
            Toast.makeText(AddCalendarEvent.this, "There was an unexpected error.", Toast.LENGTH_LONG).show();
        }
    }

    public void addNewEvent(String APIstub){
        AsyncHttpClient client = new AsyncHttpClient();
        client.get(APIstub, new AsyncHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                //Put the responsebody into HRF
                String responseString = new String(responseBody);

                //Convert that to a JSON object so we can see if it was successful or not
                try{
                    JSONObject responseObject = new JSONObject(responseString);

                    if(responseObject.getString("status").matches("200")){
                        //Let the user know the event was added.
                        Toast.makeText(AddCalendarEvent.this, "Event added.", Toast.LENGTH_LONG).show();
                        finish();
                    } else {
                        //Let the user know the event was not added and why.
                        Toast.makeText(AddCalendarEvent.this, "Event not added because " + responseObject.getString("reason"), Toast.LENGTH_LONG).show();
                    }
                } catch (Exception e){
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                //Let the user know the event was not added and why.
                Toast.makeText(AddCalendarEvent.this, "Event not added. Please check you are connected to the internet.", Toast.LENGTH_LONG).show();
            }
        });
    }

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

    public static int getLocationId(Context ctx)
    {
        return getSharedPreferences(ctx).getInt("location_id",0);
    }
}
