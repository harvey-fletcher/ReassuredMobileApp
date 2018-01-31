package uk.co.reassured.reassuredmobileapp;

import android.app.NotificationManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONObject;

/**
 * Created by Harvey on 28/01/2018.
 */

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private String TAG;

    public void onMessageReceived(RemoteMessage remoteMessage) {
        try{
            //Translate the response into useable stuff
            JSONObject messageData = new JSONObject(remoteMessage.getData());

            //This is used to display notifications
            NotificationCompat.Builder NB = new NotificationCompat.Builder(this)
                    .setSmallIcon(R.drawable.icon_transparent_background);

            System.out.println("APPLICATION RECEIVED NOTIFICATION: \n \n " +messageData);

            //Decide what type of notification it is
            String notification_type = messageData.getString("notification_type");
            if(notification_type.matches("traffic")){
                NB.setContentTitle("Traffic Info!");
                NB.setContentText("Check your route before you travel, there is a reported incident.");
            } else if(notification_type.matches("calendar")) {

            } else if(notification_type.matches("late")){
                NB.setContentTitle("Information:");
                NB.setContentText(messageData.getString("affected_user") + " is running late.");
            } else if(notification_type.matches("meeting")){

            } else if(notification_type.matches("myreassured")){

            } else if(notification_type.matches("message")){
                NB.setContentTitle("New message from " + messageData.getString("from_user_name"));

                if(messageData.getString("message_body").length() > 20){
                    NB.setContentText(messageData.getString("message_body").substring(0,20) + "...");
                } else {
                    NB.setContentText(messageData.getString("message_body"));
                }
            }


            //Set a notification for the ID
            int mNotificationID = 001;

            //Get an instance of the notification manager service
            NotificationManager mNotifyMgr = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);

            //Build the notification and issue it
            mNotifyMgr.notify(mNotificationID, NB.build());
        } catch (Exception e){
            e.printStackTrace();
        }
    }

}
