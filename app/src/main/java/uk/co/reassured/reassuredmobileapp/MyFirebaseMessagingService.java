package uk.co.reassured.reassuredmobileapp;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONObject;

import java.util.Date;

/**
 * Created by Harvey on 28/01/2018.
 */

public class MyFirebaseMessagingService extends FirebaseMessagingService {
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
            System.out.println("APPLICATION RECEIVED NOTIFICATION: \n \n " +messageData);

            //For opening activity
           Intent openActivity = new Intent(this, HomePage.class);

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

            } else if(notification_type.matches("myreassured")){

            } else if(notification_type.matches("message")){
                NB.setContentTitle("New message from " + messageData.getString("from_user_name"));
                openActivity = new Intent(this, MyReassured.class);

                if(messageData.getString("message_body").length() > 20){
                    NB.setContentText(messageData.getString("message_body").substring(0,20) + "...");
                } else {
                    NB.setContentText(messageData.getString("message_body"));
                }
            }

            //Set up the notification so it opens the activity.
            PendingIntent contentIntent = PendingIntent.getActivity(this, 0, openActivity, PendingIntent.FLAG_ONE_SHOT);
            NB.setContentIntent(contentIntent);

            //Build the notification and issue it
            mNotifyMgr.notify(mNotificationID, NB.build());

            //Light the screen up.
            LightUpScreen();

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

}
