package uk.co.reassured.reassuredmobileapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

/**
 * Created by hfletcher on 24/01/2018.
 */

public class MyReassured extends AppCompatActivity {

    //Where is the app API hosted?
    private String AppHost = "http://82.10.188.99/api/";

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

}
