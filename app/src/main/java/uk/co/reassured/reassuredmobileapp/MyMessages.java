package uk.co.reassured.reassuredmobileapp;

import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.DrawableContainer;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.text.Layout;
import android.view.Display;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Harvey on 01/02/2018.
 */

public class MyMessages extends AppCompatActivity {

    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_messages);

        produceConversations(MyMessages.this);
    }

    public static SharedPreferences SharedPrefs(Context ctx){
        return PreferenceManager.getDefaultSharedPreferences(ctx);
    }

    public void produceConversations(Context ctx){
        Display display = getWindowManager().getDefaultDisplay();
        int width = display.getWidth();
        int BGColor = 0;

        try {
            //This is where the conversations will be displayed (the most recent message)
            RelativeLayout mainBody = findViewById(R.id.mainBody);

            //Get the array of conversations.
            JSONArray conversations_array = new JSONArray(SharedPrefs(ctx).getString("conversations_array",""));

            //Always start at array position 0
            int conversation = 0;

            //How many different conversations are there?
            int total_conversations = conversations_array.length();

            //Start the first conversation message display at 20
            int default_y_axis = 20;

            //Loop through every conversation
            do{
                //The container for the message preview
                RelativeLayout ConversationPreview = new RelativeLayout(MyMessages.this);

                //The textview where the most recent message will be shown.
                TextView conversationPartner = new TextView(ctx);
                TextView conversationPreview = new TextView(ctx);

                //Get all the messages in that conversation
                JSONArray currentConversation = conversations_array.getJSONArray(conversation);

                //String the message preview details
                String user_name = "";
                String message_body = "";

                //The conversation statistics
                int ConversationLength = currentConversation.length();

                //Check inward messages for the last message
                //Start at message 9
                int MessagePosition = 0;
                do{
                    //Store the message
                    JSONObject message = currentConversation.getJSONObject(MessagePosition);

                    //Get the message direction (0 is in)
                    int Direction = message.getInt("direction");

                    //If it's an inward message, save it
                    if(Direction == 0){
                        user_name = message.getString("user_name");
                        message_body = message.getString("message");
                    }

                    //Increment to the next message
                    MessagePosition++;
                } while (MessagePosition < ConversationLength);

                //If getting an inward message was unsuccessful, get an outward one instead.
                MessagePosition = 0;
                do{
                    //Store the message
                    JSONObject message = currentConversation.getJSONObject(MessagePosition);

                    //Get the message direction (1 is out)
                    int Direction = message.getInt("direction");

                    //If it's an inward message, save it
                    if(Direction == 1){
                        user_name = message.getString("user_name");
                        message_body = message.getString("message");
                    }

                    //Increment to the next message
                    MessagePosition++;
                } while (MessagePosition < ConversationLength);

                //This is the conversation partner
                conversationPartner.setText(user_name);
                conversationPartner.setTextSize(30);
                int preview_y = Math.round(conversationPartner.getX() + conversationPartner.getTextSize());


                //Shorten the preview if it's over than 25 chars
                if(message_body.length() > 25){
                    message_body = message_body.substring(0,25) + "...";
                }

                //This is the message preview text
                conversationPreview.setText(message_body);
                conversationPreview.setMinimumWidth(width);
                conversationPreview.setTextSize(20);
                conversationPreview.setX(20);
                conversationPreview.setY(preview_y);

                //Add the conversation to the main body
                ConversationPreview.addView(conversationPartner);
                ConversationPreview.addView(conversationPreview);
                ConversationPreview.setMinimumHeight(Math.round(preview_y + conversationPreview.getTextSize() + 20));
                ConversationPreview.setMinimumWidth(width);
                ConversationPreview.setY(default_y_axis);

                //Decide what shade this message is
                if(BGColor == 0){
                    ConversationPreview.setBackgroundColor(Color.parseColor("#ededed"));
                    BGColor = 1;
                } else {
                    ConversationPreview.setBackgroundColor(Color.parseColor("#ffffff"));
                    BGColor = 0;
                }

                //Add that to mainbody
                mainBody.addView(ConversationPreview);
                mainBody.refreshDrawableState();

                //This is where the next item will be placed
                ConversationPreview.measure(0,0);
                default_y_axis+= ConversationPreview.getMeasuredHeight();

                conversation++;
            } while (conversation < total_conversations);
        } catch (Exception e){
            e.printStackTrace();
        }
    }
}
