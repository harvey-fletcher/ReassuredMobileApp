package uk.co.reassured.reassuredmobileapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.function.Function;

import cz.msebera.android.httpclient.Header;

/**
 * Created by hfletcher on 05/04/2018.
 */

public class ITServiceDesk extends AppCompatActivity {

    //ClassGlobals file
    ClassGlobals classGlobals = new ClassGlobals();

    //Used for loading API Function
    private String ApiFunction = "inbound";

    public void onCreate(Bundle savedInstanceState){
        //Load the application bundle
        super.onCreate(savedInstanceState);

        //Load the correct content view.
        setContentView(R.layout.activity_it_service_desk);

        //when the submit button is clicked
        Button SubmitButton = findViewById(R.id.submit_ticket);
        SubmitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                submitTicket();
            }
        });

        //When "Go Back" is clicked.
        RelativeLayout GoBack = findViewById(R.id.GoBackLink);
        GoBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    public void submitTicket(){
        //These are the fields
        EditText title_field = findViewById(R.id.ticket_title);
        EditText desc_field = findViewById(R.id.ticket_desc);

        //Get the values
        String title = title_field.getText().toString();
        String desc = desc_field.getText().toString();

        //Check they aren't blank
        if(title.matches("")){
            Toast.makeText(ITServiceDesk.this, "Title cannot be blank.", Toast.LENGTH_LONG).show();
            return;
        } else if(desc.matches("")){
            Toast.makeText(ITServiceDesk.this, "Description cannot be blank.", Toast.LENGTH_LONG).show();
            return;
        }

        //Create a PostData for parameters
        JSONObject PostData = new JSONObject();

        //Apply the values from the form
        try{
            PostData.put("subject", title);
            PostData.put("email_body", desc);
        } catch (Exception e){
            Toast.makeText(ITServiceDesk.this, "There was an unexpected error.", Toast.LENGTH_LONG).show();
            return;
        }

        //Send the new servicedesk request
        PerformPostRequest(new OnJSONResponseCallback(){
            @Override
            public JSONObject onJSONResponse(boolean success, JSONObject response) {
                if(response.has("success")){
                    Toast.makeText(ITServiceDesk.this, "Your ticket has been submitted!", Toast.LENGTH_LONG).show();
                    finish();
                } else {
                    Toast.makeText(ITServiceDesk.this, "There was an unexpected error.\nPlease try again.", Toast.LENGTH_LONG).show();
                }

                return null;
            }
        }, PostData);
    }

    //Because we are performing post requests to the new api, we need to use an interface
    public interface OnJSONResponseCallback{
        public JSONObject onJSONResponse(boolean success, JSONObject response);
    }

    //This function performs post requests to the server
    public void PerformPostRequest(final OnJSONResponseCallback callback, JSONObject PostData) {
        //To authenticate against the API we need the user's credentials
        String Email = SharedPrefs(this).getString("Email","");
        String Password = SharedPrefs(this).getString("Password","");

        //Add the credentials to post data
        try{
            PostData.put("function", ApiFunction);
            PostData.put("email", Email);
            PostData.put("password", Password);
        } catch (Exception e){
            e.printStackTrace();
        }

        System.out.println(PostData);

        //Then we need to put the post data into request parameters so we can send them in the call.
        RequestParams RequestParameters = new RequestParams();
        RequestParameters.put("", PostData);

        //This is the client we will use to make the request.
        AsyncHttpClient client = new AsyncHttpClient();

        client.post(classGlobals.AppHost + "ITServiceDesk.php", RequestParameters, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                try {
                    String ResponseString = new String(responseBody);
                    JSONObject ResponseObject = new JSONObject(ResponseString);
                    callback.onJSONResponse(true, ResponseObject);
                } catch (Exception e) {
                    Log.e("Exception", "JSONException on success: " + e.toString());
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                try {
                    Toast.makeText(ITServiceDesk.this, "Error: " + statusCode, Toast.LENGTH_LONG).show();
                } catch (Exception e) {
                    Log.e("Exception", "JSONException on failure: " + e.toString());
                }
            }
        });
    }

    public static SharedPreferences SharedPrefs(Context ctx){
        return PreferenceManager.getDefaultSharedPreferences(ctx);
    }
}
