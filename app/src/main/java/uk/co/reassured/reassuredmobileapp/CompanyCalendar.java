package uk.co.reassured.reassuredmobileapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.RelativeSizeSpan;
import android.view.Display;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.DateFormatSymbols;
import java.util.Calendar;
import java.util.Iterator;

import cz.msebera.android.httpclient.Header;

/**
 * Created by hfletcher on 24/01/2018.
 */

public class CompanyCalendar extends AppCompatActivity {

    //ClassGlobals variables
    ClassGlobals classGlobals = new ClassGlobals();

    //These functions are used for the calendar
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

    //Used for labelling dates
    Calendar calendar = Calendar.getInstance();
    DateFormatSymbols dfs = new DateFormatSymbols();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //Load the calendar layout
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_company_calendar);

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
        final RelativeLayout go_back = findViewById(R.id.GoBackLink);
        final ImageView next_month = findViewById(R.id.monthNext);
        final ImageView previous_month = findViewById(R.id.monthPrevious);

        //When the "Go Back" link is clicked, close this screen.
        go_back.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                finish();
            }
        });

        //When the "Next Month" button is clicked, move to the next month.
        next_month.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                moveToNext();
            }
        });

        //When the "Previous Month" button is clicked, move to the previous month.
        previous_month.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                moveToLast();
            }
        });

        //Display 4 more records on a new page
        MR.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view){
                DisplayMoreRecords();
            }
        });

        //Display the previous 4 records
        LR.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view){
                DisplayFewerRecords();
            }
        });

        //This is the user's team
        int user_team_id = classGlobals.sharedPrefs().getInt("team_id", 0);

        //If the user is HR, Marketing or IT, the manage events button will be displayed.
        if((user_team_id == 1) || (user_team_id == 2) || (user_team_id == 3)){
            //The events manager button is here.
            Button ManageEventsButton = (Button)findViewById(R.id.mangageEvents);

            //When the events manager button is clicked, load the events manager
            ManageEventsButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    Intent CalendarManager = new Intent(ReassuredMobileApp.getAppContext(), ManageCalendarEvents.class);
                    startActivity(CalendarManager);
                }
            });

            //Make the events manager button visible
            findViewById(R.id.mangageEvents).setVisibility(View.VISIBLE);
        }
    }

    protected void onResume(){
        super.onResume();
        setMonthText(ViewMonth, ViewYear);
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
        String url = classGlobals.AppHost + "calendar.php?list=true&start=" + ViewYear + "-" + fetchMonth + "-01&end=" + ViewYear + "-" + fetchMonth + "-31&from_result=" + from_record;

        System.out.println(url);

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

                    //Where are we placing the first event in the panel?
                    int DefaultPosition = 0;

                    //This is a string of events
                    SpannableStringBuilder CalendarEventsString = new SpannableStringBuilder();

                    //Clear the views
                    MB.removeAllViews();

                    JSONObject CalendarEventsByDate = new JSONObject();

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

                            //If the array of events does not have a current section for this day of month, create it.
                            if(!CalendarEventsByDate.has(DayOfMonth)) {
                                CalendarEventsByDate.put(DayOfMonth, new JSONArray());
                            }

                            //Put this event into the array of events sorted by day.
                            JSONArray EventsOnDay = CalendarEventsByDate.getJSONArray(DayOfMonth);
                            EventsOnDay.put(Event);
                            CalendarEventsByDate.put(DayOfMonth, EventsOnDay);

                            EventNum ++;
                        } while ((EventNum < NumEvents) && EventNum < 4);

                        //Get all the keys of events sorted by day
                        Iterator<?> DaysWithEvents = CalendarEventsByDate.keys();

                        //For all the days with events
                        while( DaysWithEvents.hasNext() ){
                            //Which date are we looking up
                            String LookUpDate = (String) DaysWithEvents.next();

                            //Get all the events on that day
                            JSONArray EventsOnDay = CalendarEventsByDate.getJSONArray(LookUpDate);

                            //Put a "Date" header for each group of events.
                            SpannableString Date = new SpannableString(LookUpDate + "\n");
                            Date.setSpan(new RelativeSizeSpan(2f), 0, Date.length(), 0);
                            CalendarEventsString.append(Date);

                            //Put all the event details into the list, size them, and put them on the events string.
                            for(int i=0;i<EventsOnDay.length();i++){
                                //Get each individual event
                                JSONObject Event = EventsOnDay.getJSONObject(i);

                                //Get the data from the event that we want to display
                                SpannableString EventName = new SpannableString(Event.getString("event_name") + "\n");
                                SpannableString EventLocation = new SpannableString(Event.getString("event_location") + "\n");
                                SpannableString EventInfo = new SpannableString(Event.getString("event_information") + "\n\n");

                                //Set the size
                                EventName.setSpan(new RelativeSizeSpan(1.5f),0, EventName.length(), 0);
                                EventLocation.setSpan(new RelativeSizeSpan(1.2f),0, EventLocation.length(), 0);
                                EventInfo.setSpan(new RelativeSizeSpan(1.2f),0, EventInfo.length(), 0);

                                //Append the data to the list.
                                CalendarEventsString.append(EventName).append(EventLocation).append(EventInfo);
                            }
                        }
                    } else {
                        SpannableString NoEventString = new SpannableString("There are no events this month.");
                        NoEventString.setSpan(new RelativeSizeSpan(1.2f),0, NoEventString.length(), 0);
                        CalendarEventsString.append(NoEventString);
                    }

                    TextView NewText = new TextView(ReassuredMobileApp.getAppContext());
                    NewText.setText(CalendarEventsString);
                    NewText.setY(DefaultPosition);
                    NewText.setX(10);
                    MB.addView(NewText);
                    MB.refreshDrawableState();

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
                new AlertDialog.Builder(ReassuredMobileApp.getAppContext())
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
}
