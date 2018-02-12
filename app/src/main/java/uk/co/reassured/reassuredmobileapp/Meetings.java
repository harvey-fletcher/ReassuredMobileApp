package uk.co.reassured.reassuredmobileapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.RelativeSizeSpan;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.util.Date;

import cz.msebera.android.httpclient.Header;

public class Meetings extends AppCompatActivity {

    //Where is the app API hosted?
    private String AppHost = "http://82.10.188.99/api/";

    //This is the context
    private Context ctx = Meetings.this;

    //Used for positioning items
    public int StaticID = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meetings);

        //This is the button which will make the user go back to the home page.
        final TextView go_back = findViewById(R.id.GoBackLink);
        go_back.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                finish();
            }
        });

        //Make a post request to get the existing meetings.
        MakeGetMeetingsRequest();
    };

    public void MakeGetMeetingsRequest(){
        try{
            JSONObject PostData = new JSONObject();
            PostData.put("action","ListPersonalMeetings");
            PerformPostRequest(new OnJSONResponseCallback(){
                @Override
                public JSONArray onJSONResponse(boolean success, JSONArray response){
                    DisplayExistingMeetings(response);
                    return response;
                }
            }, PostData);
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public void DisplayExistingMeetings(final JSONArray Meetings){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //Get all the time frames (Today, Tomorrow, Beyond)
                JSONObject AllTimeframes = new JSONObject();
                System.out.println(Meetings);

                try{
                    AllTimeframes = Meetings.getJSONObject(0);
                } catch (Exception e){
                    Toast.makeText(ctx, "There are no meetings.", Toast.LENGTH_LONG).show();
                }

                //Get all existing meetings
                try{
                    //This is the container for existing meetings
                    RelativeLayout ExistingMeetingsContainer = findViewById(R.id.ExistingMeetingsContainer);
                    ExistingMeetingsContainer.removeAllViews();

                    //Iterate through all of today's meetings.
                    IterateTodayMeetings(AllTimeframes);

                    //Iterate through all of tomorrow's meetings.
                    IterateTomorrowMeetings(AllTimeframes);

                    //Iterate through future meetings
                    IterateFutureMeetings(AllTimeframes);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void IterateFutureMeetings(JSONObject AllTimeframes) throws JSONException{
        //This is the container for existing meetings
        RelativeLayout ExistingMeetingsContainer = findViewById(R.id.ExistingMeetingsContainer);

        //This container says "Future"
        RelativeLayout FutureHeader = new RelativeLayout(ctx);
        TextView FutureHeaderText = new TextView(ctx);
        FutureHeaderText.setText("Future:");
        FutureHeaderText.setTextSize(15);
        FutureHeader.addView(FutureHeaderText);
        RelativeLayout.LayoutParams FutureHeaderParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        FutureHeaderParams.addRule(RelativeLayout.BELOW, StaticID);
        FutureHeader.setLayoutParams(FutureHeaderParams);
        FutureHeader.setId(StaticID + 1);
        ExistingMeetingsContainer.addView(FutureHeader);
        ExistingMeetingsContainer.refreshDrawableState();

        //Increment staticID
        StaticID++;

        //Get all the meetings today
        JSONArray FutureMeetings = AllTimeframes.getJSONArray("future");

        //How many meetings are there.
        int MeetingsTodayCount = FutureMeetings.length();

        for(int i=0;i<MeetingsTodayCount;i++){
            //Build up the meeting data
            JSONObject meeting = new JSONObject(FutureMeetings.getString(i));
            String title = meeting.getString("title");
            String location = meeting.getString("location");
            String start = meeting.getString("start_time").substring(meeting.getString("start_time").length() -8, meeting.getString("start_time").length());
            int invited = new JSONArray(meeting.getString("invited")).length();
            int attending = new JSONArray(meeting.getString("attending")).length();
            int declined = new JSONArray(meeting.getString("declined")).length();

            //The meeting goes in a spannable layout so that it can be ordered.
            RelativeLayout IndividualMeetingContainer = new RelativeLayout(ctx);

            //Give it an ID so we can assign it parameters
            IndividualMeetingContainer.setId(StaticID + 1);

            //Each meeting is in a textview
            TextView Meeting = new TextView(ctx);

            //Build a spannable string to populate the meeting details
            SpannableString MeetingDetails = BuildMeetingSpannable(title, location, start, invited, attending, declined);
            Meeting.setText(MeetingDetails);
            Meeting.setX(10);

            //Add the textview to the container
            IndividualMeetingContainer.addView(Meeting);

            //Should the user be able to accept this meeting?
            if(meeting.getInt("can_accept") == 1){
                RelativeLayout AcceptButton = EnableAcceptButton(meeting, 1);
                IndividualMeetingContainer.addView(AcceptButton);
            } else {
                RelativeLayout AcceptButton = EnableAcceptButton(meeting, 0);
                IndividualMeetingContainer.addView(AcceptButton);
            }

            //The container needs to be positioned below the previous one
            RelativeLayout.LayoutParams ContainerParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);

            //Display this box below the previous one
            ContainerParams.addRule(RelativeLayout.BELOW, StaticID);
            ContainerParams.setMargins(0,0,0,20);

            //Apply the parameters
            IndividualMeetingContainer.setLayoutParams(ContainerParams);

            //Add the individual meeting container so it can be seen.
            ExistingMeetingsContainer.addView(IndividualMeetingContainer);

            //Increment static ID by one So we can place future meetings below it.
            StaticID++;
        }
    }

    public RelativeLayout EnableAcceptButton(JSONObject meeting, int DisplayAccept){
        //The button needs a container
        RelativeLayout ButtonLayout = new RelativeLayout(ctx);

        try{
            //Each button needs a layout
            RelativeLayout Accept = new RelativeLayout(ctx);
            RelativeLayout Decline = new RelativeLayout(ctx);

            //Give those IDs
            int AcceptID = (int)((new Date().getTime() / 1000L) % Integer.MAX_VALUE);
            Accept.setId(AcceptID);

            //Those layouts are next to each other
            RelativeLayout.LayoutParams AcceptParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,RelativeLayout.LayoutParams.WRAP_CONTENT);
            RelativeLayout.LayoutParams DeclineParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,RelativeLayout.LayoutParams.WRAP_CONTENT);
            AcceptParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            DeclineParams.addRule(RelativeLayout.RIGHT_OF, AcceptID);
            Accept.setLayoutParams(AcceptParams);
            Decline.setLayoutParams(DeclineParams);

            //This is the accept button
            Button AcceptButton = new Button(ctx);
            AcceptButton.setText("Accept");
            AcceptButton.setTextSize(10);
            AcceptButton.setOnClickListener(AcceptMeeting(Integer.parseInt(meeting.getString("id"))));

            //This is the decline button
            Button DeclineButton = new Button(ctx);
            DeclineButton.setText("Decline");
            DeclineButton.setTextSize(10);
            DeclineButton.setOnClickListener(DeclineMeeting(Integer.parseInt(meeting.getString("id"))));

            //Make those views contain the button
            Accept.addView(AcceptButton);
            Decline.addView(DeclineButton);

            //Add them to the button container
            if(DisplayAccept == 1){
                ButtonLayout.addView(Accept);
                ButtonLayout.addView(Decline);
            } else {
                ButtonLayout.addView(Decline);
            }
        } catch (Exception e){
            e.printStackTrace();
        }

        RelativeLayout.LayoutParams AcceptLayoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        AcceptLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        AcceptLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        ButtonLayout.setLayoutParams(AcceptLayoutParams);

        return ButtonLayout;
    }

    View.OnClickListener AcceptMeeting(final int meetingID){
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try{
                    //Perform a postrequest for accepting the meeting
                    JSONObject PostData = new JSONObject();
                    PostData.put("action","AcceptMeeting");
                    PostData.put("meetingID", meetingID);
                    PerformPostRequest(new OnJSONResponseCallback(){
                        @Override
                        public JSONArray onJSONResponse(boolean success, JSONArray response){
                            MakeGetMeetingsRequest();
                           return null;
                        };
                    }, PostData);

                    //Success message.
                    Toast.makeText(ctx, "Meeting accepted!", Toast.LENGTH_LONG).show();

                } catch (Exception e){
                    Toast.makeText(ctx, "Something went wrong.", Toast.LENGTH_LONG).show();
                }
            }
        };
    }

    View.OnClickListener DeclineMeeting(final int meetingID){
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try{
                    //Perform a postrequest for accepting the meeting
                    JSONObject PostData = new JSONObject();
                    PostData.put("action","DeclineMeeting");
                    PostData.put("meetingID", meetingID);
                    PerformPostRequest(new OnJSONResponseCallback(){
                        @Override
                        public JSONArray onJSONResponse(boolean success, JSONArray response){
                            MakeGetMeetingsRequest();
                            return null;
                        };
                    }, PostData);

                    //Success message.
                    Toast.makeText(ctx, "Meeting declined.", Toast.LENGTH_LONG).show();

                } catch (Exception e){
                    Toast.makeText(ctx, "Something went wrong.", Toast.LENGTH_LONG).show();
                }
            }
        };
    }

    public void IterateTomorrowMeetings(JSONObject AllTimeframes) throws JSONException{
        //This is the container for existing meetings
        RelativeLayout ExistingMeetingsContainer = findViewById(R.id.ExistingMeetingsContainer);

        //This container says "Tomorrow"
        RelativeLayout TomorrowHeader = new RelativeLayout(ctx);
        TextView TomorrowHeaderText = new TextView(ctx);
        TomorrowHeaderText.setText("Tomorrow:");
        TomorrowHeaderText.setTextSize(15);
        TomorrowHeader.addView(TomorrowHeaderText);
        RelativeLayout.LayoutParams TomorrowHeaderParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        TomorrowHeaderParams.addRule(RelativeLayout.BELOW, StaticID);
        TomorrowHeader.setLayoutParams(TomorrowHeaderParams);
        TomorrowHeader.setId(StaticID + 1);
        ExistingMeetingsContainer.addView(TomorrowHeader);
        ExistingMeetingsContainer.refreshDrawableState();

        //Increment staticID
        StaticID++;

        //Get all the meetings today
        JSONArray TomorrowMeetings = AllTimeframes.getJSONArray("tomorrow");

        //How many meetings are there.
        int MeetingsTodayCount = TomorrowMeetings.length();

        for(int i=0;i<MeetingsTodayCount;i++){
            //Build up the meeting data
            JSONObject meeting = new JSONObject(TomorrowMeetings.getString(i));
            String title = meeting.getString("title");
            String location = meeting.getString("location");
            String start = meeting.getString("start_time").substring(meeting.getString("start_time").length() -8, meeting.getString("start_time").length());
            int invited = new JSONArray(meeting.getString("invited")).length();
            int attending = new JSONArray(meeting.getString("attending")).length();
            int declined = new JSONArray(meeting.getString("declined")).length();

            //The meeting goes in a spannable layout so that it can be ordered.
            RelativeLayout IndividualMeetingContainer = new RelativeLayout(ctx);

            //Give it an ID so we can assign it parameters
            IndividualMeetingContainer.setId(StaticID + 1);

            //Each meeting is in a textview
            TextView Meeting = new TextView(ctx);

            //Build a spannable string to populate the meeting details
            SpannableString MeetingDetails = BuildMeetingSpannable(title, location, start, invited, attending, declined);
            Meeting.setText(MeetingDetails);
            Meeting.setX(10);

            //Add the textview to the container
            IndividualMeetingContainer.addView(Meeting);

            //Should the user be able to accept this meeting?
            if(meeting.getInt("can_accept") == 1){
                RelativeLayout AcceptButton = EnableAcceptButton(meeting, 1);
                IndividualMeetingContainer.addView(AcceptButton);
            } else {
                RelativeLayout AcceptButton = EnableAcceptButton(meeting, 0);
                IndividualMeetingContainer.addView(AcceptButton);
            }

            //The container needs to be positioned below the previous one
            RelativeLayout.LayoutParams ContainerParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);

            //Display this box below the previous one
            ContainerParams.addRule(RelativeLayout.BELOW, StaticID);
            ContainerParams.setMargins(0,0,0,20);

            //Apply the parameters
            IndividualMeetingContainer.setLayoutParams(ContainerParams);

            //Add the individual meeting container so it can be seen.
            ExistingMeetingsContainer.addView(IndividualMeetingContainer);

            //Increment static ID by one So we can place future meetings below it.
            StaticID++;
        }
    }

    public void IterateTodayMeetings(JSONObject AllTimeframes) throws JSONException{
        //This is the container for existing meetings
        RelativeLayout ExistingMeetingsContainer = findViewById(R.id.ExistingMeetingsContainer);

        //This container says "Today"
        RelativeLayout TodayHeader = new RelativeLayout(ctx);
        TextView TodayHeaderText = new TextView(ctx);
        TodayHeaderText.setText("Today:");
        TodayHeaderText.setTextSize(15);
        TodayHeader.addView(TodayHeaderText);
        TodayHeader.setId(StaticID);
        ExistingMeetingsContainer.addView(TodayHeader);
        ExistingMeetingsContainer.refreshDrawableState();

        //Get all the meetings today
        JSONArray TodayMeetings = AllTimeframes.getJSONArray("today");

        //How many meetings are there.
        int MeetingsTodayCount = TodayMeetings.length();
        String CountMessage = "";
        if(MeetingsTodayCount > 1){
            CountMessage = "You have " + MeetingsTodayCount + " meetings today.";
        } else {
            CountMessage = "You have " + MeetingsTodayCount + " meeting today.";
        }
        Toast.makeText(ctx, CountMessage, Toast.LENGTH_LONG).show();

        for(int i=0;i<MeetingsTodayCount;i++){
            //Build up the meeting data
            JSONObject meeting = new JSONObject(TodayMeetings.getString(i));
            String title = meeting.getString("title");
            String location = meeting.getString("location");
            String start = meeting.getString("start_time").substring(meeting.getString("start_time").length() -8, meeting.getString("start_time").length());
            int invited = new JSONArray(meeting.getString("invited")).length();
            int attending = new JSONArray(meeting.getString("attending")).length();
            int declined = new JSONArray(meeting.getString("declined")).length();

            //The meeting goes in a spannable layout so that it can be ordered.
            RelativeLayout IndividualMeetingContainer = new RelativeLayout(ctx);

            //Give it an ID so we can assign it parameters
            IndividualMeetingContainer.setId(StaticID + 1);

            //Each meeting is in a textview
            TextView Meeting = new TextView(ctx);

            //Build a spannable string to populate the meeting details
            SpannableString MeetingDetails = BuildMeetingSpannable(title, location, start, invited, attending, declined);
            Meeting.setText(MeetingDetails);
            Meeting.setX(10);

            //Add the textview to the container
            IndividualMeetingContainer.addView(Meeting);

            //Should the user be able to accept this meeting?
            if(meeting.getInt("can_accept") == 1){
                RelativeLayout AcceptButton = EnableAcceptButton(meeting, 1);
                IndividualMeetingContainer.addView(AcceptButton);
            } else {
                RelativeLayout AcceptButton = EnableAcceptButton(meeting, 0);
                IndividualMeetingContainer.addView(AcceptButton);
            }

            //The container needs to be positioned below the previous one
            RelativeLayout.LayoutParams ContainerParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);

            //Display this box below the previous one
            ContainerParams.addRule(RelativeLayout.BELOW, StaticID);
            ContainerParams.setMargins(0,0,0,20);

            //Apply the parameters
            IndividualMeetingContainer.setLayoutParams(ContainerParams);

            //Add the individual meeting container so it can be seen.
            ExistingMeetingsContainer.addView(IndividualMeetingContainer);

            //Increment static ID by one So we can place future meetings below it.
            StaticID++;
        }
    }

    public SpannableString BuildMeetingSpannable(String title, String location, String start, int invited, int attending, int declined){
        SpannableStringBuilder builder = new SpannableStringBuilder();

        //Add the meeting title
        String MeetingTitle = new String(title + "\n");

        //Add the meeting location
        String MeetingLocation = new String(location + " @ " + start.substring(0,5) + "\n");

        //Add the number of people attending out of those invited and those who declined
        String AttendingDetails = new String(attending + "/" + invited + " attending." + "\n" + declined + " declined");

        SpannableString MeetingDetails = new SpannableString(MeetingTitle + MeetingLocation + AttendingDetails);
        MeetingDetails.setSpan(new RelativeSizeSpan(2f),0, MeetingTitle.length(), 0);
        MeetingDetails.setSpan(new RelativeSizeSpan(1.3f), MeetingTitle.length(), MeetingTitle.length() + MeetingLocation.length(), 0);

        return MeetingDetails;
    }

    public interface OnJSONResponseCallback {
        public JSONArray onJSONResponse(boolean success, JSONArray response);
    }

    public void PerformPostRequest(final OnJSONResponseCallback callback, JSONObject PostData) {
        //To authenticate against the API we need the user's credentials
        String Email = getSharedPreferences(ctx).getString("Email","");
        String Password = getSharedPreferences(ctx).getString("Password","");

        final JSONArray[] ResponseStorage = new JSONArray[1];

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

        client.post(AppHost + "MyMeetings.php", RequestParameters, new AsyncHttpResponseHandler() {
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
};
