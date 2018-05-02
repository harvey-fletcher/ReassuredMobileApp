package uk.co.reassured.reassuredmobileapp;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONObject;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.Header;

/**
 * Created by hfletcher on 30/01/2018.
 */

public class RegisterUser extends AppCompatActivity {

    //The spinner objects
    Spinner teamsSpinner;
    Spinner locationsSpinner;

    //ClassGlobals variables
    ClassGlobals classGlobals = new ClassGlobals();

    //The user entry text fields
    EditText email;
    EditText password1;
    EditText password2;
    EditText firstname;
    EditText lastname;

    //Used for the drop downs
    public List<String> team_names = new ArrayList<>();
    public List<Integer> team_id = new ArrayList<>();
    public List<String> location_names = new ArrayList<>();
    public List<Integer> location_id = new ArrayList<>();

    @Override
    public void onCreate(Bundle savedInstanceState){
        //Display the user registration
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_user);

        //These are the spinners
        teamsSpinner = (Spinner)findViewById(R.id.teams);
        locationsSpinner = (Spinner)findViewById(R.id.locations);

        //Build a list of teams
        getTeams();

        //Build a list of locations
        getLocations();

        //The "Go Back" link
        final TextView goBackLink = (TextView)findViewById(R.id.GoBackLink);

        //The user entry text fields
        email = (EditText)findViewById(R.id.reassuredEmail);
        password1 = (EditText)findViewById(R.id.password1);
        password2 = (EditText)findViewById(R.id.password2);
        firstname = (EditText)findViewById(R.id.firstname);
        lastname = (EditText)findViewById(R.id.lastname);

        //This is the register button
        Button registerButton = (Button)findViewById(R.id.registerButton);

        //What happens when the go back link is clicked.
        goBackLink.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                finish();
            }
        });

        //What happens when the register button is clicked
        registerButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                validateUsers();
            }
        });
    }

    public void validateUsers(){
        //The user input values
        int SelectedLocation = location_id.get(locationsSpinner.getSelectedItemPosition());
        int SelectedTeam = team_id.get(teamsSpinner.getSelectedItemPosition());
        String email_address = email.getText().toString();
        String password_1 = password1.getText().toString();
        String password_2 = password2.getText().toString();
        String first_name = firstname.getText().toString();
        String last_name = lastname.getText().toString();

        //If the validation fails, this gets populated
        String reason = "";

        //Is that a real reassured email address?
        if(email_address.length() >= 16 && !email_address.substring(0,1).matches("@")){
            if(!email_address.substring(email_address.length() - 16, email_address.length()).matches("@reassured.co.uk")){
                reason = "You must use your reassured email address.";
            };
        } else if (email_address.length() < 16){
            reason = "You must use your reassured email address.";
        } else if(email_address.substring(0,1).matches("@")){
            reason = "You must use your reassured email address.";
        }

        //Are all the other fields valid?
        if(reason.length() > 0){
            Toast.makeText(RegisterUser.this, reason, Toast.LENGTH_LONG).show();
        } else {
            password_1 = password_1.replace("*",".*");
            password_2 = password_2.replace("*",".*");

            if (!password_1.matches(password_2)) {
                reason = "Password 1 and 2 must match";
            } else if(password_1.matches("")){
                reason = "Password cannot be blank";
            } else if (first_name.matches("")) {
                reason = "First name cannot be null";
            } else if (last_name.matches("")) {
                reason = "Last name cannot be null";
            } else if (SelectedTeam == 0) {
                reason = "Please select a team";
            } else if (SelectedLocation == 0) {
                reason = "Please select a location";
            } else {
                //Hash up the password
                password_1 = password_1.replace(".*","*");
                String password_hash = getSHA512(password_1);

                //Build the URL
                String url = "email=" + email_address + "&password=" + password_hash + "&firstname=" + first_name + "&lastname=" + last_name + "&team_id=" + SelectedTeam + "&location_id=" + SelectedLocation;

                //Do the request to add the user.
                createUser(url);
            }

            if(reason.length() > 0){
                //Display the reason the user account couldn't be created.
                Toast.makeText(RegisterUser.this, reason, Toast.LENGTH_LONG).show();
            }
        }
    }

    public void createUser(String url){
        try{
            AsyncHttpClient client = new AsyncHttpClient();

            client.get(classGlobals.AppHost + "users.php?create=true&" + url, new AsyncHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                    try{
                        //Split up the response.
                        String responseObj = new String(responseBody);
                        JSONObject response = new JSONObject(responseObj);
                        int status = response.getInt("status");
                        String reason = response.getString("reason");

                        if(status == 200){
                            //Display the success message and close the activity
                            Toast.makeText(RegisterUser.this, reason, Toast.LENGTH_LONG).show();
                            finish();
                        } else {
                            //Display the reason the user account couldn't be created.
                            Toast.makeText(RegisterUser.this, "The user account couldn't be created. " + reason, Toast.LENGTH_LONG).show();
                        }
                    } catch (Exception e){
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {

                }
            });
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public void getTeams(){
        try{
            AsyncHttpClient client = new AsyncHttpClient();

            client.get(classGlobals.AppHost + "social.php?teams=true", new AsyncHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                    String response = new String(responseBody);
                    try{
                        JSONArray responseJSON = new JSONArray(response);
                        int status = responseJSON.getJSONObject(0).getInt("status");
                        if(status == 200){
                            int position = 1;
                            int array_items = responseJSON.length();

                            team_names.add("Team");
                            team_id.add(0);

                            //Add all the teams and their id to the list
                            do {
                                team_names.add(responseJSON.getJSONObject(position).getString("team_name"));
                                team_id.add(responseJSON.getJSONObject(position).getInt("id"));
                                position++;
                            } while (position < array_items);

                            //Build the array adapter
                            ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                                    RegisterUser.this,
                                    R.layout.support_simple_spinner_dropdown_item,
                                    team_names
                            );

                            //Populate the drop down with the teams
                            teamsSpinner.setAdapter(adapter);

                        } else {
                            //Display a welcome message
                            Toast.makeText(RegisterUser.this, "Error " + status + " please try again.", Toast.LENGTH_LONG).show();
                        }
                    } catch (Exception e){
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {

                }
            });
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public void getLocations(){
        try{
            AsyncHttpClient client = new AsyncHttpClient();

            client.get(classGlobals.AppHost + "social.php?locations=true", new AsyncHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                    String response = new String(responseBody);
                    try{
                        JSONArray responseJSON = new JSONArray(response);
                        int status = responseJSON.getJSONObject(0).getInt("status");
                        if(status == 200){
                            int position = 1;
                            int array_items = responseJSON.length();

                            location_names.add("Location");
                            location_id.add(0);

                            //Add all the teams and their id to the list
                            do {
                                location_names.add(responseJSON.getJSONObject(position).getString("location_name"));
                                location_id.add(responseJSON.getJSONObject(position).getInt("id"));
                                position++;
                            } while (position < array_items);

                            //Build the array adapter
                            ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                                    RegisterUser.this,
                                    R.layout.support_simple_spinner_dropdown_item,
                                    location_names
                            );

                            //Populate the drop down with the teams
                            locationsSpinner.setAdapter(adapter);

                        } else {
                            //Display a welcome message
                            Toast.makeText(RegisterUser.this, "Error " + status + " please try again.", Toast.LENGTH_LONG).show();
                        }
                    } catch (Exception e){
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {

                }
            });
        } catch (Exception e){
            e.printStackTrace();
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

}
