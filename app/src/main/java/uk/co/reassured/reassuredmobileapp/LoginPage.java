package uk.co.reassured.reassuredmobileapp;

import java.math.BigInteger;
import java.security.MessageDigest;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.loopj.android.http.*;

import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

public class LoginPage extends AppCompatActivity {

    //Where is the app API hosted?
    private String AppHost = "http://82.10.188.99/api/";

    @Override
    public void onCreate(Bundle savedInstanceState){
        //Load the login activity layout
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_page);

        //This is the login button
        final Button loginButton = findViewById(R.id.sign_in);

        //Get the application ID
        Intent FireBaseID = new Intent(this, MyFirebaseInstanceIdService.class);
        startService(FireBaseID);

        //If the user is currently signed in, run the method, don't wait for button click.
        if(!getEmail(LoginPage.this).matches("")) {
            loginMethod();
        } else {
            loginButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    //Skip Logins
                    //Intent homepage = new Intent(LoginPage.this, HomePage.class);
                    //startActivity(homepage);

                    //Close the on screen keyboard.
                    View view = getCurrentFocus();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);

                    //Sign in
                    loginMethod();
                }
            });
        }
    }

    public void loginMethod(){
        //These are the email and password fields
        final EditText email = findViewById(R.id.reassuredEmail);
        final EditText password = findViewById(R.id.password);

        //Hash the password
        final String passwordHash = getSHA512(password.getText().toString());

        try {
            AsyncHttpClient client = new AsyncHttpClient();

            //The URL to go to
            String url = AppHost;

            //Are we using stored details or user entered ones?
            if(!getEmail(LoginPage.this).matches("")){
                url = url+ "users.php?login=true&email=" + getEmail(LoginPage.this) + "&password=" + getPassword(LoginPage.this) + "&token=" + getFirebase(LoginPage.this);
            } else {
                url = url+ "users.php?login=true&email=" + email.getText().toString()+"&password="+passwordHash + "&token=" + getFirebase(LoginPage.this);
            }

            client.get(url, new AsyncHttpResponseHandler() {

                @Override
                public void onStart() {
                    // called before request is started
                }

                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] response) {
                    // called when response HTTP status is "200 OK"
                    String ReassuredAppUserDetails = new String(response);
                    try{
                        //Store the result as a JSON array
                        JSONObject SESSION = new JSONObject(ReassuredAppUserDetails);

                        //If the session is a valid user, move on to the ID Card activity, else present error message
                        if(SESSION.getInt("status") == 200){
                            //Set up the homepage activity
                            Intent homepage = new Intent(LoginPage.this, HomePage.class);

                            //Display a welcome message
                            Toast.makeText(LoginPage.this, "Welcome back, " + SESSION.get("firstname"), Toast.LENGTH_LONG).show();

                            //Save the user details
                            if(!email.getText().toString().matches("")){
                                saveUserDetails(LoginPage.this, SESSION.getInt("id"), email.getText().toString(), passwordHash, SESSION.getString("firstname"), SESSION.getString("lastname"), SESSION.getInt("team_id"), SESSION.getInt("location_id"));
                            }

                            //Start the new activity
                            startActivity(homepage);

                            //Close this activity
                            finish();

                        } else {
                            destroyUserDetails(LoginPage.this);
                            Toast.makeText(LoginPage.this, "Username or password incorrect.", Toast.LENGTH_LONG).show();
                        }
                    } catch (Exception E){
                        System.out.println("Error");
                    }

                }

                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] errorResponse, Throwable e) {
                    // called when response HTTP status is "4XX" (eg. 401, 403, 404)
                    new AlertDialog.Builder(LoginPage.this)
                            .setMessage("Something went wrong. Please try again. Error: " + statusCode)
                            .setNegativeButton("OK", null)
                            .create()
                            .show();

                    destroyUserDetails(LoginPage.this);
                }

                @Override
                public void onRetry(int retryNo) {
                    // called when request is retried
                }
            });
        } catch (Exception E){
            new AlertDialog.Builder(LoginPage.this)
                    .setMessage("Something went wrong: " + E)
                    .setNegativeButton("OK", null)
                    .create()
                    .show();
        }
    }

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

    static SharedPreferences getSharedPreferences(Context ctx){
        return PreferenceManager.getDefaultSharedPreferences(ctx);
    }

    public static void saveUserDetails(Context ctx, int id, String Email, String Password, String firstname, String lastname, int team_id, int location_id){
        SharedPreferences.Editor userDetails = getSharedPreferences(ctx).edit();
        String FireBaseToken = getSharedPreferences(ctx).getString("FirebaseToken","");

        userDetails.clear();

        userDetails.putInt("id", id);
        userDetails.putString("Email", Email);
        userDetails.putString("Password", Password);
        userDetails.putString("firstname", firstname);
        userDetails.putString("lastname", lastname);
        userDetails.putString("FirebaseToken", FireBaseToken);
        userDetails.putInt("team_id", team_id);
        userDetails.putInt("location_id", location_id);

        userDetails.commit();
    }

    public static void destroyUserDetails(Context ctx){
        SharedPreferences.Editor userDetails = getSharedPreferences(ctx).edit();
        userDetails.remove("Email");
        userDetails.remove("Password");
        userDetails.remove("firstname");
        userDetails.remove("lastname");
        userDetails.remove("team_id");
        userDetails.remove("location_id");

        userDetails.commit();
    }

    public static String getEmail(Context ctx)
    {
        return getSharedPreferences(ctx).getString("Email", "");
    }

    public static String getPassword(Context ctx)
    {
        return getSharedPreferences(ctx).getString("Password", "");
    }

    public static String getFirebase(Context ctx)
    {
        return getSharedPreferences(ctx).getString("FirebaseToken", "");
    }
}


