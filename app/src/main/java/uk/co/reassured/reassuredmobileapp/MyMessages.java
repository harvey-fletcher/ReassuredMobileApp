package uk.co.reassured.reassuredmobileapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Harvey on 01/02/2018.
 */

public class MyMessages extends AppCompatActivity {

    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_messages);

        produceConversations(MyMessages.this);
    }

    public static SharedPreferences SharedPrefs(Context ctx){
        return PreferenceManager.getDefaultSharedPreferences(ctx);
    }

    public void produceConversations(Context ctx){
        try {
            RelativeLayout mainBody = findViewById(R.id.mainBody);

            JSONArray conversations_array = new JSONArray(SharedPrefs(ctx).getString("conversations_array",""));

            int conversation = 0;
            int total_conversations = conversations_array.length();
            int default_y_axis = 20;

            do{
                TextView newTextView = new TextView(ctx);
                newTextView.setText(conversations_array.getJSONArray(conversation).toString());
                newTextView.setY(default_y_axis);
                newTextView.measure(0,0);
                default_y_axis = default_y_axis + newTextView.getMeasuredHeight() + 20;
                mainBody.addView(newTextView);



                conversation++;
            } while (conversation < total_conversations);

            System.out.println(conversations_array);
        } catch (Exception e){
            e.printStackTrace();
        }
    }
}
