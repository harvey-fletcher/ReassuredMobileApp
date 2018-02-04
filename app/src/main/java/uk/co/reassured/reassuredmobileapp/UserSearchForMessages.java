package uk.co.reassured.reassuredmobileapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.view.ScrollingView;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.json.JSONArray;

import cz.msebera.android.httpclient.Header;

/**
 * Created by Harvey on 04/02/2018.
 */

public class UserSearchForMessages extends AppCompatActivity {
    //The search field
    public EditText SearchBox;

    //The scroll view
    public ScrollView ResultsScroller;

    //This is where the API is
    public String AppHost = "http://82.10.188.99/api/";

    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_search_message);

        //This is the link to close the search
        TextView goBackLink = findViewById(R.id.GoBackLink);
        goBackLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        //Now that we have loaded the layout, associate the objects
        SearchBox = findViewById(R.id.UserSearchBox);
        ResultsScroller = findViewById(R.id.resultsScroller);

        //Add a text listener to the search box to find when it changes
        SearchBox.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if(SearchBox.getText().toString().length() >= 2) {
                    getSearchResults(SearchBox.getText().toString());
                } else {
                    ResultsScroller.removeAllViews();
                }
            }
        });

    }

    public static SharedPreferences SharedPrefs(Context ctx){
        return PreferenceManager.getDefaultSharedPreferences(ctx);
    }

    public void getSearchResults(String SearchTerm){
        try{
            AsyncHttpClient client = new AsyncHttpClient();
            Context ctx = UserSearchForMessages.this;
            String Email = SharedPrefs(ctx).getString("Email","");
            String Password = SharedPrefs(ctx).getString("Password","");

            String url = AppHost + "social.php?list_users=true&email=" + Email + "&password=" + Password + "&search=" + SearchTerm;

            client.get(url, new AsyncHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                    String resultsAsString = new String(responseBody);
                    try {
                        JSONArray resultsAsArray = new JSONArray(resultsAsString);
                        System.out.println("Results: \n\n" + resultsAsArray + "\n\n");

                        displayResults(resultsAsArray);
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

    public void displayResults(JSONArray Results){
        int ResultsCount = Results.length();

        Context ctx = UserSearchForMessages.this;

        RelativeLayout ResultsContainer = new RelativeLayout(ctx);

        ResultsScroller.removeAllViews();

        for(int i=0; i < ResultsCount; i++){
            String FullName = "";
            String OfficeLocation = "";
            int user_id = 0;

            try{
                FullName = Results.getJSONObject(i).getString("firstname") + " " + Results.getJSONObject(i).getString("lastname");
                OfficeLocation = Results.getJSONObject(i).getString("location_name") + "\n";
                user_id = Results.getJSONObject(i).getInt("id");
            } catch (Exception e){
                e.printStackTrace();
            }

            RelativeLayout ResultRecord = new RelativeLayout(ctx);
            TextView Result = new TextView(ctx);

            String Details = FullName + "\n" + OfficeLocation;

            System.out.println("User Detail: \n" + Details);
            Result.setText(Details);

            ResultRecord.addView(Result);
            ResultRecord.setId(i + 1);

            if(i > 0){
                RelativeLayout.LayoutParams ResultContainerLayout = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                ResultContainerLayout.addRule(RelativeLayout.BELOW, i);
                ResultRecord.setLayoutParams(ResultContainerLayout);
            }

            ResultsContainer.setOnClickListener(getOnClickDoSomething(user_id, FullName));

            ResultsContainer.addView(ResultRecord);
        }

        ResultsScroller.addView(ResultsContainer);
    }

    View.OnClickListener getOnClickDoSomething(final int user_id, final String user_name){
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                RelativeLayout MB = findViewById(R.id.mainBody);
                MB.removeAllViews();

                Display display = getWindowManager().getDefaultDisplay();
                int width = display.getWidth();
                int height = display.getHeight();

                TextView Title = new TextView(UserSearchForMessages.this);
                Title.setText("New chat with " + user_name);
                Title.setWidth(width);
                Title.setGravity(Gravity.CENTER);
                Title.setTextSize(20);

                EditText MessageText = new EditText(UserSearchForMessages.this);
                MessageText.setWidth(width);
                MessageText.setHeight(height / 2);
                MessageText.setGravity(Gravity.TOP);
                MessageText.setY(50);
                MessageText.setHint("Message...");

                Button SendButton = new Button(UserSearchForMessages.this);
                SendButton.setWidth(width);
                SendButton.setHeight(25);
                SendButton.setText("Send");
                SendButton.setY((height / 2) + 75);

                MB.setGravity(Gravity.CENTER_HORIZONTAL);
                MB.addView(Title);
                MB.addView(MessageText);
                MB.addView(SendButton);

            }
        };
    };
}
