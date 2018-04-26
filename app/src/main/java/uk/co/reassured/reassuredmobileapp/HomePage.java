package uk.co.reassured.reassuredmobileapp;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class HomePage extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);

        //Set up to receive notifications
        Intent FireBaseMessages = new Intent(this, MyFirebaseMessagingService.class);
        startService(FireBaseMessages);

        //Remove notifications
        removeNotifications();

        final RelativeLayout signOut = findViewById(R.id.SignOutLink);
        final ImageView company_calendar = findViewById(R.id.companyCalendarButton);
        final ImageView lift_share = findViewById(R.id.liftSharingButton);
        final ImageView meetings = findViewById(R.id.meetingsButton);
        final ImageView my_reassured = findViewById(R.id.myReassuredButton);

        signOut.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                sign_out(HomePage.this);
                finish();
            }
        });

        company_calendar.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //Set up intent
                Intent companyCalendar = new Intent(HomePage.this, CompanyCalendar.class);

                //Open intent
                startActivity(companyCalendar);
            }
        });

        lift_share.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //Set up intent
                Intent liftShare = new Intent(HomePage.this, ReassuredTravel.class);

                //Open intent
                startActivity(liftShare);
            }
        });

        meetings.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //Set up intent
                Intent Meetings = new Intent(HomePage.this, Meetings.class);

                //Open intent
                startActivity(Meetings);
            }
        });

        my_reassured.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //Set up intent
                Intent MyReassured = new Intent(HomePage.this, MyReassured.class);

                //Open intent
                startActivity(MyReassured);
            }
        });

    };


    public void removeNotifications(){
        NotificationManager mNotifyMgr = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        mNotifyMgr.cancelAll();
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

    public static void sign_out(Context ctx){
        SharedPreferences.Editor editor = getSharedPreferences(ctx).edit();
        editor.remove("Email");
        editor.remove("Password");
        editor.remove("messages");
        editor.remove("user_conversations_with");
        editor.remove("conversations_array");
        editor.commit();
    };

};
