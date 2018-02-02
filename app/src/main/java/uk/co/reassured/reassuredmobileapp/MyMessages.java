package uk.co.reassured.reassuredmobileapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;

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
            //Get the messages
            JSONArray messages = new JSONArray(SharedPrefs(ctx).getString("messages",""));

            //Start at position 0
            int position = 0;

            //How many messages are there?
            int MessageCount = messages.length();

            //This is where a list of user_conversations are stored
            JSONArray user_conversations = new JSONArray("[0]");

            do{
                int from_id = messages.getJSONObject(position).getInt("user_id");

                int at_position = 0;
                for(int i = 0; i< user_conversations.length(); i++){
                    if(user_conversations.getInt(i) == from_id){
                        at_position = i;
                        break;
                    }
                }

                if(at_position == 0){
                    user_conversations.put(from_id);
                }

                position++;
            } while (position < messages.length());

            //Remove user ID 0, that user will NEVER exist
            ArrayList<String> list = new ArrayList<String>();
            int len = user_conversations.length();
            for(int i=0; i<len;i++){
                list.add(user_conversations.get(i).toString());
            }
            list.remove(0);
            user_conversations = new JSONArray(list);

            //Set up the conversations array to have enough sub array
            len = user_conversations.length();
            position = 0;

            JSONArray conversations_array = new JSONArray();
            do{
                conversations_array.put(new JSONArray());
                position++;
            } while (position < len);

            position = 0;

            do{
                int from_id = messages.getJSONObject(position).getInt("user_id");

                int at_position = 0;
                for(int i = 0; i< user_conversations.length(); i++){
                    if(user_conversations.getInt(i) == from_id){
                        at_position = i;
                        break;
                    }
                }


                conversations_array.getJSONArray(at_position).put(messages.getJSONObject(position));

                position++;
            } while (position < messages.length());

            System.out.println(conversations_array);
        } catch (Exception e){
            e.printStackTrace();
        }
    }
}
