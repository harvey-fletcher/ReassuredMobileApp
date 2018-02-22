package uk.co.reassured.reassuredmobileapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.ShapeDrawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.lang.reflect.Array;
import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;

import cz.msebera.android.httpclient.Header;

/**
 * Created by hfletcher on 13/02/2018.
 */

public class CreateNewMeeting extends AppCompatActivity {

    //This is where the API is
    public String AppHost = "http://rmobileapp.co.uk/MyMeetings.php";

    //These are the parameters for the meeting.
    public JSONObject MeetingParameters = new JSONObject();

    //This is the context
    public Context ctx;

    //This is the screen
    public Display display;
    public int width = 0;
    public int height = 0;

    //This is the main body
    public RelativeLayout MB;

    public void onCreate(Bundle savedInstanceState){
        //Load the layout
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_meetings);

        //Set up the context
        ctx = CreateNewMeeting.this;

        //Get the screen size
        display = getWindowManager().getDefaultDisplay();
        width = display.getWidth();
        height = display.getHeight();

        //Set the main body container
        MB = findViewById(R.id.MainBody);

        //Start by asking for a meeting title
        NewMeetingTitle();

        //We need to add an action on the go back link so that the user can exit
        TextView ExitLink = findViewById(R.id.GoBackLink);
        ExitLink.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Toast.makeText(ctx, "Changes not saved.", Toast.LENGTH_LONG).show();
                finish();
            }
        });
    }

    public void NewMeetingTitle(){
        //This is the main container
        MB = findViewById(R.id.MainBody);

        //This is the step instructions
        TextView PageTitle = findViewById(R.id.PageTitle);

        //Tell the user what to do.
        PageTitle.setText("What is your meeting called?");

        //The fields each need to be inside a relativelayout so that they can be positioned correctly
        RelativeLayout MeetingNameBox = new RelativeLayout(ctx);
        RelativeLayout NextStepButtonBox = new RelativeLayout(ctx);

        //Give each container an ID
        int UID = (int)((new Date().getTime() / 1000L) % Integer.MAX_VALUE);
        MeetingNameBox.setId(UID);
        NextStepButtonBox.setId(UID + 1);

        //Position the containers on the page
        RelativeLayout.LayoutParams MeetingNameBoxParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        RelativeLayout.LayoutParams NextStepBoxParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        MeetingNameBoxParams.setMargins(0,100,0,50);
        NextStepBoxParams.addRule(RelativeLayout.BELOW, UID);
        MeetingNameBox.setLayoutParams(MeetingNameBoxParams);
        NextStepButtonBox.setLayoutParams(NextStepBoxParams);

        //Add an edit text to the right container
        final EditText MeetingName = new EditText(ctx);
        MeetingName.setHint("Meeting name");
        MeetingNameBox.addView(MeetingName);

        //Add a button to the right container
        Button NextStep = new Button(ctx);
        NextStep.setText("Next Step");
        NextStepButtonBox.addView(NextStep);

        //Make the fields the width of the screen
        MeetingName.setMinimumWidth(width);
        NextStep.setMinimumWidth(width);

        //Add those containers to mainbody
        MB.addView(MeetingNameBox);
        MB.addView(NextStepButtonBox);

        //Set up the next step button so it saves the name and moves on to the next step.
        NextStep.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String MeetingNameAsString = MeetingName.getText().toString();
                SaveMeetingName(MeetingNameAsString);
            }
        });
    }

    public void NewMeetingDateTime(){
        //This is the main container
        MB = findViewById(R.id.MainBody);
        MB.removeAllViews();

        //This is the step instructions
        TextView PageTitle = findViewById(R.id.PageTitle);

        //Tell the user what to do.
        PageTitle.setText("When is your meeting?");

        //The fields each need to be inside a relativelayout so that they can be positioned correctly
        RelativeLayout MeetingDateBox = new RelativeLayout(ctx);
        RelativeLayout MeetingTimeBox = new RelativeLayout(ctx);
        RelativeLayout NextStepButtonBox = new RelativeLayout(ctx);

        //Give each container an ID
        int UID = (int)((new Date().getTime() / 1000L) % Integer.MAX_VALUE);
        MeetingDateBox.setId(UID);
        MeetingTimeBox.setId(UID + 1);
        NextStepButtonBox.setId(UID + 2);

        //This is the calendar object
        Calendar mycal = new GregorianCalendar(new Date().getYear(), new Date().getMonth(), new Date().getDay());

        //The days of the month go in the datepicker
        int MonthTotalDays = mycal.getActualMaximum(Calendar.DAY_OF_MONTH);
        ArrayList<Integer> DaysInMonth = new ArrayList<Integer>();
        for(int i=0;i<MonthTotalDays;i++){
            DaysInMonth.add(i+1);
        }

        //Allow users to book meetings for the current and next months only
        ArrayList<Integer> AvailableMonths = new ArrayList<Integer>();
        AvailableMonths.add(mycal.MONTH);
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MONTH, 1);
        calendar.set(Calendar.DATE, calendar.getActualMinimum(Calendar.DAY_OF_MONTH));
        SimpleDateFormat sdfMonthNext = new SimpleDateFormat("MM");
        Date nextMonthFirstDay = calendar.getTime();
        int NextMonth = Integer.parseInt(sdfMonthNext.format(nextMonthFirstDay));
        AvailableMonths.add(NextMonth);

        //we will only provide option for the next year if the month is december
        SimpleDateFormat sdfYears = new SimpleDateFormat("yyyy");
        int CurrentYear = Integer.parseInt(sdfYears.format(new Date().getTime()));
        ArrayList<Integer> AvailableYears = new ArrayList<Integer>();
        AvailableYears.add(CurrentYear);
        if(NextMonth == 1){
            AvailableYears.add(CurrentYear + 1);
        }

        //There is a spinner for days and months
        final Spinner DaysSpinner = new Spinner(ctx);
        final Spinner MonthsSpinner = new Spinner(ctx);
        final Spinner YearsSpinner = new Spinner(ctx);

        //Each list needs an adapter
        ArrayAdapter<Integer> DaysListAdapt = new ArrayAdapter<Integer>(this, android.R.layout.simple_spinner_dropdown_item, DaysInMonth);
        ArrayAdapter<Integer> MonthListAdapt = new ArrayAdapter<Integer>(this, android.R.layout.simple_spinner_dropdown_item, AvailableMonths);
        ArrayAdapter<Integer> YearListAdapt = new ArrayAdapter<Integer>(this, android.R.layout.simple_spinner_dropdown_item, AvailableYears);

        //Add the adapters to the list
        DaysSpinner.setAdapter(DaysListAdapt);
        MonthsSpinner.setAdapter(MonthListAdapt);
        YearsSpinner.setAdapter(YearListAdapt);

        //Each spinner needs its own relativelayout
        RelativeLayout DSLayout = new RelativeLayout(ctx);
        RelativeLayout MSLayout = new RelativeLayout(ctx);
        RelativeLayout YSLayout = new RelativeLayout(ctx);

        //Each layout needs a set of parameters
        RelativeLayout.LayoutParams DSLParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        RelativeLayout.LayoutParams MSLParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        RelativeLayout.LayoutParams YSLParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);

        //Each layout needs an ID
        int DSId = (int)((new Date().getTime() / 1000L) % Integer.MAX_VALUE);
        int MSId = (int)((new Date().getTime() / 985L) % Integer.MAX_VALUE);
        int YSId = (int)((new Date().getTime() / 970L) % Integer.MAX_VALUE);
        DaysSpinner.setId(DSId);
        MonthsSpinner.setId(MSId);
        YearsSpinner.setId(YSId);

        //The month and years are to the right of days
        MSLParams.addRule(RelativeLayout.RIGHT_OF, DSId);
        YSLParams.addRule(RelativeLayout.RIGHT_OF, MSId);

        //Apply the parameters
        DaysSpinner.setLayoutParams(DSLParams);
        MonthsSpinner.setLayoutParams(MSLParams);
        YearsSpinner.setLayoutParams(YSLParams);

        //The 3 spinners go in a single container
        RelativeLayout DatePickerContainer = new RelativeLayout(ctx);
        int DatePickerContainerId = (int)((new Date().getTime() / 1000L) % Integer.MAX_VALUE);
        DatePickerContainer.setId(DatePickerContainerId);
        DatePickerContainer.addView(DaysSpinner);
        DatePickerContainer.addView(MonthsSpinner);
        DatePickerContainer.addView(YearsSpinner);

        //That single container has some parameters
        RelativeLayout.LayoutParams DPCParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        DPCParams.setMargins(10,100,0,0);
        DatePickerContainer.setLayoutParams(DPCParams);
        DatePickerContainer.measure(0,0);
        DatePickerContainer.setX((width / 2) - (DatePickerContainer.getMeasuredWidth() / 2));

        //Add the datepicker to the main body
        MB.addView(DatePickerContainer);

        //there will be a label that says "at" in between the date picker and time picker
        RelativeLayout AtLabel = new RelativeLayout(ctx);
        int AtLabelID = (int)((new Date().getTime() / 950) % Integer.MAX_VALUE);
        AtLabel.setId(AtLabelID);
        RelativeLayout.LayoutParams ATLParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        ATLParams.addRule(RelativeLayout.BELOW, DatePickerContainerId);
        ATLParams.setMargins(0,50,0,0);
        AtLabel.setLayoutParams(ATLParams);

        //Create the "at" label and put it in the right container
        TextView AtLabelText = new TextView(ctx);
        AtLabelText.setTextSize(20);
        AtLabelText.setText("at");
        AtLabelText.setWidth(width);
        AtLabelText.setGravity(Gravity.CENTER);
        AtLabel.addView(AtLabelText);

        //Add the atlabel to main body
        MB.addView(AtLabel);

        //The time picker has a container
        RelativeLayout TimePickerContainer = new RelativeLayout(ctx);

        //The container has an ID
        int TimePickerContainerID = (int)((new Date().getTime() / 925) % Integer.MAX_VALUE);
        TimePickerContainer.setId(TimePickerContainerID);

        //The container has a layout
        RelativeLayout.LayoutParams TPCParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        TPCParams.addRule(RelativeLayout.BELOW, AtLabelID);
        TPCParams.setMargins(0,50,0,0);
        TimePickerContainer.setLayoutParams(TPCParams);
        TimePickerContainer.setGravity(Gravity.CENTER);

        //The hours and minutes selectors are in their own layouts
        RelativeLayout HoursLayout = new RelativeLayout(ctx);
        RelativeLayout MinutesLayout = new RelativeLayout(ctx);

        //The layouts have IDs
        int HLId =  (int)((new Date().getTime() / 900) % Integer.MAX_VALUE);
        int MLId =  (int)((new Date().getTime() / 875) % Integer.MAX_VALUE);
        HoursLayout.setId(HLId);
        MinutesLayout.setId(MLId);

        //The minutes layout is to the right of the hours layout
        RelativeLayout.LayoutParams MLParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        MLParams.addRule(RelativeLayout.RIGHT_OF, HLId);
        MinutesLayout.setLayoutParams(MLParams);

        //The hours values
        ArrayList<Integer> Hours = new ArrayList<Integer>();
        for(int i=9;i<20;i++){
            Hours.add(i);
        }

        //The minutes values
        ArrayList<Integer> Minutes = new ArrayList<Integer>();
        for(int i=0;i<60;i=i+10){
            Minutes.add(i);
        }

        //There is a spinner for days and months
        final Spinner HoursSpinner = new Spinner(ctx);
        final Spinner MinutesSpinner = new Spinner(ctx);

        //Each list needs an adapter
        ArrayAdapter<Integer> HoursAdapt = new ArrayAdapter<Integer>(this, android.R.layout.simple_spinner_dropdown_item, Hours);
        ArrayAdapter<Integer> MinutesAdapt = new ArrayAdapter<Integer>(this, android.R.layout.simple_spinner_dropdown_item, Minutes);

        //Add the adapters to the list
        HoursSpinner.setAdapter(HoursAdapt);
        MinutesSpinner.setAdapter(MinutesAdapt);

        //Display the spinners
        HoursLayout.addView(HoursSpinner);
        MinutesLayout.addView(MinutesSpinner);

        //Add the hours and minutes layouts to the timepicker container.
        TimePickerContainer.addView(HoursLayout);
        TimePickerContainer.addView(MinutesLayout);

        //Add the time picker to the main view
        MB.addView(TimePickerContainer);

        //Add the relativelayout for the continue button
        RelativeLayout NextStepContainer = new RelativeLayout(ctx);
        RelativeLayout.LayoutParams NSCParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
        NSCParams.addRule(RelativeLayout.BELOW, TimePickerContainerID);
        NSCParams.setMargins(0,50,0,0);
        NextStepContainer.setLayoutParams(NSCParams);

        //Add the continue button
        Button NextStep = new Button(ctx);
        NextStep.setWidth(width);
        NextStep.setText("Next Step");
        NextStepContainer.addView(NextStep);

        //Make the continue button do something.
        NextStep.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String Month = MonthsSpinner.getSelectedItem().toString();
                String Day = DaysSpinner.getSelectedItem().toString();
                String Hours = HoursSpinner.getSelectedItem().toString();
                String Minutes = MinutesSpinner.getSelectedItem().toString();
                if(Integer.parseInt(Month) < 10){
                    Month = "0" + Month;
                }
                if(Integer.parseInt(Day) < 10){
                    Day = "0" + Day;
                }
                if(Integer.parseInt(Hours) < 10){
                    Hours = "0" + Hours;
                }
                if(Integer.parseInt(Minutes) < 10){
                    Minutes = "0" + Minutes;
                }

                String start_time = YearsSpinner.getSelectedItem().toString() + "-" + Month + "-" + Day + " " + Hours + ":" + Minutes +":00";
                try{
                    MeetingParameters.put("start_time", start_time);
                } catch (Exception e){
                    e.printStackTrace();
                }

                SetMeetingDuration();
            }
        });
        //Display the continue button
        MB.addView(NextStepContainer);
    }

    public void SetMeetingDuration(){
        //Set the title
        TextView PageTitle = findViewById(R.id.PageTitle);
        PageTitle.setText("How long is your meeting?");

        //Clear the main body
        MB.removeAllViews();

        //There are two containers, one for duration and one for the "Next Step" button
        RelativeLayout DC = new RelativeLayout(ctx);
        RelativeLayout NS = new RelativeLayout(ctx);

        //Each of those layouts have an ID
        int DCid = (int)((new Date().getTime() / 1000) % Integer.MAX_VALUE);
        int NSid = (int)((new Date().getTime() / 900) % Integer.MAX_VALUE);

        //Assign the layouts with their ID
        DC.setId(DCid);
        NS.setId(NSid);

        //Each of the layouts needs a parameters
        RelativeLayout.LayoutParams DCP = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        RelativeLayout.LayoutParams NSP = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        DCP.setMargins(0,50,0,0);
        NSP.addRule(RelativeLayout.BELOW, DCid);
        NSP.setMargins(0,50,0,0);
        DC.setLayoutParams(DCP);
        NS.setLayoutParams(NSP);

        //Align it center
        DC.setGravity(Gravity.CENTER);

        //There is a spinner for duration with a maximum of "Whole Day"
        final Spinner DurationSpiner = new Spinner(ctx);

        //These are the duration values
        ArrayList<String> Durations = new ArrayList<String>();
        Durations.add("5 Minutes");
        Durations.add("10 Minutes");
        Durations.add("15 Minutes");
        Durations.add("20 Minutes");
        Durations.add("25 Minutes");
        Durations.add("30 Minutes");
        Durations.add("35 Minutes");
        Durations.add("40 Minutes");
        Durations.add("45 Minutes");
        Durations.add("50 Minutes");
        Durations.add("55 Minutes");
        Durations.add("1 Hour");
        Durations.add("1 Hour 15 Minutes");
        Durations.add("1 Hour 30 Minutes");
        Durations.add("1 Hour 45 Minutes");
        Durations.add("2 Hours");
        Durations.add("2 Hours 15 Minutes");
        Durations.add("2 Hours 30 Minutes");
        Durations.add("2 Hours 45 Minutes");
        Durations.add("3 Hours");
        Durations.add("3 Hours 15 Minutes");
        Durations.add("3 Hours 30 Minutes");
        Durations.add("3 Hours 45 Minutes");
        Durations.add("4 Hours");
        Durations.add("4 Hours 30 Minutes");
        Durations.add("5 Hours");
        Durations.add("6 Hours");
        Durations.add("7 Hours");
        Durations.add("Whole Day");

        //Those durations have an associative numeric value (in minutes)
        ArrayList<Integer> AssociativeNumericValue = new ArrayList<Integer>();
        AssociativeNumericValue.add(5);
        AssociativeNumericValue.add(10);
        AssociativeNumericValue.add(15);
        AssociativeNumericValue.add(20);
        AssociativeNumericValue.add(25);
        AssociativeNumericValue.add(30);
        AssociativeNumericValue.add(35);
        AssociativeNumericValue.add(40);
        AssociativeNumericValue.add(45);
        AssociativeNumericValue.add(50);
        AssociativeNumericValue.add(55);
        AssociativeNumericValue.add(60);
        AssociativeNumericValue.add(75);
        AssociativeNumericValue.add(90);
        AssociativeNumericValue.add(105);
        AssociativeNumericValue.add(120);
        AssociativeNumericValue.add(135);
        AssociativeNumericValue.add(150);
        AssociativeNumericValue.add(165);
        AssociativeNumericValue.add(180);
        AssociativeNumericValue.add(195);
        AssociativeNumericValue.add(210);
        AssociativeNumericValue.add(225);
        AssociativeNumericValue.add(240);
        AssociativeNumericValue.add(270);
        AssociativeNumericValue.add(300);
        AssociativeNumericValue.add(360);
        AssociativeNumericValue.add(420);
        AssociativeNumericValue.add(480);

        //Put the durations into the drop down.
        ArrayAdapter<String> DurationValues = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, Durations);
        DurationSpiner.setAdapter(DurationValues);

        //Add the durations to the spinner
        DC.setMinimumWidth(width);
        DC.addView(DurationSpiner);

        //Add a next step button
        Button NSButton = new Button(ctx);
        NSButton.setText("Next Step");
        NSButton.setMinimumWidth(width);
        final Spinner InnerDuration = DurationSpiner;
        final ArrayList InnerNumeric = AssociativeNumericValue;
        NSButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try{
                    int Selected = (int)InnerDuration.getSelectedItemId();
                    int AssociativeValue = (int)InnerNumeric.get(Selected);
                    MeetingParameters.put("duration", AssociativeValue);
                    InviteAttendees();
                } catch (Exception e){
                    e.printStackTrace();
                }
            }
        });

        //Add the button so it can be seen
        NS.addView(NSButton);

        //Display all the containers.
        MB.addView(DC);
        MB.addView(NS);
    }

    public void InviteAttendees() {
        //Set the title
        final TextView PageTitle = findViewById(R.id.PageTitle);
        PageTitle.setText("Invite Attendees");

        //Construct an array in Parameters so we have somewhere to put USER IDs
        try{
            MeetingParameters.put("invitees", new JSONArray());
        } catch (Exception e){
            e.printStackTrace();
        }


        //Clear the main body
        MB.removeAllViews();

        //There is a search field and a results scroller which has an inner container, the scroller is in a relativelayout so it can be ordered
        RelativeLayout SearchFieldContainer = new RelativeLayout(ctx);
        RelativeLayout ResultsScrollerContainer = new RelativeLayout(ctx);
        final RelativeLayout NextStepButtonContainer = new RelativeLayout(ctx);
        final ScrollView ResultsScroller = new ScrollView(ctx);

        //Give outer containers an ID
        int SFCId = (int)((new Date().getTime() / 750) % Integer.MAX_VALUE);
        int RSCId = (int) ((new Date().getTime() / 775) % Integer.MAX_VALUE);
        int NSCId = (int) ((new Date().getTime() / 750) % Integer.MAX_VALUE);
        SearchFieldContainer.setId(SFCId);
        ResultsScrollerContainer.setId(RSCId);

        //The containers have a layout
        RelativeLayout.LayoutParams SFContainer = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        RelativeLayout.LayoutParams RSContainer = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        RelativeLayout.LayoutParams NSContainer = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);

        //The search field is 50px from top
        SFContainer.setMargins(0, 50, 0, 0);

        //The results are 50px below the searchfield
        RSContainer.addRule(RelativeLayout.BELOW, SFCId);
        NSContainer.addRule(RelativeLayout.BELOW, RSCId);

        //Apply the parameters
        SearchFieldContainer.setLayoutParams(SFContainer);
        ResultsScrollerContainer.setLayoutParams(RSContainer);
        NextStepButtonContainer.setLayoutParams(NSContainer);

        //The searchbox has a textview
        final EditText SearchBox = new EditText(ctx);
        SearchBox.setMinimumWidth(width);
        SearchBox.setHint("Type names and tap results to invite.");
        SearchBox.setTextSize(20);
        SearchFieldContainer.addView(SearchBox);

        //When the user types something in the search box, we want to make a new post request to the server so that we can list the users
        SearchBox.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                try {
                    TextView Error = findViewById(R.id.PageTitle);
                    String SearchTerm = SearchBox.getText().toString();

                    if(SearchTerm.length() >= 2){
                        ResultsScroller.removeAllViews();
                        Error.setText("Invite Attendees");
                        JSONObject PostData = new JSONObject();
                        PostData.put("action", "usersearch");
                        PostData.put("searchterm", SearchTerm);

                        PerformPostRequest(new OnJSONResponseCallback() {
                            @Override
                            public JSONArray onJSONResponse(boolean success, JSONArray response) {
                                ResultsScroller.addView(DisplayUserSearchResults(response));
                                System.out.println(response);
                                return null;
                            }
                        }, PostData);
                    } else {
                        ResultsScroller.removeAllViews();
                        Error.setText("Too short.");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        //Add a next step button to the next step container
        Button NextStepButton = new Button(ctx);
        NextStepButton.setText("Next Step");
        NextStepButton.setMinimumWidth(width);
        NextStepButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try{
                    if(MeetingParameters.getJSONArray("invitees").length() < 1){
                        Toast.makeText(ctx, "You must invite at least one person.", Toast.LENGTH_SHORT).show();
                    } else {
                        ChooseLocation();
                    }
                } catch (Exception e){
                    e.printStackTrace();
                }
            }
        });
        NextStepButtonContainer.addView(NextStepButton);

        //Add the scrollview to the container
        ResultsScrollerContainer.addView(ResultsScroller);

        //Add the outer containers to the mainbody so they can be seen
        MB.addView(SearchFieldContainer);
        MB.addView(ResultsScrollerContainer);
        MB.addView(NextStepButtonContainer);
    }

    public void ChooseLocation(){
        //Clear all views
        MB.removeAllViews();

        //There are two layouts, one for "Finish" and one for the list of available meeting rooms.
        RelativeLayout RoomListContainer = new RelativeLayout(ctx);

        //Set their IDs so that they can be positioned
        int RLCId = (int)((new Date().getTime() / 1000L) % Integer.MAX_VALUE);
        RoomListContainer.setId(RLCId);

        //The outer layouts are positioned one after the other
        RelativeLayout.LayoutParams RLCParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        RoomListContainer.setLayoutParams(RLCParams);

        //Inside the roomlist container there is a scrolling view with an innerlayout relativelayout
        final ScrollView RoomList = new ScrollView(ctx);

        //We need to make a post request to the API so that we can obtain a list of meeting rooms.
        try{
            //Build the options
            JSONObject PostData = new JSONObject();
            PostData.put("action", "CheckAvailabilityRooms");
            PostData.put("eventStart", MeetingParameters.getString("start_time"));
            PostData.put("duration", MeetingParameters.getInt("duration"));

            //We need to let the user know we are loading meeting rooms.
            TextView PageTitle = findViewById(R.id.PageTitle);
            PageTitle.setText("Loading meeting rooms...");

            //Make the post request to the API
            PerformPostRequest(new OnJSONResponseCallback() {
                @Override
                public JSONArray onJSONResponse(boolean success, JSONArray response) {
                    RoomList.addView(ListAvailableRooms(response));
                    return null;
                }
            }, PostData);
        } catch (Exception e){
            Toast.makeText(ctx, "There was an unexpected error. Please try again.", Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }

        //make the scrollview visible
        RoomListContainer.addView(RoomList);

        //Add all the views
        MB.addView(RoomListContainer);
    }

    public RelativeLayout ListAvailableRooms(JSONArray AvailableRooms){
        //Set the title
        TextView PageTitle = findViewById(R.id.PageTitle);
        PageTitle.setText("Select a meeting room");

        //Room list container that will be returned
        RelativeLayout RoomsListContainer = new RelativeLayout(ctx);

        try{
            JSONObject RoomsList = new JSONObject(AvailableRooms.getJSONObject(0).getString("AvailableRooms"));

            Iterator<String> keys = RoomsList.keys();
            ArrayList<String> RoomEmails = new ArrayList<String>();
            ArrayList<String> RoomNames = new ArrayList<String>();

            while(keys.hasNext()){
                String key = keys.next();
                RoomEmails.add(key);
                RoomNames.add(RoomsList.getString(key));
            }

            //Since we are about to increment through the list of meeting rooms, we want to give each item a border. To do this we need a colourscheme. 0 is reassured orange, 1 is reassured purple.
            int ColourScheme = 0;

            int RoomID = (int)((new Date().getTime() / 1000L) % Integer.MAX_VALUE);
            for(int i=0;i<RoomNames.size();i++){
                //This is the room's container
                RelativeLayout Room = new RelativeLayout(ctx);

                //The layout has an ID
                Room.setId(RoomID);

                //The container has parameters
                RelativeLayout.LayoutParams RoomLayoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);

                //Seperation between rooms
                RoomLayoutParams.setMargins(10,0,10,20);
                RoomLayoutParams.height = 150;

                //Everything should go in the center
                Room.setGravity(Gravity.CENTER);

                //Position the container before the previous one
                if(i>0){
                    RoomLayoutParams.addRule(RelativeLayout.BELOW, RoomID - 1);
                }

                //Apply the parameters so the container is positioned
                Room.setLayoutParams(RoomLayoutParams);

                //Give the individual post a border
                ShapeDrawable rectShapeDrawable = new ShapeDrawable(); // pre defined class

                // get paint
                Paint paint = rectShapeDrawable.getPaint();

                // set border color, stroke and stroke width
                if(ColourScheme == 0){
                    //Reassured Orange
                    paint.setColor(Color.parseColor("#FE8A00"));

                    //Change so the next item has the opposite colour
                    ColourScheme = 1;
                } else {
                    //Reassured Purple
                    paint.setColor(Color.parseColor("#1870A0"));

                    //Change so the next item has the opposite colour
                    ColourScheme = 0;
                }
                paint.setStyle(Paint.Style.STROKE);
                paint.setStrokeWidth(4);
                Room.setBackgroundDrawable(rectShapeDrawable);

                //Put the room name in the container
                TextView RoomNameText = new TextView(ctx);
                RoomNameText.setTextSize(20);
                RoomNameText.setText(RoomNames.get(i));
                RoomNameText.setX(10);
                Room.addView(RoomNameText);

                //Set up the container so that when the user clicks it, we add the chosen meeting room to the MeetingParameters
                Room.setOnClickListener(SetUpRoomClick(RoomEmails.get(i), RoomNames.get(i)));

                //Increment the room id
                RoomID++;

                //Add the container to the main list
                RoomsListContainer.addView(Room);
            }
        } catch (Exception e){
            e.printStackTrace();
        }

        //Return the room list
        return RoomsListContainer;
    };

    View.OnClickListener SetUpRoomClick(final String RoomEmail, final String RoomName){
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try{
                    MeetingParameters.put("venue", RoomEmail);
                    MeetingParameters.put("venueName", RoomName);
                    SendFinalMeetingBook();
                } catch (Exception e){
                    Toast.makeText(ctx, "There was an error. Please try again.", Toast.LENGTH_LONG);
                    finish();
                    e.printStackTrace();
                }
            }
        };
    }

    public void SendFinalMeetingBook(){
        //This is for debug purposes.
        System.out.println("WE ARE ABOUT TO BOOK A MEETING WITH THE FOLLOWING PARAMETERS \n \n" + MeetingParameters + "\n");

        //Clear all views so we can display the "Please Wait" message
        MB.removeAllViews();

        //Set the title
        TextView PageTitle = findViewById(R.id.PageTitle);
        PageTitle.setText("Booking Meeting...");

        //Make the post request
        try{
            //Build the postdata
            JSONObject PostData = MeetingParameters;
            PostData.put("action", "MeetingRoomBook");

            //Do the post request
            PerformPostRequest(new OnJSONResponseCallback() {
                @Override
                public JSONArray onJSONResponse(boolean success, JSONArray response) {
                    try{
                        JSONObject result = response.getJSONObject(0);
                        if(result.getString("status").matches("200")){
                            Toast.makeText(ctx, "Meeting Booked!", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(ctx, "Unexpected error \n Please try again" , Toast.LENGTH_LONG).show();
                        }

                        finish();
                    } catch (Exception e){
                        e.printStackTrace();
                    }
                    return null;
                }
            }, PostData);
        } catch (Exception e){
            Toast.makeText(ctx, "There was an error. Please try again.", Toast.LENGTH_LONG);
            finish();
            e.printStackTrace();
        }

    }

    public RelativeLayout DisplayUserSearchResults(JSONArray response){
        RelativeLayout InnerResultsContainer = new RelativeLayout(ctx);
        RelativeLayout.LayoutParams IRCP = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
        IRCP.height = 200;
        InnerResultsContainer.setLayoutParams(IRCP);
        InnerResultsContainer.removeAllViews();

        try{
            int LastID = (int)((new Date().getTime() / 1000L) % Integer.MAX_VALUE);
            for(int i=0;i<response.length();i++){
                //Each result goes in a container
                RelativeLayout Container = new RelativeLayout(ctx);

                //Each container has params
                RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);

                //There needs to be a seperation between results
                layoutParams.setMargins(10,0,0,20);

                //If this is not the first item, place it below the last one
                if(i > 0){
                    layoutParams.addRule(RelativeLayout.BELOW, LastID);
                }

                //Apply the parameters
                Container.setLayoutParams(layoutParams);

                //Give the container a background colour
                Container.setBackgroundColor(Color.parseColor("#00FFFF"));

                //The container needs a textview
                TextView Result = new TextView(ctx);
                JSONObject UserDetails = new JSONObject(response.getString(i));
                String detail = UserDetails.getString("firstname") + " " + UserDetails.getString("lastname") + "\n" + UserDetails.getString("location_name");
                Result.setText(detail);
                Result.setTextSize(20);
                Result.setMinHeight(50);
                Container.addView(Result);

                //Add the invitee when they get clicked
                Container.setOnClickListener(AddInvitee(Integer.parseInt(UserDetails.getString("id"))));

                //Each container has an ID
                LastID++;
                Container.setId(LastID);

                //Display the result
                InnerResultsContainer.addView(Container);
            }
        } catch (Exception e){
            e.printStackTrace();
        }

        //Add the textview
        return InnerResultsContainer;
    }

    View.OnClickListener AddInvitee(final int UserID){
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try{
                    JSONArray Invitees = MeetingParameters.getJSONArray("invitees");

                    //Check if the user has already been invited.
                    for(int i=0;i<Invitees.length();i++){
                        if(Invitees.getInt(i) == UserID){
                            Toast.makeText(ctx, "User has already been invited.", Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }

                    Invitees.put(UserID);
                    MeetingParameters.put("invitees", Invitees);
                    Toast.makeText(ctx, "Added to invite list.", Toast.LENGTH_SHORT).show();
                } catch (Exception e){
                    e.printStackTrace();
                }
            }
        };
    }

    public void SaveMeetingName(String MeetingName){
        //First, check that the meeting name does not contain forbidden characters
        String[] ForbiddenChars = new String[7];
        ForbiddenChars[0] = "\"";
        ForbiddenChars[1] = "'";
        ForbiddenChars[2] = "?";
        ForbiddenChars[3] = "!";
        ForbiddenChars[4] = "_";
        ForbiddenChars[5] = "|";
        ForbiddenChars[6] = "@";

        for(int i=0;i<ForbiddenChars.length; i++){
            if(MeetingName.contains(ForbiddenChars[i])){
                Toast.makeText(ctx, "Your meeting name contains a \"" + ForbiddenChars[i] + "\" \nMeeting names cannot contain that character.", Toast.LENGTH_LONG).show();
                return;
            }
        }

        try{
            MeetingParameters.put("name", MeetingName);
            NewMeetingDateTime();
        } catch (Exception e){
            e.printStackTrace();
        }

    }

    public interface OnJSONResponseCallback {
        public JSONArray onJSONResponse(boolean success, JSONArray response);
    }

    public void PerformPostRequest(final OnJSONResponseCallback callback, JSONObject PostData) {
        //To authenticate against the API we need the user's credentials
        String Email = getSharedPreferences(ctx).getString("Email","");
        String Password = getSharedPreferences(ctx).getString("Password","");

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

        client.post(AppHost, RequestParameters, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                try {
                    String ResponseString = new String(responseBody);
                    JSONArray ResponseArray = new JSONArray(ResponseString);
                    callback.onJSONResponse(true, ResponseArray);
                } catch (Exception e) {
                    Log.e("Exception", "JSONException on success: " + e.toString());
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                try {
                    Toast.makeText(ctx, "Error: " + statusCode, Toast.LENGTH_LONG).show();
                } catch (Exception e) {
                    Log.e("Exception", "JSONException on failure: " + e.toString());
                }
            }
        });
    }

    public static SharedPreferences getSharedPreferences(Context ctx){
        return PreferenceManager.getDefaultSharedPreferences(ctx);
    }
}
