package uk.co.reassured.reassuredmobileapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Display;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.DateFormatSymbols;
import java.util.Calendar;

import cz.msebera.android.httpclient.Header;

/**
 * Created by hfletcher on 24/01/2018.
 */

public class ManageCalendarEvents extends AppCompatActivity {

    //Where is the app API hosted?
    private String AppHost = "http://82.10.188.99/api/";

    public String[] suffixes = new String[]{ "th", "st", "nd", "rd", "th", "th", "th", "th", "th", "th" };
    public int ViewMonth;
    public int ViewYear;
    int from_record = 0;

    //View more  / less buttons
    TextView MR;
    TextView LR;

    //View for events
    RelativeLayout MB;

    //The current screen size
    Display display;
    Point size;
    int TotalScreenHeight;
    int TotalScreenWidth;

    Calendar calendar = Calendar.getInstance();
    DateFormatSymbols dfs = new DateFormatSymbols();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_events);

        //Actually set the buttons now the content is loaded
        MR = (TextView)findViewById(R.id.showMore);
        LR = (TextView)findViewById(R.id.showLess);

        //Hide them
        MR.setVisibility(View.INVISIBLE);
        LR.setVisibility(View.INVISIBLE);

        //This is the view where the events go
        MB = (RelativeLayout)findViewById(R.id.mainBody);

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

        MR.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view){
                DisplayMoreRecords();
            }
        });

        LR.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view){
                DisplayFewerRecords();
            }
        });


        System.out.println(getTeamId(ManageCalendarEvents.this));
        if((getTeamId(ManageCalendarEvents.this) == 1) || (getTeamId(ManageCalendarEvents.this) == 2) || (getTeamId(ManageCalendarEvents.this) == 3)){
            Button addEventButton = (Button)findViewById(R.id.addEvent);
            addEventButton.setVisibility(View.VISIBLE);
            addEventButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    Intent addEvent = new Intent(ManageCalendarEvents.this, AddCalendarEvent.class);
                    startActivity(addEvent);
                }
            });
        }
    }

    public void moveToNext(){
        //If the month is december, go to January of the next year
        if(ViewMonth == 11){
            ViewMonth = 0;
            ViewYear++;
        } else {
            ViewMonth++;
        }

        //Reset from_record to 0
        from_record = 0;

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

        //Reset from_record to 0
        from_record = 0;

        //Change the displayed text
        setMonthText(ViewMonth, ViewYear);
    }

    public void setMonthText(int month, int year){
        //Clear the current month view
        MB.removeAllViews();

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

        //Reset from_record to 0
        from_record = 0;

        //Change the displayed text
        getEvents();
    }

    public void DisplayMoreRecords(){
        MR.setText("Loading...");
        from_record = from_record + 4;
        getEvents();
    }

    public void DisplayFewerRecords(){
        if(from_record != 0) {
            LR.setText("Loading...");
            from_record = from_record - 4;
            getEvents();
        }
    }

    public void getEvents(){
        //The string which stores the API friendly version of the month
        String fetchMonth;

        //Set up the months so that the API can work with them.
        if(ViewMonth < 10){
            fetchMonth = "0" + Integer.toString(ViewMonth + 1);
        } else {
            fetchMonth = Integer.toString(ViewMonth + 1);
        }

        //Where is the API?
        String url = AppHost + "calendar.php?list=true&start=" + ViewYear + "-" + fetchMonth + "-01&end=" + ViewYear + "-" + fetchMonth + "-31&from_result=" + from_record;

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
                    //Build a JSONArray from the API response
                    JSONArray EventsArray = new JSONArray(EventsResponseDetails);

                    //How many events are there?
                    int NumEvents = EventsArray.length();

                    //Set this for looping through events.
                    int EventNum = 0;

                    //How tall is the screen (helps to display the events better on small screen devices)
                    display = getWindowManager().getDefaultDisplay();
                    size = new Point();
                    display.getSize(size);
                    TotalScreenHeight = size.y;
                    TotalScreenWidth = size.x;

                    //Where are we placing the first event in the panel?
                    int DefaultDatePosition = (TotalScreenHeight / 6);
                    int DefaultDetailsPosition = ((TotalScreenHeight / 6)+ 45);

                    //Clear the views
                    MB.removeAllViews();

                    if(NumEvents > 0){
                        do {
                            //Get the current event from the array
                            JSONObject Event = new JSONObject(EventsArray.getString(EventNum));

                            //What day of the month is the event?
                            String DayOfMonth = Event.getString("event_start").substring(8,10);

                            //Suffix with st, nd, rd, th appropriately
                            if(Integer.parseInt(DayOfMonth) == 11 || Integer.parseInt(DayOfMonth) == 12 || Integer.parseInt(DayOfMonth) == 13){
                                DayOfMonth = DayOfMonth + "th";
                            } else {
                                DayOfMonth = DayOfMonth + suffixes[Integer.parseInt(DayOfMonth) % 10];
                            }

                            //Remove leading 0 from date
                            if(DayOfMonth.substring(0,1).matches("0")){
                                DayOfMonth = DayOfMonth.substring(1, DayOfMonth.length());
                            }

                            //Add the new event's date
                            TextView EventDate = new TextView(ManageCalendarEvents.this);
                            EventDate.setText(DayOfMonth + " - " + Event.getString("event_name") );
                            EventDate.setTextSize(TotalScreenHeight / 50);
                            EventDate.setY(DefaultDatePosition);
                            MB.addView(EventDate);
                            MB.refreshDrawableState();

                            //Add the new event's details
                            TextView EventDetails = new TextView(ManageCalendarEvents.this);
                            EventDetails.setText(Event.getString("event_organiser") + "\n" + Event.getString("event_information"));
                            EventDetails.setTextSize(TotalScreenHeight / 60);
                            EventDetails.setY(DefaultDetailsPosition);
                            MB.addView(EventDetails);
                            MB.refreshDrawableState();

                            //Add a delete link
                            TextView DeleteLink = new TextView(ManageCalendarEvents.this);
                            DeleteLink.setText("DELETE");
                            DeleteLink.setTextSize(15);
                            DeleteLink.setX(TotalScreenWidth - 125);
                            DeleteLink.setY(DefaultDatePosition + 10);
                            MB.addView(DeleteLink);
                            MB.refreshDrawableState();

                            //Set that button up so it does something
                            DeleteLink.setOnClickListener(getOnClickDoSomething(DeleteLink, Event.getInt("id")));

                            //The Y axis position of the next text box
                            DefaultDatePosition = DefaultDatePosition + (TotalScreenHeight / 7);
                            DefaultDetailsPosition = DefaultDetailsPosition + (TotalScreenHeight / 7);

                            EventNum ++;
                        } while ((EventNum < NumEvents) && EventNum < 4);
                    } else {
                        TextView NewText = new TextView(ManageCalendarEvents.this);
                        NewText.setText("\n \n \n There are no events this month");
                        NewText.setY(DefaultDatePosition);
                        NewText.setX(20);
                        MB.addView(NewText);
                        MB.refreshDrawableState();
                    }

                    //Max events per page is 4 for any device, if there are more in the results, disply the more button
                    if(NumEvents > 4){
                        MR.setVisibility(View.VISIBLE);
                    } else {
                        MR.setVisibility(View.INVISIBLE);
                    }

                    //If we are on page 0, hide the "less" button
                    if(from_record != 0){
                        LR.setVisibility(View.VISIBLE);
                    } else {
                        LR.setVisibility(View.INVISIBLE);
                    }

                    //Now that the result is displayed, reset "more" and "less" buttons back to the right text other than "Loading..."
                    MR.setText("More");
                    LR.setText("Less");
                } catch (Exception E) {
                    E.printStackTrace();
                }

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] errorResponse, Throwable e) {
                // called when response HTTP status is "4XX" (eg. 401, 403, 404)
                new AlertDialog.Builder(ManageCalendarEvents.this)
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

    View.OnClickListener getOnClickDoSomething(final TextView DeleteEvent, final int id)  {
        return new View.OnClickListener() {
            public void onClick(View v) {
                //The url to go to to delete an event
                String url = AppHost + "calendar.php?delete=true&id=" + id + "&email=" + getEmail(ManageCalendarEvents.this) + "&password=" +getPassword(ManageCalendarEvents.this);

                //Go to the url set above.
                AsyncHttpClient client = new AsyncHttpClient();
                client.get(url, new AsyncHttpResponseHandler() {

                    @Override
                    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                        //Put the responsebody into HRF
                        String responseString = new String(responseBody);

                        String reason = "There was a client side error.";

                        //Split that up into two strings and display them in the console.
                        try{
                            JSONObject actionStatus = new JSONObject(responseString);
                            reason = actionStatus.getString("reason");
                        } catch (Exception e){
                            e.printStackTrace();
                        }

                        //Re-Load the events panel
                        getEvents();

                        //Display a message describing the outcome
                        Toast.makeText(ManageCalendarEvents.this, reason, Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                        System.out.println("Couldn't delete event " + id + ". Plese try again");
                    }
                });
            };
        };
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
