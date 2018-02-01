package uk.co.reassured.reassuredmobileapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONObject;

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

            //Build the conversations based on user ID
            JSONArray talking_to = new JSONArray();
            JSONArray conversations = new JSONArray();


            do{
                //Only display each conversation once on the list.
                if(!talking_to.toString().contains(messages.getJSONObject(position).getString("user_name"))){
                    talking_to.put(messages.getJSONObject(position).getString("user_name"));

                    conversations.put("["+ messages.getJSONObject(position) +"]");
                } else {
                    int conPOS = 0;

                    do{
                        if(new JSONArray(conversations.getString(conPOS)).getJSONObject(0).getString("user_name").matches(messages.getJSONObject(position).getString("user_name"))){
                           break;
                        } else {
                            conPOS++;
                        }
                    } while (conPOS < conversations.length());

                    JSONObject currentConversationObject = new JSONArray(conversations.getString(conPOS)).getJSONObject(0);
                    String currentConversationString = new String(currentConversationObject.toString());

                    currentConversationString+= "," + messages.getJSONObject(position).toString();

                    conversations.put(conPOS, "[" + currentConversationString + "]");
                }

                position++;
            } while (position < messages.length());

            System.out.println(conversations);
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public void displayConversations(JSONObject fi){

    }
}
