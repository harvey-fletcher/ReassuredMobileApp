package uk.co.reassured.reassuredmobileapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

public class Meetings extends AppCompatActivity {

    //Where is the app API hosted?
    private String AppHost = "http://82.10.188.99/api/";

    //This is the context
    private Context ctx = Meetings.this;

    //This is used for storing responses
    public JSONArray XMLResponse = new JSONArray();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meetings);

        //This is the button which will make the user go back to the home page.
        final TextView go_back = findViewById(R.id.GoBackLink);
        go_back.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                finish();
            }
        });

        JSONObject PostData = new JSONObject();
        try{
            PostData.put("action","test");
        } catch (Exception e){
            e.printStackTrace();
        }

        getJSONObj(new OnJSONResponseCallback(){
            @Override
            public void onJSONResponse(boolean success, JSONArray response){
                System.out.println("RESPONSE! \n \n " + response + " \n \n -------------------------------");
            }
        }, PostData);
    };

    public interface OnJSONResponseCallback {
        public void onJSONResponse(boolean success, JSONArray response);
    }

    public JSONObject getJSONObj(final OnJSONResponseCallback callback, JSONObject PostData) {
        //To authenticate against the API we need the user's credentials
        String Email = getSharedPreferences(ctx).getString("Email","");
        String Password = getSharedPreferences(ctx).getString("Password","");

        //Add the credentials to post data
        try{
            PostData.put("email", Email);
            PostData.put("password", Password);
        } catch (Exception e){
            e.printStackTrace();
        }

        //Then we need to put the post data into request parameters so we can send them in the call.
        RequestParams RequestParameters = new RequestParams();
        RequestParameters.put("data", PostData);

        //This is the client we will use to make the request.
        AsyncHttpClient client = new AsyncHttpClient();

        client.post(AppHost + "MyMeetings.php", RequestParameters, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                try {
                    String ResponseString = new String(responseBody);
                    JSONArray ResponseArray = new JSONArray(ResponseString);
                    callback.onJSONResponse(true, ResponseArray);
                } catch (Exception e) {
                    Log.e("Exception", "JSONException on success: " + e.toString());
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                try {
                    Toast.makeText(ctx, "Error: " + statusCode, Toast.LENGTH_LONG).show();
                } catch (Exception e) {
                    Log.e("Exception", "JSONException on failure: " + e.toString());
                }
            }
        });

        return null;
    }

    public static SharedPreferences getSharedPreferences(Context ctx){
        return PreferenceManager.getDefaultSharedPreferences(ctx);
    }
};
