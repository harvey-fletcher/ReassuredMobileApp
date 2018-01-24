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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_reassured);

        final TextView go_back = findViewById(R.id.GoBackLink);

        go_back.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                finish();
            }
        });
    }

    static SharedPreferences getSharedPreferences(Context ctx){
        return PreferenceManager.getDefaultSharedPreferences(ctx);
    }

    public static void sign_out(Context ctx){
        SharedPreferences.Editor editor = getSharedPreferences(ctx).edit();
        editor.remove("Email");
        editor.remove("Password");
        editor.commit();
    }
}
