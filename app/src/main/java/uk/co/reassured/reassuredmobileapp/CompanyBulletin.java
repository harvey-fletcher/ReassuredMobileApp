package uk.co.reassured.reassuredmobileapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import java.net.URLEncoder;
import java.util.Timer;
import java.util.TimerTask;

import cz.msebera.android.httpclient.Header;

/**
 * Created by hfletcher on 09/02/2018.
 */

public class CompanyBulletin extends AppCompatActivity {

    //This is where the API is
    public String AppHost = "http://82.10.188.99/api/";

    //This is used for scheduling tasks.
    public Timer timer = new Timer();

    //This is for what post comments we are viewing
    public int PostCommentsId = 0;

    //This is deciding what view we are refreshing
    public int ViewMode = 1;

    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_company_bulletin);

        //This is the go back button
        TextView GoBackLink = (TextView)findViewById(R.id.GoBackLink);
        GoBackLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        //This is the new post form
        final EditText NewPostTextBox = (EditText)findViewById(R.id.NewPostTextBox);
        Button NewPostSubmitButton = (Button) findViewById(R.id.NewPostSubmitButton);

        //When we click the NewPostSubmitButton check that the textbox is between the two lengths, if it is, dont post.
        NewPostSubmitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String NewPostBody = NewPostTextBox.getText().toString();

                if(NewPostBody.length() < 10 || NewPostBody.length() > 499){
                    Toast.makeText(CompanyBulletin.this, "Post must be between 10 and 500 characters.", Toast.LENGTH_LONG).show();
                } else {
                    //Close the on screen keyboard.
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);

                    //Clear the textbox
                    NewPostTextBox.setText("");

                    //Send the post
                    sendNewPost(CompanyBulletin.this, NewPostBody);
                }
            }
        });

        timer.schedule(new timedTask(), 0, 2500);
    }

    public static SharedPreferences sharedPrefs(Context ctx){
        return PreferenceManager.getDefaultSharedPreferences(ctx);
    }

    public void sendNewPost(final Context ctx, String PostBody){
        try{
            //Email and password so we can authenticate against our API
            String email = sharedPrefs(ctx).getString("Email","");
            String password = sharedPrefs(ctx).getString("Password","");

            //Where we are going to send the get request
            String url = AppHost + "MyReassured.php?email=" + email + "&password=" + password + "&action=post&post_body=" + URLEncoder.encode(PostBody.replace("&","<ampersand>"));

            //The client to perform the get request
            AsyncHttpClient client = new AsyncHttpClient();

            //Perform the request
            client.get(url, new AsyncHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                    Toast.makeText(ctx, "Success!", Toast.LENGTH_LONG).show();
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                    Toast.makeText(ctx, "Unexpected error: " + statusCode, Toast.LENGTH_LONG).show();
                }
            });
        } catch (Exception e){
            Toast.makeText(ctx, "Unexpected " + e.getClass().getSimpleName(), Toast.LENGTH_LONG).show();
        }
    }

    public void getUncleanPosts(final Context ctx){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                String PostsString = sharedPrefs(ctx).getString("MyReassuredPosts","");

                TextView PostsTextview = new TextView(ctx);
                PostsTextview.setText(PostsString);

                RelativeLayout ResultsContainer = (RelativeLayout)findViewById(R.id.ResultsContainer);
                ResultsContainer.removeAllViews();
                ResultsContainer.addView(PostsTextview);
            }
        });
    }

    public class timedTask extends TimerTask{
        @Override
        public void run() {
            if(ViewMode == 1){
                getUncleanPosts(CompanyBulletin.this);
            }
        }
    }
}
