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
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by hfletcher on 24/01/2018.
 */

public class MyReassured extends AppCompatActivity {

    //ClassGlobals variables
    ClassGlobals classGlobals = new ClassGlobals();

    //This is for looping message count check every 5 seconds
    public Timer timer = new Timer();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //Load the MyReassured layout
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_reassured);

        //This is the "Go Back" link
        final RelativeLayout go_back = findViewById(R.id.GoBackLink);

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
                Intent messagesActivity = new Intent(ReassuredMobileApp.getAppContext(), MyMessages.class);
                startActivity(messagesActivity);
            }
        });

        //Set up what happens when the company bulletin button is clicked.
        final ImageView CompanyBulletinButton = (ImageView)findViewById(R.id.CompanyBulletinButton);
        CompanyBulletinButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent companyBulletin = new Intent(ReassuredMobileApp.getAppContext(), CompanyBulletin.class);
                startActivity(companyBulletin);
            }
        });

        //This button will open the IT Service Desk
        ImageView ITServiceDeskButton = (ImageView)findViewById(R.id.ServiceDeskButton);
        ITServiceDeskButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent ITServiceDesk = new Intent(ReassuredMobileApp.getAppContext(), ITServiceDesk.class);
                startActivity(ITServiceDesk);
            }
        });

        //This button will open user settings top level
        ImageView UserSettings = (ImageView)findViewById(R.id.UserSettingsButton);
        UserSettings.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Intent UserSettingsTop = new Intent(ReassuredMobileApp.getAppContext(), UserSettingsTopLevel.class);
                startActivity(UserSettingsTop);
            }
        });
    }

    public class getCount extends TimerTask{
        @Override
        public void run() {
            getMessageCount(ReassuredMobileApp.getAppContext());
        }
    }

    public void getMessageCount(final Context ctx){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                int MessagesCount = 0;

                try{
                    //Get conversations and how many there are
                    JSONArray ConversationsArray = new JSONArray(classGlobals.sharedPrefs().getString("conversations_array",""));
                    int ConversationsCount = ConversationsArray.length();
                    int conversation = 0;

                    do{
                        //Get the specific conversation and the number of messages contained within it
                        JSONArray Conversation = ConversationsArray.getJSONArray(conversation);
                        int MessagesInConversation = Conversation.length();

                        //Find the number of those messages that are unread
                        int message = 0;
                        do{
                            //Get the specific message and the read status
                            JSONObject CurrentMessage = Conversation.getJSONObject(message);
                            int ReadStatus = CurrentMessage.getInt("read");

                            //If the message is unread, count it, else don't
                            if(ReadStatus == 0){
                                MessagesCount++;
                            }

                            message++;
                        } while (message < MessagesInConversation);

                        conversation++;
                    } while ((conversation < ConversationsCount));
                } catch (Exception e){
                    MessagesCount = 0;
                }


                //This is the number of messages
                TextView messageCount = (TextView)findViewById(R.id.messageCount);

                if(MessagesCount > 9) {
                    messageCount.setText("9+");
                } else {
                    messageCount.setText(Integer.toString(MessagesCount));
                }
                messageCount.setTextColor(ContextCompat.getColor(ReassuredMobileApp.getAppContext(), R.color.reassuredPurple));
            }
        });
    }

}
