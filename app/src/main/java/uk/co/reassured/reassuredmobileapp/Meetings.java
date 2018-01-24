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

public class Meetings extends AppCompatActivity {

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

    public static void sign_out(Context ctx){
        SharedPreferences.Editor editor = getSharedPreferences(ctx).edit();
        editor.remove("Email");
        editor.remove("Password");
        editor.commit();
    };

};
