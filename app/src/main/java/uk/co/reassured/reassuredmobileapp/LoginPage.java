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

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_page);

        //This is the login button
        final Button loginButton = findViewById(R.id.sign_in);

        //If the user is currently signed in, run the method, don't wait for button click.
        if(!getEmail(LoginPage.this).matches("")) {
            loginMethod();
        } else {
            loginButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
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

            //The domain the user API is at
            String url = "http://e-guestlist.co.uk/api/app_login?";

            //Are we using stored details or user entered ones?
            if(!getEmail(LoginPage.this).matches("")){
                url = url+ "email=" + getEmail(LoginPage.this) + "&password=" + getPassword(LoginPage.this);
            } else {
                url = url+ "email=" + email.getText().toString()+"&password="+passwordHash;
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

                            //Pass the user details to the next activity
                            Bundle userDetails = new Bundle();
                            userDetails.putString("ReassuredAppUserDetails", ReassuredAppUserDetails);
                            homepage.putExtras(userDetails);

                            //Save the user details
                            if(!email.getText().toString().matches("")){
                                saveUserDetails(LoginPage.this, email.getText().toString(), passwordHash);
                            }

                            //Start the new activity
                            startActivity(homepage);

                            //Close this activity
                            finish();

                        } else {
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

    public static void saveUserDetails(Context ctx, String Email, String Password){
        SharedPreferences.Editor editor = getSharedPreferences(ctx).edit();
        editor.putString("Email", Email);
        editor.putString("Password", Password);
        editor.commit();
    }

    public static void destroyUserDetails(Context ctx){
        SharedPreferences.Editor editor = getSharedPreferences(ctx).edit();
        editor.remove("Email");
        editor.remove("Password");
        editor.commit();
    }

    public static String getEmail(Context ctx)
    {
        return getSharedPreferences(ctx).getString("Email", "");
    }

    public static String getPassword(Context ctx)
    {
        return getSharedPreferences(ctx).getString("Password", "");
    }
}


