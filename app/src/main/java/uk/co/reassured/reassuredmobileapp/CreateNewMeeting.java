package uk.co.reassured.reassuredmobileapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import cz.msebera.android.httpclient.Header;

/**
 * Created by hfletcher on 13/02/2018.
 */

public class CreateNewMeeting extends AppCompatActivity {

    //This is where the API is
    public String AppHost = "http://82.10.188.99/MyMeetings.php";

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
                    System.out.println(MeetingParameters);
                } catch (Exception e){
                    e.printStackTrace();
                }
            }
        });
        //Display the continue button
        MB.addView(NextStepContainer);

        System.out.println(DaysInMonth);
        System.out.println(AvailableMonths);
        System.out.println(AvailableYears);

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

    public void PerformPostRequest(final Meetings.OnJSONResponseCallback callback, JSONObject PostData) {
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
