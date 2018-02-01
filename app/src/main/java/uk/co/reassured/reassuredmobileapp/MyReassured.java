package uk.co.reassured.reassuredmobileapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.Image;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONArray;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by hfletcher on 24/01/2018.
 */

public class MyReassured extends AppCompatActivity {

    //Where is the app API hosted?
    private String AppHost = "http://82.10.188.99/api/";

    //This is for looping message count check every 5 seconds
    public Timer timer = new Timer();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //Load the MyReassured layout
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_reassured);

        //This is the "Go Back" link
        final TextView go_back = findViewById(R.id.GoBackLink);

        //When the "Go Back" link is clicked, close this activity (Will display the main screen)
        go_back.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                finish();
            }
        });

        //Check for new unread messages every 2.5 seconds
        timer.schedule(new getCount(), 0, 2500);

        //Set up what happens when the messages icon is clicked.
        ImageView messagesButton = (ImageView)findViewById(R.id.myMessagesButton);
        messagesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent messagesActivity = new Intent(MyReassured.this, MyMessages.class);
                startActivity(messagesActivity);
            }
        });
    }

    public class getCount extends TimerTask{
        @Override
        public void run() {
            getMessageCount(MyReassured.this);
        }
    }

    public static SharedPreferences getSharedPreferences(Context ctx){
        return PreferenceManager.getDefaultSharedPreferences(ctx);
    }

    public static String get_user_id(Context ctx)
    {
        return getSharedPreferences(ctx).getString("id", "");
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

    public static String getTeamId(Context ctx)
    {
        return getSharedPreferences(ctx).getString("team_id", "");
    }

    public static String getLocationId(Context ctx)
    {
        return getSharedPreferences(ctx).getString("location_id", "");
    }

    public void getMessageCount(Context ctx){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                String messagesCount = "0";

                try{
                    String messages = getSharedPreferences(MyReassured.this).getString("messages","");
                    JSONArray allMessages = new JSONArray(messages);

                    int position = 0;
                    int UnreadMSG = 0;

                    do {
                        //If the message is unread, add 1 to the count
                        if(allMessages.getJSONObject(0).getInt("read") == 0){
                            UnreadMSG++;
                        }
                        position++;
                    } while (position < allMessages.length());

                    if(UnreadMSG > 9){
                        messagesCount = "9+";
                    } else {
                        messagesCount = Integer.toString(UnreadMSG);
                    }
                } catch (Exception e){
                    SharedPreferences.Editor editor = getSharedPreferences(MyReassured.this).edit();
                    editor.putString("messages", "[]");
                    editor.commit();
                }


                //This is the number of messages
                TextView messageCount = (TextView)findViewById(R.id.messageCount);
                messageCount.setText(messagesCount);
                messageCount.setTextColor(ContextCompat.getColor(MyReassured.this, R.color.reassuredPurple));
            }
        });
    }

}
