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

import static uk.co.reassured.reassuredmobileapp.CompanyCalendar.sign_out;

public class HomePage extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);

        final TextView signOut = findViewById(R.id.SignOutLink);
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
                Intent liftShare = new Intent(HomePage.this, LiftShare.class);

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

    public static SharedPreferences getSharedPreferences(Context ctx){
        return PreferenceManager.getDefaultSharedPreferences(ctx);
    }

    public static void sign_out(Context ctx){
        SharedPreferences.Editor editor = getSharedPreferences(ctx).edit();
        editor.remove("Email");
        editor.remove("Password");
        editor.commit();
    };

};
