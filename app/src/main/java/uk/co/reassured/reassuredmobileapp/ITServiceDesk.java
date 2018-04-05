package uk.co.reassured.reassuredmobileapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

/**
 * Created by hfletcher on 05/04/2018.
 */

public class ITServiceDesk extends AppCompatActivity {

    //This is where the application API is located
    private String AppHost = "http://rmobileapp.co.uk/";

    public void onCreate(Bundle savedInstanceState){
        //Load the application bundle
        super.onCreate(savedInstanceState);

        //Load the correct content view.
        setContentView(R.layout.activity_it_service_desk);

        JSONObject PostData = new JSONObject();
        //Send the message
        PerformPostRequest(new OnJSONResponseCallback(){
            @Override
            public JSONObject onJSONResponse(boolean success, JSONObject response) {
                System.out.println(response);
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

        client.post(AppHost + "ITServiceDesk.php", RequestParameters, new AsyncHttpResponseHandler() {
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
