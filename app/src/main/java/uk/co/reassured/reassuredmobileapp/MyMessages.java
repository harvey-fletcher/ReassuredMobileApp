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
import android.view.ViewGroup;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import cz.msebera.android.httpclient.impl.client.cache.HeapResource;

/**
 * Created by Harvey on 01/02/2018.
 */

public class MyMessages extends AppCompatActivity {

    //Used for converstion pagination
    public int display_page = 1;

    //These are the elements on the page. Their actions are set later.
    public TextView goBack;
    public TextView Header;
    public TextView moreConversations;
    public TextView lessConversations;
    public RelativeLayout MB;
    public RelativeLayout container;


    //This will stop the mainBody from reloading as conversations display
    public int MessageViewMode;

    //This is for loading individual conversations
    public int ConversationID;

    //Used for re checking messages every 5 seconds
    public Timer timer = new Timer();

    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_messages);

        //This is the go back link
        goBack = (TextView)findViewById(R.id.GoBackLink);

        //This is the "More" link for conversations, it will start hidden and display if there are more than 5 convo
        moreConversations = (TextView)findViewById(R.id.showMore);
        moreConversations.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                display_page++;
                produceConversations(MyMessages.this);
            }
        });

        //This is the "Less" link for conversations, it will start hidden and display if display page is greater than 1
        lessConversations = (TextView)findViewById(R.id.showLess);
        lessConversations.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                display_page--;
                produceConversations(MyMessages.this);
            }
        });

        //The bit that sys "My Messages"
        Header = (TextView)findViewById(R.id.messagesTitle);

        //Recheck for new conversations every 5 seconds
        timer.schedule(new checkConversation(),0, 5000);

        //Enable auto refreshing of the conversations list.
        MessageViewMode = 0;

        //Hide the text box until its needed
         container = (RelativeLayout)findViewById(R.id.textboxContainer);
         container.setVisibility(View.INVISIBLE);

        //This is the main panel
        MB = findViewById(R.id.mainBody);
    }

    public static SharedPreferences SharedPrefs(Context ctx){
        return PreferenceManager.getDefaultSharedPreferences(ctx);
    }

    public class checkConversation extends TimerTask{

        @Override
        public void run() {
            if(MessageViewMode == 0) {
                produceConversations(MyMessages.this);
            } else if(MessageViewMode == 1){
                individualConversationMessages(MyMessages.this);
            }
        }
    }

    public void produceConversations(final Context ctx){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //Enable auto refreshing of the conversations list.
                MessageViewMode = 0;

                //This will close the conversations
                goBack.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        finish();
                    }
                });

                //Display dimensions
                Display display = getWindowManager().getDefaultDisplay();
                int width = display.getWidth();
                int BGColor = 0;

                try {
                    //This is where the conversations will be displayed (the most recent message)
                    MB.removeAllViews();

                    //Get the array of conversations.
                    JSONArray conversations_array = new JSONArray(SharedPrefs(ctx).getString("conversations_array",""));

                    //Conversations to show in this page.
                    int conversation = (5 * display_page) - 5;

                    //How many different conversations are there?
                    int total_conversations = conversations_array.length();

                    //Only display the "More" link if there are more than 5 conversations
                    if(total_conversations > (5 * display_page)){
                        moreConversations.setVisibility(View.VISIBLE);
                    } else {
                        moreConversations.setVisibility(View.INVISIBLE);
                    }

                    //Only display the "Less" link if there is a page to go back to.
                    if(display_page > 1){
                        lessConversations.setVisibility(View.VISIBLE);
                    } else {
                        lessConversations.setVisibility(View.INVISIBLE);
                    }

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
                        ConversationPreview.setOnClickListener(getOnClickDoSomething(conversation, conversations_array, user_name));
                        MB.addView(ConversationPreview);
                        MB.refreshDrawableState();

                        //This is where the next item will be placed
                        ConversationPreview.measure(0,0);
                        default_y_axis+= ConversationPreview.getMeasuredHeight();

                        conversation++;
                    } while ((conversation < total_conversations) && (conversation < (display_page * 5)));
                } catch (Exception e){
                    e.printStackTrace();
                }
            }
        });
    }

    View.OnClickListener getOnClickDoSomething(final int conversation, final JSONArray Conversations, final String DisplayUserName)  {
        return new View.OnClickListener() {
            public void onClick(View v) {
                //Disable auto refreshing of the conversations list and enable individual conversation loading and refreshing.
                MessageViewMode = 1;

                //The conversation that has been opened
                ConversationID = conversation;

                //Clear the main body
                MB.removeAllViews();

                //Hide the more and less links.
                moreConversations.setVisibility(View.INVISIBLE);
                lessConversations.setVisibility(View.INVISIBLE);

                //Change the header
                Header.setText(DisplayUserName);

                //Change the go back link so it opens all conversations again.
                goBack.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Header.setText("My Messages");
                        produceConversations(MyMessages.this);
                        sendMessageBox(MyMessages.this, 0);
                    }
                });

                //These are the messages for the converstion
                try{
                    //Set up so that the individual conversation is in view and auto refreshes
                    MessageViewMode = 1;
                    individualConversationMessages(MyMessages.this);
                    sendMessageBox(MyMessages.this, 1);
                } catch (Exception e){
                    e.printStackTrace();
                }
            };
        };
    }

    public void individualConversationMessages(final Context ctx){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //Clear the screen
                MB.removeAllViews();

                //Set up the screen dimensions
                Display display = getWindowManager().getDefaultDisplay();

                //This is where the individual conversation will get stored
                JSONArray ConversationMessages = new JSONArray();

                int container_id = 0;

                try {
                    //Get all conversations on the device
                    JSONArray Conversations = new JSONArray(SharedPrefs(ctx).getString("conversations_array",""));

                    //Store only a single conversation
                    ConversationMessages = Conversations.getJSONArray(ConversationID);

                    //This is how many messages are in the conversation.
                    int MessageCount = ConversationMessages.length();

                    //Where the first message will be placed
                    int default_y_position = 20;

                    for(int i=0;i<MessageCount;i++){
                        //Get the message we are displaying
                        JSONObject MessageData = ConversationMessages.getJSONObject(i);

                        //Set all the messages in the conversation to read so that they no longer appear.
                        MessageData.put("read",1);

                        //This is where the message will be displayed
                        RelativeLayout MessageContainer = new RelativeLayout(ctx);
                        MessageContainer.setMinimumWidth(display.getWidth());

                        //This is the message information we will be displaying.
                        TextView message_text = new TextView(ctx);

                        //Set the message data
                        String body = "\n" + MessageData.getString("sent") + "\n" + MessageData.getString("message");
                        message_text.setText(body);

                        //Make those a nice easy to read size
                        message_text.setTextSize(13);

                        //Add the message text and sent time to the container
                        MessageContainer.addView(message_text);
                        MessageContainer.setMinimumHeight(message_text.getMeasuredHeight() + 20);

                        //Measure the message container so the next one can be positioned below it
                        MessageContainer.measure(0,0);


                        //Layout params
                        if(container_id != 0) {
                            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                            params.addRule(RelativeLayout.BELOW, container_id);
                            MessageContainer.setLayoutParams(params);
                        }

                        container_id++;
                        MessageContainer.setId(container_id);

                        //Add the message container to the view of messages
                        MB.addView(MessageContainer);

                        //Save all the messages in this conversation to red status
                        SharedPrefs(ctx).edit().putString("conversations_array", Conversations.toString()).commit();
                    }
                } catch (Exception e){
                    TextView message = new TextView(ctx);
                    message.setTextSize(15);
                    message.setTextColor(Color.parseColor("#ff0000"));
                    message.setText("Error \n \n Something went wrong \n \n" + e.getClass().getSimpleName());
                    MB.addView(message);
                }


            }
        });
    }

    public void sendMessageBox(Context ctx, int mode){
        //The mode integer will show or hide the textbox
        if(mode == 1){
            container.setVisibility(View.VISIBLE);
        } else {
            container.setVisibility(View.INVISIBLE);
        }
    }
}
