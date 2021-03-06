package uk.co.reassured.reassuredmobileapp;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import cz.msebera.android.httpclient.Header;

/**
 * Created by Harvey on 01/02/2018.
 */

public class MyMessages extends AppCompatActivity {

    //ClassGlobals variables
    ClassGlobals classGlobals = new ClassGlobals();

    //Used for converstion pagination
    public int display_page = 1;

    //These are the elements on the page. Their actions are set later.
    public RelativeLayout goBack;
    public TextView Header;
    public TextView moreConversations;
    public TextView lessConversations;
    public RelativeLayout MB;
    public RelativeLayout container;
    public ImageView AddConversationButton;


    //This will stop the mainBody from reloading as conversations display
    public int MessageViewMode;

    //This is for loading individual conversations
    public int ConversationID;

    //Used for re checking messages every 5 seconds
    public Timer timer = new Timer();

    //This is where conversations are displayed.
    public ScrollView MessagesScrollingView;

    //This is to see if we need to scroll the scrollview
    public int TotalConversationMessages = 0;

    //Who the converstion is with
    public int user_id = 0;

    //This is where the individual conversation will get stored
    JSONArray ConversationMessages = new JSONArray();
    JSONArray Conversations = new JSONArray();

    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_messages);

        //This is the go back link
        goBack = (RelativeLayout)findViewById(R.id.GoBackLink);

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
        timer.schedule(new checkConversation(),0, 2000);

        //Enable auto refreshing of the conversations list.
        MessageViewMode = 0;

        //Hide the text box until its needed
         container = (RelativeLayout)findViewById(R.id.textboxContainer);
         container.setVisibility(View.INVISIBLE);

        //This is the main panel
        MB = findViewById(R.id.mainBody);

        //This is the button for adding conversations
        AddConversationButton = findViewById(R.id.addChatButton);
        AddConversationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent UserSearch = new Intent(MyMessages.this, UserSearchForMessages.class);
                startActivity(UserSearch);
            }
        });

        TextView OnDemandRefreshLink = findViewById(R.id.OnDemandRefreshLink);
        OnDemandRefreshLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try{
                    //Let the user know they have performed the action
                    Toast.makeText(MyMessages.this, "Please wait...", Toast.LENGTH_LONG).show();

                    JSONObject PostData = new JSONObject();

                    PostData.put("action","refresh");

                    PerformPostRequest(new OnJSONResponseCallback() {
                        @Override
                        public JSONArray onJSONResponse(boolean success, JSONArray response) {
                            return null;
                        }
                    }, PostData);
                } catch (Exception e){
                    e.printStackTrace();
                }
            }
        });
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
                if(TotalConversationMessages > 0) {
                    individualConversationMessages(MyMessages.this, 1);
                } else {
                    individualConversationMessages(MyMessages.this, 0);
                }
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
                        String from_user_name = "";
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

                            //This is the user details of the conversation
                            user_name = message.getString("user_name");
                            message_body = message.getString("message");

                            //This gets used on the message preview (Who sent the last message)
                            if(message.getInt("direction") == 0) {
                                String user = message.getString("user_name");
                                from_user_name = user.substring(0, user.indexOf(" "));
                            } else {
                                from_user_name = "you";
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
                        conversationPreview.setText(from_user_name + ": " +message_body);
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
                    TextView NoMessages = new TextView(ctx);
                    String NoMessageText = "There are no conversations. \n \n Click the plus button below to start a new one.";
                    NoMessages.setText(NoMessageText);
                    NoMessages.setX(20);
                    NoMessages.setY(20);
                    NoMessages.setTextSize(15);
                    MB.addView(NoMessages);
                }

                AddConversationButton.setVisibility(View.VISIBLE);
            }
        });
    }

    View.OnClickListener getOnClickDoSomething(final int conversation, final JSONArray Conversations, final String DisplayUserName)  {
        return new View.OnClickListener() {
            public void onClick(View v) {
                //Hide the add conversation button
                AddConversationButton.setVisibility(View.INVISIBLE);

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
                    individualConversationMessages(MyMessages.this, 0);
                    sendMessageBox(MyMessages.this, 1);
                } catch (Exception e){
                    e.printStackTrace();
                }
            };
        };
    }

    public void individualConversationMessages(final Context ctx, final int isRefreshing){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (isRefreshing == 0) {
                    //Clear all the views
                    MB.removeAllViews();
                }

                //Set up the screen dimensions
                Display display = getWindowManager().getDefaultDisplay();

                int container_id = 0;

                //The messages go in this container which goes in the scrollbox
                RelativeLayout messages_container_view = new RelativeLayout(ctx);

                try {
                    //Get all conversations on the device
                    Conversations = new JSONArray(SharedPrefs(ctx).getString("conversations_array", ""));

                    //Store only a single conversation
                    ConversationMessages = Conversations.getJSONArray(ConversationID);

                    //This is how many messages are in the conversation.
                    int MessageCount = ConversationMessages.length();

                    for(int i = 0;i<MessageCount;i++){
                        //Get the message we are displaying
                        JSONObject MessageData = ConversationMessages.getJSONObject(i);

                        //Message container
                        RelativeLayout MessageContainer = new RelativeLayout(ctx);

                        //Set all the messages in the conversation to read so that they no longer appear.
                        MessageData.put("read",1);

                        //Set the user_id of the conversation
                        user_id = MessageData.getInt("user_id");

                        //This is where the message will be displayed
                        MessageContainer.setMinimumWidth(display.getWidth());

                        //This is the message information we will be displaying.
                        TextView message_text = new TextView(ctx);

                        //Set the message data
                        String body = "\n" + MessageData.getString("sent") + "\n" + MessageData.getString("message");
                        message_text.setText(body);

                        //Make those a nice easy to read size
                        message_text.setTextSize(20);

                        //Add the message text and sent time to the container
                        MessageContainer.addView(message_text);
                        MessageContainer.setMinimumHeight(message_text.getMeasuredHeight() + 10);

                        //Try to remove the notification
                        try{
                            NotificationManager mNotifyMgr = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
                            int mNotificationID = MessageData.getInt("notification_id");

                            if(mNotificationID > 0) {
                                mNotifyMgr.cancel(MessageData.getInt("notification_id"));
                                MessageData.put("notification_id", 0);
                                System.out.println("Notification Removed! :)");
                            }
                        } catch (Exception e){
                            System.out.println("Notification NOT removed ===>" + e.getClass().getSimpleName());
                        }

                        //Layout params
                        if(container_id != 0) {
                            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                            params.addRule(RelativeLayout.BELOW, container_id);
                            MessageContainer.setLayoutParams(params);
                        }

                        container_id++;
                        MessageContainer.setId(container_id);

                        //Received messages come in orange
                        if(MessageData.getInt("direction") == 0) {
                            MessageContainer.setBackgroundColor(Color.parseColor("#FE8A00"));
                        } else {
                            MessageContainer.setGravity(Gravity.END);
                            message_text.setGravity(Gravity.END);
                            message_text.measure(0,0);
                        }

                        //Add the message container to the view of messages
                        messages_container_view.addView(MessageContainer);

                        //Save all the messages in this conversation to red status
                        SharedPrefs(ctx).edit().putString("conversations_array", Conversations.toString()).commit();
                    }
                } catch (Exception e){
                    //If something goes wrong, show an error message in red
                    TextView message = new TextView(ctx);
                    message.setTextSize(15);
                    message.setTextColor(Color.parseColor("#ff0000"));
                    message.setText("Error \n \n Something went wrong \n \n" + e.getClass().getSimpleName());
                    MB.addView(message);
                }

                if(isRefreshing == 0 || container_id > TotalConversationMessages) {

                    if(isRefreshing == 1){
                        MB.removeAllViews();
                    }

                    MessagesScrollingView = new ScrollView(MyMessages.this);

                    //Add the messages into the scrolling view
                    MessagesScrollingView.addView(messages_container_view);

                    //Add a new scrollview and go to the bottom
                    MessagesScrollingView.post(new Runnable() {
                        @Override
                        public void run() {
                            MessagesScrollingView.fullScroll(View.FOCUS_DOWN);
                        }
                    });

                    //Add the scrollview to the main body
                    MB.addView(MessagesScrollingView);
                }

               TotalConversationMessages = container_id;
            }
        });
    }

    public void sendMessageBox(final Context ctx, int mode){
        //The mode integer will show or hide the textbox
        if(mode == 1){
            container.setVisibility(View.VISIBLE);

            Button SendMessageButton = (Button)findViewById(R.id.sendMessage);
            final EditText MessageTextField = (EditText)findViewById(R.id.messageTextbox);

            SendMessageButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //Hide the soft keyboard from view.
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);

                    //Get the textbox field
                    String NewMessageText = MessageTextField.getText().toString();

                    //Build up the message JSON object
                    JSONObject NewMessage = new JSONObject();
                    try{
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
                        NewMessage.put("user_name", Header.getText());
                        NewMessage.put("message", NewMessageText);
                        NewMessage.put("sent", timeHM);
                        NewMessage.put("read", 1);
                        NewMessage.put("direction",1);
                        NewMessage.put("notification_id", 0);

                        //Add that message to the conversation
                        ConversationMessages.put(NewMessage);

                        //Save that conversation in the array
                        Conversations.put(ConversationID, ConversationMessages);

                        //Save those in shared preferences.
                        SharedPreferences.Editor editor = SharedPrefs(ctx).edit();
                        editor.putString("conversations_array", Conversations.toString());
                        editor.commit();

                        //Clear the text field
                        MessageTextField.setText("");

                        //Construct the message in a JSONObject so we can make a post request
                        JSONObject PostData = new JSONObject();
                        try{
                            PostData.put("action", "send");
                            PostData.put("to_user_id", user_id);
                            PostData.put("message_body", NewMessageText);
                        } catch (Exception e){
                            e.printStackTrace();
                            return;
                        }

                        //Send the message
                        PerformPostRequest(new OnJSONResponseCallback() {
                            @Override
                            public JSONArray onJSONResponse(boolean success, JSONArray response) {
                                System.out.println(response);
                                return null;
                            }
                        }, PostData);

                    } catch (Exception e){
                        Toast.makeText(ctx, "Couldn't send message (internal error)", Toast.LENGTH_LONG).show();
                    }
                }
            });

        } else {
            container.setVisibility(View.INVISIBLE);
        }
    }

    //Because we are performing post requests to the new api, we need to use an interface
    public interface OnJSONResponseCallback{
        public JSONArray onJSONResponse(boolean success, JSONArray response);
    }

    //This function performs post requests to the server
    public void PerformPostRequest(final OnJSONResponseCallback callback, JSONObject PostData) {
        //To authenticate against the API we need the user's credentials
        String Email = SharedPrefs(MyMessages.this).getString("Email","");
        String Password = SharedPrefs(MyMessages.this).getString("Password","");

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

        client.post(classGlobals.AppHost + "MyMessages.php", RequestParameters, new AsyncHttpResponseHandler() {
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
                    Toast.makeText(MyMessages.this, "Error: " + statusCode, Toast.LENGTH_LONG).show();
                } catch (Exception e) {
                    Log.e("Exception", "JSONException on failure: " + e.toString());
                }
            }
        });
    }
}
