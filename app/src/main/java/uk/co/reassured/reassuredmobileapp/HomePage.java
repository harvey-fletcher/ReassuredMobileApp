package uk.co.reassured.reassuredmobileapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class HomePage extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);
    }

    public void sign_out(View view){
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(HomePage.this).edit();
        editor.remove("Email");
        editor.remove("Password");
        editor.commit();

        finish();
    }
}
