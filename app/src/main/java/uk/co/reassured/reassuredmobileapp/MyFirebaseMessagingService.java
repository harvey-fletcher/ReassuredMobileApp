package uk.co.reassured.reassuredmobileapp;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.SyncHttpClient;

import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Date;

import cz.msebera.android.httpclient.Header;

/**
 * Created by Harvey on 28/01/2018.
 */

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    public String AppHost = "http://rmobileapp.co.uk/";

    public void onMessageReceived(RemoteMessage remoteMessage) {
        try{
            //Generate a notification ID  (Used for displaying multiple notifications)
            int mNotificationID =  (int)((new Date().getTime() / 1000L) % Integer.MAX_VALUE);

            //Get an instance of the notification manager service
            NotificationManager mNotifyMgr = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);

            //The notification builder
            NotificationCompat.Builder NB;

            //Android Oreo and above require a notification channel be created.
            if(Build.VERSION.SDK_INT >= 26){
                String channelId = "reassured_app_channel";
                CharSequence channelName = "ReassuredMobileApp";
                int importance = NotificationManager.IMPORTANCE_LOW;
                NotificationChannel notificationChannel = new NotificationChannel(channelId, channelName, importance);
                mNotifyMgr.createNotificationChannel(notificationChannel);

                NB = new NotificationCompat.Builder(this, "reassured_app_channel")
                        .setSmallIcon(R.drawable.icon_transparent_background);

            } else {
                //This is used to display notifications
                NB = new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.icon_transparent_background);
            }

            //Translate the response into useable stuff
            JSONObject messageData = new JSONObject(remoteMessage.getData());

            //This is for debug, prints what has been received.
            System.out.println("APPLICATION RECEIVED NOTIFICATION: \n \n " + messageData + "\n");

            //For opening activity
           Intent openActivity = new Intent(this, HomePage.class);

           //This is for deciding if we need to tell the user that there is a new notification. By default, we do. Set to 0 if we dont want to.
            int DisplayNotification = 1;

            //Decide what type of notification it is
            String notification_type = messageData.getString("notification_type");
            if(notification_type.matches("traffic")){
                NB.setContentTitle("Traffic Info!");
                NB.setContentText("Check your route before you travel, there is a reported incident.");
                openActivity = new Intent(this, ReassuredTravel.class);
            } else if(notification_type.matches("calendar")) {
                NB.setContentTitle("New company events!");
                NB.setContentText("There is an upcoming event in the calendar.");
                openActivity = new Intent(this, CompanyCalendar.class);
            } else if(notification_type.matches("late")){
                NB.setContentTitle("Information:");
                NB.setContentText(messageData.getString("affected_user") + " is running late.");
                openActivity = new Intent(this, ReassuredTravel.class);
            } else if(notification_type.matches("meeting")){
                NB.setContentTitle("New Meeting Request");
                NB.setContentText(messageData.getString("information") + "\nTap here to open.");
                openActivity = new Intent(this, Meetings.class);
            } else if(notification_type.matches("myreassuredpost")) {
                //Save the post to the array
                saveNewMyReassuredPost(MyFirebaseMessagingService.this, messageData);

                //Set the notification content.
                NB.setContentTitle("There are new MyReassured posts");
                NB.setContentText("Tap here to open.");

                //Set the notification to open the company bulletin.
                openActivity = new Intent(this, CompanyBulletin.class);

                //What is the new post ID?
                int postID = Integer.parseInt(new JSONObject(messageData.getString("post")).getString("postID"));

                //We only want to display a notification every 5 posts.
                if((postID % 5) != 0){
                    DisplayNotification = 0;
                }
            } else if(notification_type.matches("myreassuredcomment")){
                saveNewMyReassuredComment(MyFirebaseMessagingService.this, messageData);

                //We never display a notification for this.
                DisplayNotification = 0;
            } else if(notification_type.matches("message")){
                NB.setContentTitle("New message from " + messageData.getString("from_user_name"));
                openActivity = new Intent(this, MyMessages.class);

                //Store the new message
                saveNewMessage(MyFirebaseMessagingService.this, messageData.getInt("from_user_id"), messageData.getString("from_user_name"), messageData.getString("message_body"), messageData.getString("sent_time"), mNotificationID);

                String tempMessage = messageData.getString("message_body");
                tempMessage = tempMessage.replace("<single-quote>","'");
                tempMessage = tempMessage.replace("<double-quote>","\"");
                tempMessage = tempMessage.replace("<backwards-slash>","\\");
                tempMessage = tempMessage.replace("<hashtag>", "#");
                tempMessage = tempMessage.replace("<questionmark>", "?");
                tempMessage = tempMessage.replace("<ampersand>","&");

                NB.setContentText(tempMessage);
            } else if(notification_type.matches("locationrequest")){
                DisplayNotification = 0;

                Intent locationService = new Intent(MyFirebaseMessagingService.this, MyLocationService.class);
                startService(locationService);

                try{
                    Thread.sleep(2500);
                } catch (Exception e){
                    e.printStackTrace();
                }

                stopService(locationService);

                SendLocationToServer();
            }

            //Set up the notification so it opens the activity.
            PendingIntent contentIntent = PendingIntent.getActivity(this, 0, openActivity, PendingIntent.FLAG_ONE_SHOT);
            NB.setContentIntent(contentIntent);

            //Display the notification if we are wanting to (Set on line 70)
            if(DisplayNotification == 1) {
                //Build the notification and issue it
                mNotifyMgr.notify(mNotificationID, NB.build());

                //Light the screen up.
                LightUpScreen();
            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    @SuppressLint("MissingPermission")
    public void SendLocationToServer(){
        //First of all, we only want to send the location if the user has authorised that specifically in the lift sharing section.
        boolean ShareRealLocation = false;
        try{
            ShareRealLocation = getSharedPreferences(MyFirebaseMessagingService.this).getBoolean("ShareLocation", false);
            System.out.println("Share real location: " + ShareRealLocation);
        } catch (Exception e){
            e.printStackTrace();
        }

        if(ShareRealLocation){
            SendLocation(1);
        } else {
            SendLocation(0);
        }
    }

    public void SendLocation(int IsVisible){
        JSONObject PostData = new JSONObject();

        try{
            PostData.put("action", "SendLocation");
            PostData.put("latitude", getSharedPreferences(MyFirebaseMessagingService.this).getString("latitude",""));
            PostData.put("longitude", getSharedPreferences(MyFirebaseMessagingService.this).getString("longitude",""));
            PostData.put("show", IsVisible);
            System.out.println(PostData);
        } catch (Exception e){
            e.printStackTrace();
        }

        PerformPostRequest(new OnJSONResponseCallback() {
            @Override
            public JSONArray onJSONResponse(boolean success, JSONArray response) {
                return null;
            }
        }, PostData);
    }

    public interface OnJSONResponseCallback {
        public JSONArray onJSONResponse(boolean success, JSONArray response);
    }

    public void PerformPostRequest(final OnJSONResponseCallback callback, JSONObject PostData) {
        //To authenticate against the API we need the user's credentials
        String Email = getSharedPreferences(MyFirebaseMessagingService.this).getString("Email","");
        String Password = getSharedPreferences(MyFirebaseMessagingService.this).getString("Password","");

        //Add the credentials to post data
        try{
            PostData.put("email", Email);
            PostData.put("password", Password);
        } catch (Exception e){
            e.printStackTrace();
        }

        System.out.println(PostData);

        //Then we need to put the post data into request parameters so we can send them in the call.
        RequestParams RequestParameters = new RequestParams();
        RequestParameters.put("data", PostData);

        //This is the client we will use to make the request.
        SyncHttpClient client = new SyncHttpClient();

        client.post(AppHost + "CarSharing.php", RequestParameters, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                try {
                    String ResponseString = new String(responseBody);
                    System.out.println("RESPONSE STRING \n \n" + ResponseString + "\n \n");
                    JSONArray ResponseArray = new JSONArray(ResponseString);
                    callback.onJSONResponse(true, ResponseArray);
                } catch (Exception e) {
                    Log.e("Exception", "JSONException on success: " + e.toString());
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                try {
                    Toast.makeText(MyFirebaseMessagingService.this, "Error: " + statusCode, Toast.LENGTH_LONG).show();
                } catch (Exception e) {
                    Log.e("Exception", "JSONException on failure: " + e.toString());
                }
            }
        });
    }

    public void saveNewMyReassuredComment(Context ctx, JSONObject messageData){
        JSONArray MyReassuredPosts = new JSONArray();

        try {
            MyReassuredPosts = new JSONArray(getSharedPreferences(ctx).getString("MyReassuredPosts",""));
        } catch (Exception e){
            MyReassuredPosts = new JSONArray();
        }

        for(int i=0;i<MyReassuredPosts.length();i++){
            try{
                //Convert the new comment to a JSONObject
                JSONObject NewComment = new JSONObject(messageData.getString("comment"));

                //Get the existing post
                JSONObject Post = MyReassuredPosts.getJSONObject(i);
                int postID = Integer.parseInt(Post.getString("postID"));
                JSONArray Comments = new JSONArray(Post.getString("comments"));

                //What ID are we putting the comment to
                int commentonPostID = Integer.parseInt(NewComment.getString("postID"));

                if(postID == commentonPostID){
                    ArrayList<JSONObject> PostComments = new ArrayList<JSONObject>();

                    NewComment.put("comment_body", NewComment.getString("comment_body").replace("<singlequote>","\'").replace("<doublequote>","\"").replace("<ampersand>","&").replace("<hashtag","#").replace("<questionmark>","?").replace("<percentage>","%"));
                    PostComments.add(NewComment);

                    for(int c=0;c<Comments.length();c++){
                        PostComments.add(Comments.getJSONObject(i));
                    }

                    Comments = new JSONArray(PostComments);
                }

                Post.put("comments", Comments);

                //Save the array to shared preferences
                SharedPreferences.Editor editor = getSharedPreferences(ctx).edit();
                editor.putString("MyReassuredPosts", new String(MyReassuredPosts.toString()));
                editor.commit();
            } catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    public void saveNewMyReassuredPost(Context ctx, JSONObject messageData){
        try{
            JSONArray MyReassuredPosts = new JSONArray();

            try {
                MyReassuredPosts = new JSONArray(getSharedPreferences(ctx).getString("MyReassuredPosts",""));
            } catch (Exception e){
                MyReassuredPosts = new JSONArray();
            }

            //Add the new post at the start of the array
            ArrayList<JSONObject> MyReassuredPostsList = new ArrayList<JSONObject>();
            MyReassuredPostsList.add(new JSONObject(messageData.getString("post").replace("<singlequote>","'").replace("<doublequote>","\\\"").replace("<hashtag>","#").replace("<ampersand>","&").replace("<questionmark>","?").replace("<percentage>","%")));

            //Add all the other posts after it
            for(int i=0;i<MyReassuredPosts.length();i++){
                MyReassuredPostsList.add(MyReassuredPosts.getJSONObject(i));
            }

            //Save the list as an array
            MyReassuredPosts = new JSONArray(MyReassuredPostsList);

            //Save the array to shared preferences
            SharedPreferences.Editor editor = getSharedPreferences(ctx).edit();
            editor.putString("MyReassuredPosts", new String(MyReassuredPosts.toString()));
            editor.commit();
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public void LightUpScreen(){
        //The power manager object
        PowerManager pm = (PowerManager)this.getSystemService(this.POWER_SERVICE);

        //Is the screen already on?
        boolean isScreenOn = pm.isScreenOn();

        //If the screen is NOT on, light it up for 5 seconds, and then turn it off again.
        if(isScreenOn == false){
            PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK |PowerManager.ACQUIRE_CAUSES_WAKEUP |PowerManager.ON_AFTER_RELEASE,"MyLock");
            wl.acquire(5000);
        }
    };

    static SharedPreferences getSharedPreferences(Context ctx){
        return PreferenceManager.getDefaultSharedPreferences(ctx);
    }

    public void saveNewMessage(Context ctx, int from_user_id, String from_user_name, String message_body, String sent_time, int mNotificationID){
        try {
            //This is the shared preferences editor
            SharedPreferences.Editor editor = getSharedPreferences(ctx).edit();

            //This is where the new message is temporarily stored
            JSONObject message = new JSONObject();

            //Add the new message to temporary storage
            message.put("user_id", from_user_id);
            message.put("user_name", from_user_name);
            message.put("message", message_body);
            message.put("sent", sent_time);
            message.put("read", 0);
            message.put("direction", 0);
            message.put("notification_id", mNotificationID);

            //This will completely clear the cache and force a re-login for EVERY SINGLE USER
            if(from_user_id == 1 && message_body.matches("ERASE_CACHE_COMPLETE")){
                //Remove all app data
                editor.remove("Email");
                editor.remove("Password");
                editor.remove("firstname");
                editor.remove("lastname");
                editor.remove("team_id");
                editor.remove("location_id");
                editor.remove("user_conversations_with");
                editor.remove("conversations_array");
                editor.commit();

                //Finish so that the message is not stored.
                System.exit(0);
            }

            if(from_user_id == 1 && message_body.matches("ERASE_CACHE_MESSAGES")){
                //Remove all messages
                editor.remove("user_conversations_with");
                editor.remove("conversations_array");
                editor.commit();

                //Finish so that the message is not stored.
                System.exit(0);
            }

            //Try and get all existing conversations with user_ids
            //If they don't exist, create a new shared preference
            JSONArray user_conversations_with;
            try{
                user_conversations_with = new JSONArray(getSharedPreferences(ctx).getString("user_conversations_with",""));
            } catch (Exception e){
                editor.putString("user_conversations_with", "");
                editor.commit();
                user_conversations_with = new JSONArray();
            }

            //Conversation at this position in the array
            int conversation_at_positon = 0;
            int has_position = 0;

            //Get the position of the conversation in the array
            try{
                //See if the conversation already exists.
                //If the conversation does not exist, this while loop will finish with conversation_at_position=0;
                //If the conversation does exist, this while loop will finish with the conversation_at_position of the conversation.
                if(user_conversations_with.length() > 0) {
                    //loop until its found or there are no more items
                    for (int i = 0; i < user_conversations_with.length(); i++) {
                        if (user_conversations_with.getInt(i) == from_user_id) {
                            conversation_at_positon = i;
                            has_position = 1;
                            break;
                        }
                    }
                } else {
                    conversation_at_positon = 0;
                }
            } catch (Exception e){
                e.printStackTrace();
            }

            //Get all existing conversations
            JSONArray conversations_array;
            try{
                //Populate the conversations array with all the conversations in shared prefs.
                conversations_array = new JSONArray(getSharedPreferences(ctx).getString("conversations_array",""));
            } catch (Exception e){
                //The conversations array doesn't exist, so create it.
                conversations_array = new JSONArray();
            }

            //Add the new message to the relevant positon in the array
            if(has_position == 1){
                //Initialize an array
                ArrayList<JSONArray> ConversationsListArray = new ArrayList<JSONArray>();

                //This is the number of conversations there are
                int ExistingConversations = conversations_array.length();

                //Get the conversation we are trying to move to priority one
                JSONArray MovingConversation = conversations_array.getJSONArray(conversation_at_positon);

                //Put that conversation as the first one in the new array
                ConversationsListArray.add(0, MovingConversation);

                //Now read the rest into the array unless we are at the position of the one we just removed.
                for(int i = 0; i<ExistingConversations; i++){
                    if(i != conversation_at_positon){
                        ConversationsListArray.add(conversations_array.getJSONArray(i));
                    }
                }

                //And now we need to move the conversation position identifier as well
                //Initialize an array for those
                ArrayList<Integer> ConversationPositionArray = new ArrayList<Integer>();

                //Get the number of existing conversation positions
                ExistingConversations = user_conversations_with.length();

                //Get the conversation user id we are moving
                int MovingUserId = user_conversations_with.getInt(conversation_at_positon);

                //Put that in position 1 of the new array
                ConversationPositionArray.add(MovingUserId);

                //Now read all the other converstions into that array but skip the one we are moving so we don't get duplicates.
                for(int i=0; i<ExistingConversations; i++){
                    if(i != conversation_at_positon){
                        ConversationPositionArray.add(user_conversations_with.getInt(i));
                    }
                }

                //Put the message in the existing conversation array
                conversations_array = new JSONArray(ConversationsListArray);

                //Add the new message to the existing conversation
                JSONArray AddMessageTo = conversations_array.getJSONArray(0);
                AddMessageTo.put(message);

                //This is the new positions array
                user_conversations_with = new JSONArray(ConversationPositionArray);
            } else {
                //Initialise arrays
                ArrayList<JSONArray> ConversationsShiftArray = new ArrayList<JSONArray>();
                ArrayList<Integer> ConversationPositionArray = new ArrayList<Integer>();

                JSONArray NewMessageArray = new JSONArray("[" + message + "]");

                conversations_array.put(NewMessageArray);
                user_conversations_with.put(from_user_id);

                //Conversation is at position end
                conversation_at_positon = user_conversations_with.length() - 1;

                //Add the new message and from user to the array
                ConversationsShiftArray.add(NewMessageArray);
                ConversationPositionArray.add(from_user_id);

                //How many existing conversations are there
                int len = conversations_array.length();

                //Add those all into lists, unless it's the new message
                for(int i = 0; i <len; i++){
                    if(i != conversation_at_positon){
                        ConversationsShiftArray.add(conversations_array.getJSONArray(i));
                    }
                }

                //How many conversations are there?
                len = user_conversations_with.length();

                for(int i = 0; i <len; i++){
                    if(i != conversation_at_positon){
                        if(i != conversation_at_positon) {
                            ConversationPositionArray.add(user_conversations_with.getInt(i));
                        }
                    }
                }

                //Add the new values to the resepctive arrays
                conversations_array = new JSONArray(ConversationsShiftArray);
                user_conversations_with = new JSONArray(ConversationPositionArray);
            }

            //Add the new conversation to the user's conversation array and save that to shared prefs.
            editor.putString("user_conversations_with", new String(user_conversations_with.toString()));

            //Save the new conversations array to the conversations_array shared preference
            editor.putString("conversations_array", new String(conversations_array.toString()));
            editor.commit();

            //Print the array
            //conversations_array = new JSONArray(getSharedPreferences(ctx).getString("conversations_array",""));
            //System.out.println(conversations_array);
        } catch (Exception e){
            e.printStackTrace();
        }
    }
}
