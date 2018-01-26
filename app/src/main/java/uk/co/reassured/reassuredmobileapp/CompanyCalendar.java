package uk.co.reassured.reassuredmobileapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.Image;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.DateFormatSymbols;
import java.util.Calendar;
import java.util.Date;

import cz.msebera.android.httpclient.Header;

/**
 * Created by hfletcher on 24/01/2018.
 */

public class CompanyCalendar extends AppCompatActivity {

    public int ViewMonth;
    public int ViewYear;

    Calendar calendar = Calendar.getInstance();
    DateFormatSymbols dfs = new DateFormatSymbols();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_company_calendar);

        //Pre populate date range to current month
        setMonthText(calendar.get(Calendar.MONTH), calendar.get(Calendar.YEAR));

        //Set up items on the page.
        final TextView go_back = findViewById(R.id.GoBackLink);
        final ImageView next_month = findViewById(R.id.monthNext);
        final ImageView previous_month = findViewById(R.id.monthPrevious);

        go_back.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                finish();
            }
        });

        next_month.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                moveToNext();
            }
        });

        previous_month.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                moveToLast();
            }
        });


    }

    public void moveToNext(){
        //If the month is december, go to January of the next year
        if(ViewMonth == 11){
            ViewMonth = 0;
            ViewYear++;
        } else {
            ViewMonth++;
        }

        //Change the displayed text
        setMonthText(ViewMonth, ViewYear);
    }

    public void moveToLast(){
        //If the month is december, go to January of the next year
        if(ViewMonth == 0){
            ViewMonth = 11;
            ViewYear--;
        } else {
            ViewMonth--;
        }

        //Change the displayed text
        setMonthText(ViewMonth, ViewYear);
    }

    public void setMonthText(int month, int year){
        //The field to update
        TextView DateRange = (TextView)findViewById(R.id.DateRange);

        //Build the required date
        String[] months = dfs.getMonths();
        String monthWord = months[month];

        //Put the date in the field
        DateRange.setText(monthWord + " " + year);
        DateRange.refreshDrawableState();

        //Set the month currently under view
        ViewMonth = month;

        //Set the current year under view
        ViewYear = year;

        //Do the API call to get the data
        getEvents();
    }

    public void getEvents(){

        String fetchMonth;

        if(ViewMonth < 10){
            fetchMonth = "0" + Integer.toString(ViewMonth + 1);
        } else {
            fetchMonth = Integer.toString(ViewMonth + 1);
        }

        //Where is the API?
        String url = "http://e-guestlist.co.uk/api/calendar.php?list=true&start=" + ViewYear + "-" + fetchMonth + "-01&end=" + ViewYear + "-" + fetchMonth + "-31";

        //Go get the data from the URL
        AsyncHttpClient client = new AsyncHttpClient();
        client.get(url, new AsyncHttpResponseHandler() {

            @Override
            public void onStart() {
                // called before request is started
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] response) {
                // called when response HTTP status is "200 OK"
                String EventsResponseDetails = new String(response);

                try {
                    //The textview to update
                    TextView eventsBlock = (TextView)findViewById(R.id.eventsBlock);

                    JSONArray EventsArray = new JSONArray(EventsResponseDetails);
                    int NumEvents = EventsArray.length();
                    int EventNum = 0;

                    if(NumEvents > 0){
                        eventsBlock.setText("");

                        do {
                            JSONObject Event = new JSONObject(EventsArray.getString(EventNum));

                            System.out.println(Event.getString("event_name"));
                            System.out.println(Event.getString("event_start"));
                            System.out.println(Event.getString("event_end"));
                            System.out.println(Event.getString("event_organiser"));

                            eventsBlock.append(Event.getString("event_name") + "\n" + Event.getString("event_organiser") + "\n" + Event.getString("event_start") +"\n \n \n \n");

                            EventNum++;
                        } while (EventNum < NumEvents);
                    } else {
                        eventsBlock.setText("There are no events this month.");
                    }
                } catch (Exception E) {
                    System.out.println("Error");
                }

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] errorResponse, Throwable e) {
                // called when response HTTP status is "4XX" (eg. 401, 403, 404)
                new AlertDialog.Builder(CompanyCalendar.this)
                        .setMessage("Something went wrong. Please try again. Error: " + statusCode)
                        .setNegativeButton("OK", null)
                        .create()
                        .show();
            }

            @Override
            public void onRetry(int retryNo) {
                // called when request is retried
            }
        });
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
