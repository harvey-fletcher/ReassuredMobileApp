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

import org.json.JSONObject;

import java.math.BigInteger;
import java.security.MessageDigest;

import cz.msebera.android.httpclient.Header;

/**
 * Created by hfletcher on 17/04/2018.
 */

public class UserSettingsTopLevel extends AppCompatActivity {

    public final String AppHost = "http://rmobileapp.co.uk/";

    public void onCreate(Bundle savedInstanceState){
        //Load the correct activity
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_settings_top);

        //This is the "Go Back" link
        final RelativeLayout go_back = findViewById(R.id.GoBackLink);

        //When the "Go Back" link is clicked, close this activity (Will display the main screen)
        go_back.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                finish();
            }
        });

        //When the user clicks the update password button
        Button UpdatePassword = findViewById(R.id.UpdatePassword);
        UpdatePassword.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                ChangePassword();
            }
        });
    }

    public void ChangePassword(){
        //These are the password fields.
        final EditText CurrentPasswordField = findViewById(R.id.CurrentPassword);
        final EditText NewPasswordField = findViewById(R.id.NewPassword);
        final EditText NewPasswordRepeatField = findViewById(R.id.NewPasswordRepeat);

        //These are the passwords, in hash format
        String CurrentPassword = getSHA512(CurrentPasswordField.getText().toString());
        String NewPassword = getSHA512(NewPasswordField.getText().toString());
        final String NewPasswordRepeat = getSHA512(NewPasswordRepeatField.getText().toString());

        //This is the users current password
        String UserCurrentPassword = getSharedPreferences(UserSettingsTopLevel.this).getString("Password","");

        //The password must be more than 4 characters
        if(NewPasswordField.getText().toString().length() <= 4){
            Toast.makeText(UserSettingsTopLevel.this, "The new password must be more than 4 characters", Toast.LENGTH_LONG).show();
            return;
        }

        //The new password can't contain whitespace
        if(NewPasswordField.getText().toString().contains(" ")){
            Toast.makeText(UserSettingsTopLevel.this, "The new password cannot contain whitespace", Toast.LENGTH_LONG).show();
            return;
        }

        //The new password can't be blank
        if(NewPasswordField.getText().toString().matches("")){
            Toast.makeText(UserSettingsTopLevel.this, "The new password cannot be null", Toast.LENGTH_LONG).show();
            return;
        }

        //The new password can't be "password"
        if(NewPasswordField.getText().toString().matches("password")){
            Toast.makeText(UserSettingsTopLevel.this, "The new password cannot be \"password\"", Toast.LENGTH_LONG).show();
            return;
        }

        //In order to update passwords, the current password entered needs to match what's already set
        if(!CurrentPassword.matches(UserCurrentPassword)){
            Toast.makeText(UserSettingsTopLevel.this, "Please enter your current password.", Toast.LENGTH_LONG).show();
            return;
        }

        //The new password and new password(repeated) need to match
        if(!NewPassword.matches(NewPasswordRepeat)){
            Toast.makeText(UserSettingsTopLevel.this, "The new passwords do not match", Toast.LENGTH_LONG).show();
            return;
        }

        //Since everything is OK, prepare postdata
        JSONObject PostData = new JSONObject();

        try{
            PostData.put("changePass","true");
            PostData.put("newPassword", NewPasswordRepeat);
        } catch (Exception e){
            Toast.makeText(UserSettingsTopLevel.this, "Something went wrong, please try again.", Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }

        //Send the new servicedesk request
        PerformPostRequest(new OnJSONResponseCallback(){
            @Override
            public JSONObject onJSONResponse(boolean success, JSONObject response) {
                try {
                    if (response.has("success")) {
                        Toast.makeText(UserSettingsTopLevel.this, response.getString("success"), Toast.LENGTH_LONG).show();

                        //Update the users stored password
                        SharedPreferences.Editor userDetails = getSharedPreferences(UserSettingsTopLevel.this).edit();
                        userDetails.putString("Password", NewPasswordRepeat);
                        userDetails.commit();

                        //Clear down all the password fields.
                        CurrentPasswordField.setText("");
                        NewPasswordField.setText("");
                        NewPasswordRepeatField.setText("");
                    } else {
                        Toast.makeText(UserSettingsTopLevel.this, response.getString("error"), Toast.LENGTH_LONG).show();
                    }
                } catch (Exception e){
                    Toast.makeText(UserSettingsTopLevel.this, "There was an unexpected error.\nPlease try again.", Toast.LENGTH_LONG).show();
                }

                return null;
            }
        }, PostData);
    }

    static SharedPreferences getSharedPreferences(Context ctx){
        return PreferenceManager.getDefaultSharedPreferences(ctx);
    }

    //Since this page can update password, we need to get a SHA512
    public static String getSHA512(String input){

        String toReturn = null;
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-512");
            digest.reset();
            digest.update(input.getBytes("utf8"));
            toReturn = String.format("%040x", new BigInteger(1, digest.digest()));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return toReturn;
    }

    //Because we are performing post requests to the new api, we need to use an interface
    public interface OnJSONResponseCallback{
        public JSONObject onJSONResponse(boolean success, JSONObject response);
    }

    //This function performs post requests to the server
    public void PerformPostRequest(final UserSettingsTopLevel.OnJSONResponseCallback callback, JSONObject PostData) {
        //To authenticate against the API we need the user's credentials
        String Email = getSharedPreferences(this).getString("Email","");
        String Password = getSharedPreferences(this).getString("Password","");

        //Add the credentials to post data
        try{
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

        client.post(AppHost + "users.php", RequestParameters, new AsyncHttpResponseHandler() {
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
                    Toast.makeText(UserSettingsTopLevel.this, "Error: " + statusCode, Toast.LENGTH_LONG).show();
                } catch (Exception e) {
                    Log.e("Exception", "JSONException on failure: " + e.toString());
                }
            }
        });
    }

}
