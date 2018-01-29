package uk.co.reassured.reassuredmobileapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.TextView;

public class Meetings extends AppCompatActivity {

    //Where is the app API hosted?
    private String AppHost = "http://82.10.188.99/api/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meetings);

        final TextView go_back = findViewById(R.id.GoBackLink);

        go_back.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                finish();
            }
        });
    };

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
};
