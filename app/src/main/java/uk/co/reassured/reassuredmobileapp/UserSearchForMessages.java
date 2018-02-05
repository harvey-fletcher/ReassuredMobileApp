package uk.co.reassured.reassuredmobileapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.view.ScrollingView;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

import cz.msebera.android.httpclient.Header;

/**
 * Created by Harvey on 04/02/2018.
 */

public class UserSearchForMessages extends AppCompatActivity {
    //The search field
    public EditText SearchBox;

    //The scroll view
    public ScrollView ResultsScroller;

    //This is where the API is
    public String AppHost = "http://82.10.188.99/api/";

    public void onCreate(Bundle savedInstanceState){
        //Set up the layout
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_search_message);

        //This is the link to close the search
        TextView goBackLink = findViewById(R.id.GoBackLink);
        goBackLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        //Now that we have loaded the layout, associate the objects
        SearchBox = findViewById(R.id.UserSearchBox);
        ResultsScroller = findViewById(R.id.resultsScroller);

        //Add a text listener to the search box to find when it changes
        SearchBox.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if(SearchBox.getText().toString().length() >= 2) {
                    getSearchResults(SearchBox.getText().toString());
                } else {
                    ResultsScroller.removeAllViews();
                }
            }
        });

    }

    public static SharedPreferences SharedPrefs(Context ctx){
        return PreferenceManager.getDefaultSharedPreferences(ctx);
    }

    public void getSearchResults(String SearchTerm){
        try{
            AsyncHttpClient client = new AsyncHttpClient();
            Context ctx = UserSearchForMessages.this;
            String Email = SharedPrefs(ctx).getString("Email","");
            String Password = SharedPrefs(ctx).getString("Password","");

            //This is where we are going to look for users matching the search criteria. Search filtering is done server side using the SearchTerm
            String url = AppHost + "social.php?list_users=true&email=" + Email + "&password=" + Password + "&search=" + SearchTerm;

            //Send a request to the server.
            client.get(url, new AsyncHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                    //Build a string from the returned bytes
                    String resultsAsString = new String(responseBody);
                    try {
                        //Build an array out of the results string
                        JSONArray resultsAsArray = new JSONArray(resultsAsString);

                        //Pass that array to the function that displays results
                        displayResults(resultsAsArray);
                    } catch (Exception e){
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                    //Display an error message with an error code.
                    Toast.makeText(UserSearchForMessages.this, "Something went wrong: " + statusCode, Toast.LENGTH_LONG).show();
                }
            });
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public void displayResults(JSONArray Results){
        //How many results are there in total?
        int ResultsCount = Results.length();

        //This is the context
        Context ctx = UserSearchForMessages.this;

        //This is the container where the results are put
        RelativeLayout ResultsContainer = new RelativeLayout(ctx);

        //This is so the user can scroll the search results
        ResultsScroller.removeAllViews();

        //For every result, build a clickable object which will start a new conversation stub
        for(int i=0; i < ResultsCount; i++){
            //Declare variables
            String FullName = "";
            String OfficeLocation = "";
            int user_id = 0;

            //Build the user details for the clickable result object
            try{
                FullName = Results.getJSONObject(i).getString("firstname") + " " + Results.getJSONObject(i).getString("lastname");
                OfficeLocation = Results.getJSONObject(i).getString("location_name") + "\n";
                user_id = Results.getJSONObject(i).getInt("id");
            } catch (Exception e){
                e.printStackTrace();
            }

            //Build the clickable object
            RelativeLayout ResultRecord = new RelativeLayout(ctx);

            //Create a textview then add the user details to the textview
            TextView Result = new TextView(ctx);
            String Details = FullName + "\n" + OfficeLocation;
            Result.setText(Details);

            //Add the user details textview to the clickable object
            ResultRecord.addView(Result);
            ResultRecord.setId(i + 1);

            //If the object is NOT the first result, add it below the previous one
            if(i > 0){
                RelativeLayout.LayoutParams ResultContainerLayout = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                ResultContainerLayout.addRule(RelativeLayout.BELOW, i);
                ResultRecord.setLayoutParams(ResultContainerLayout);
            }

            //Set the clickable object to start a new conversation stub message
            ResultRecord.setOnClickListener(getOnClickDoSomething(user_id, FullName));

            //Add the clickable object to the results container
            ResultsContainer.addView(ResultRecord);
        }

        //Add the results container to the scrolling view so that the user can scroll through results
        ResultsScroller.addView(ResultsContainer);
    }

    View.OnClickListener getOnClickDoSomething(final int user_id, final String user_name){
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //This is the conversation stub creator, initialise the mainbody, and clear anything else from it.
                RelativeLayout MB = findViewById(R.id.mainBody);
                MB.removeAllViews();

                //Meausure the size of the display
                Display display = getWindowManager().getDefaultDisplay();
                int width = display.getWidth();
                int height = display.getHeight();

                //Set the conversation title and align it center top
                TextView Title = new TextView(UserSearchForMessages.this);
                Title.setText("New chat with " + user_name);
                Title.setWidth(width);
                Title.setGravity(Gravity.CENTER);
                Title.setTextSize(20);

                //This is a textbox for the message text
                final EditText MessageText = new EditText(UserSearchForMessages.this);
                MessageText.setWidth(width);
                MessageText.setHeight(height / 2);
                MessageText.setGravity(Gravity.TOP);
                MessageText.setY(50);
                MessageText.setHint("Message...");

                //This is the send button
                Button SendButton = new Button(UserSearchForMessages.this);
                SendButton.setWidth(width);
                SendButton.setHeight(25);
                SendButton.setText("Send");
                SendButton.setY((height / 2) + 75);

                //When the send button is clicked, prepare the message, which will add it to the JSON Array
                //The list of conversations will then be displayed.
                SendButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        prepareMessage(user_id, user_name, MessageText.getText().toString());
                        finish();
                    }
                });

                //Display all the fields on the page.
                MB.setGravity(Gravity.CENTER_HORIZONTAL);
                MB.addView(Title);
                MB.addView(MessageText);
                MB.addView(SendButton);
            }
        };
    };

    public void prepareMessage(int user_id, String user_name, String NewMessageText){
        //Make that save for submissions.
        NewMessageText = NewMessageText.replace("'","<single-quote>");
        NewMessageText = NewMessageText.replace("\"","<double-quote>");
        NewMessageText = NewMessageText.replace("\\","<backwards-slash>");
        NewMessageText = NewMessageText.replace("&","<ampersand>");


        //Build up the message JSON object
        JSONObject NewMessage = new JSONObject();
        try{
            JSONArray Conversations = new JSONArray();
            try{
                //Try to get conversations from the sharedpreferences
                Conversations = new JSONArray(SharedPrefs(UserSearchForMessages.this).getString("conversations_array",""));
            } catch (Exception JSE){
                //If getting the sharedpreferences conversations failed, assume it doesn't exist and create a new conversations array
                Conversations = new JSONArray();
            }

            //Add a new conversation at the end of the conversations array.
            Conversations.put(new JSONArray());

            //What's the time
            Date date = new Date();
            String timeHours = Integer.toString(date.getHours());
            String timeMinutes = Integer.toString(date.getMinutes());
            if(Integer.parseInt(timeHours) < 10){
                timeHours = "0" + timeHours;
            }
            if(Integer.parseInt(timeMinutes) < 10){
                timeMinutes = "0" + timeMinutes;
            }
            String timeHM = timeHours + ":" + timeMinutes;

            //Build the message JSON Object
            NewMessage.put("user_id", user_id);
            NewMessage.put("user_name", user_name);
            NewMessage.put("message", NewMessageText);
            NewMessage.put("sent", timeHM);
            NewMessage.put("read", 1);
            NewMessage.put("direction",1);

            //Add that message to the conversation
            Conversations.getJSONArray(Conversations.length() -1).put(NewMessage);

            //This is the array that is used so when new messages are received, they get added to the correct conversations
            JSONArray user_conversations_with;
            try {
                //Try and get this array from shared preferences
                user_conversations_with = new JSONArray(SharedPrefs(UserSearchForMessages.this).getString("user_conversations_with", ""));
            } catch (Exception e){
                //If that fails, assume it doesn't exist, and create a new array
                user_conversations_with = new JSONArray();
            }

            //Add the new user ID to the user conversations with array
            user_conversations_with.put(user_id);

            //Save those in shared preferences.
            SharedPreferences.Editor editor = SharedPrefs(UserSearchForMessages.this).edit();
            editor.putString("conversations_array", Conversations.toString());
            editor.putString("user_conversations_with", user_conversations_with.toString());
            editor.commit();

            //Construct the URL
            String url = AppHost + "notifications.php?email=" + SharedPrefs(UserSearchForMessages.this).getString("Email","") + "&password=" + SharedPrefs(UserSearchForMessages.this).getString("Password","") + "&notification_type=message&to_group=individual&user_id=" + user_id + "&message_body=" + NewMessageText;

            //Send the message
            sendMessage(UserSearchForMessages.this, url);

        } catch (Exception e){
            e.printStackTrace();
            Toast.makeText(UserSearchForMessages.this, "Couldn't send message (internal error)", Toast.LENGTH_LONG).show();
        }
    }

    public void sendMessage(final Context ctx, String url){
        try{
            AsyncHttpClient client = new AsyncHttpClient();

            client.get(url, new AsyncHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, cz.msebera.android.httpclient.Header[] headers, byte[] responseBody) {
                    Toast.makeText(ctx, "Message Sent!", Toast.LENGTH_LONG).show();
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                    Toast.makeText(ctx, "Couldn't send message (server error)", Toast.LENGTH_LONG).show();
                }
            });
        } catch (Exception e){
            Toast.makeText(ctx, "Couldn't send message (internal error)", Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }
}
