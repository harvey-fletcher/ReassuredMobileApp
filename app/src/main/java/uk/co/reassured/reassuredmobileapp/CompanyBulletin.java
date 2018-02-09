package uk.co.reassured.reassuredmobileapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.ShapeDrawable;
import android.media.Image;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.text.SpannableString;
import android.text.style.RelativeSizeSpan;
import android.view.Display;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URLDecoder;
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

    //This is the screen size
    public Display display;
    public int ScreenWidth = 0;
    public int ScreenHeight = 0;

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

        //Set the screen size
        display = getWindowManager().getDefaultDisplay();
        ScreenWidth = display.getWidth();
        ScreenHeight = display.getHeight();

        //This is the close button for the comments box, we're going to set it up here
        setUpCommentsClose(CompanyBulletin.this);

        timer.schedule(new timedTask(), 0, 2500);
    }

    public static SharedPreferences sharedPrefs(Context ctx){
        return PreferenceManager.getDefaultSharedPreferences(ctx);
    }

    public void setUpCommentsClose(Context ctx){
        ImageView CloseComments = (ImageView)findViewById(R.id.CommentsCloseButton);
        final RelativeLayout CommentsContainer = (RelativeLayout)findViewById(R.id.CommentsContainer);
        CloseComments.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Close the comments popup
                CommentsContainer.setVisibility(View.INVISIBLE);

                //Set the posts scroller to true so it can move about
                ScrollView PostsScroller = findViewById(R.id.ResultsScrollView);
                PostsScroller.setFocusable(true);

                //Set the viewmode to 1 so the posts start refreshing again
                ViewMode = 1;
            }
        });
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

    public void PrettyPrintPosts(final Context ctx){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //This is the scrollview that all the posts go in
                ScrollView PostsScrollingView = (ScrollView)findViewById(R.id.ResultsScrollView);
                PostsScrollingView.removeAllViews();

                //Get the posts from storage and turn them into an array so we can use them
                JSONArray Posts = new JSONArray();
                try{
                    //Get the posts from storage on the device
                    String PostsString = sharedPrefs(ctx).getString("MyReassuredPosts","");

                    //Turn them into an array
                    Posts = new JSONArray(PostsString);
                } catch (Exception e){
                    e.printStackTrace();
                }

                //This is the relative layout that is inside the scrolling view.
                RelativeLayout PostsInnerContainer = new RelativeLayout(ctx);

                //Give that container a full width
                RelativeLayout.LayoutParams PostsInnerContainerParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                PostsInnerContainerParams.width = ScreenWidth;

                //Apply the parameters
                PostsInnerContainer.setLayoutParams(PostsInnerContainerParams);

                //This is the colourscheme. 0 is orange, 1 is purple
                int ColourScheme = 0;

                //Add each post to the relative layout inside the scrolling view in a nicely laid out format
                for(int i=0;i<Posts.length();i++){
                    //Each post goes inside its own relativelayout so it can have text and images, as well as positioning
                    RelativeLayout IndividualContainer = new RelativeLayout(ctx);

                    //This is the individual post
                    JSONObject Post = new JSONObject();
                    int postID = 0;
                    String Post_Author = "";
                    String Post_Body = "";
                    String Post_Created = "";
                    String Author_Team = "";
                    String Author_Location = "";
                    try{
                        Post = Posts.getJSONObject(i);
                        postID = Integer.parseInt(Post.getString("postID"));
                        Post_Author = Post.getString("firstname") + " " + Post.getString("lastname");
                        Post_Body = URLDecoder.decode(Post.getString("post_body")).replace("<ampersand", "&");
                        Post_Created = Post.getString("created");
                        Author_Team = Post.getString("team_name");
                        Author_Location = Post.getString("location_name");
                    } catch (Exception e){
                        e.printStackTrace();
                    }

                    //Build a string out of the post data. Needs to be spannable so we can use different font sizes.
                    SpannableString PostData = new SpannableString(Post_Author + "\n" + Post_Body + "\n\n" + Post_Created + "\n" + Author_Team + " | " + Author_Location);

                    //Span different parts of the string to have different sizes
                    int StartAtPosition = 0;
                    PostData.setSpan(new RelativeSizeSpan(2f), StartAtPosition, Post_Author.length(), 0);
                    StartAtPosition+= Post_Author.length() + 1;
                    PostData.setSpan(new RelativeSizeSpan(1.5f), StartAtPosition, StartAtPosition + Post_Body.length(), 0);
                    StartAtPosition+= Post_Body.length() + 2;
                    PostData.setSpan(new RelativeSizeSpan(0.8f), StartAtPosition, PostData.length(), 0);

                    //Create a textview for the post data and fill it
                    TextView PostDataText = new TextView(ctx);
                    PostDataText.setText(PostData);

                    //Add the PostDataText to the container so it can be seen and positioned
                    IndividualContainer.addView(PostDataText);

                    //Measure the height of the text so we can set the height of the box it is contained in
                    PostDataText.measure(0,0);
                    PostDataText.getMeasuredHeight();
                    PostDataText.setX(10);

                    //Give the individual post an ID so we can put other things below it
                    IndividualContainer.setId(i + 1);

                    //Add the parameters for the display
                    RelativeLayout.LayoutParams PostContainerParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                    PostContainerParams.height = PostDataText.getMeasuredHeight() + 10;
                    PostContainerParams.width = ScreenWidth;

                    //Everything has a left margin, but if this is not the first post, place it below the previous one.
                    if(i>0){
                        PostContainerParams.addRule(RelativeLayout.BELOW, i);
                        PostContainerParams.setMargins(10,20,10,0);
                    } else {
                        PostContainerParams.setMargins(10,0,10,0);
                    }

                    //Give the individual post a border
                    ShapeDrawable rectShapeDrawable = new ShapeDrawable(); // pre defined class

                    // get paint
                    Paint paint = rectShapeDrawable.getPaint();

                    // set border color, stroke and stroke width
                    if(ColourScheme == 0){
                        //Reassured Orange
                        paint.setColor(Color.parseColor("#FE8A00"));

                        //Change so the next post has the opposite colour
                        ColourScheme = 1;
                    } else {
                        //Reassured Purple
                        paint.setColor(Color.parseColor("#1870A0"));

                        //Change so the next post has the opposite colour
                        ColourScheme = 0;
                    }
                    paint.setStyle(Paint.Style.STROKE);
                    paint.setStrokeWidth(4);
                    IndividualContainer.setBackgroundDrawable(rectShapeDrawable);

                    //Add a comment button to the post
                    ImageView CommentButton = new ImageView(ctx);
                    CommentButton.setBackgroundResource(R.drawable.bulletin_comment_button);

                    //Give it a width and height
                    LinearLayout.LayoutParams ImageWidthAndHeight = new LinearLayout.LayoutParams(75,75);
                    CommentButton.setLayoutParams(ImageWidthAndHeight);

                    //Put the comment button on the bottom right.
                    CommentButton.setX(PostContainerParams.width - 125);
                    CommentButton.setY(PostContainerParams.height - 95);

                    //Display the comment button on the post
                    IndividualContainer.addView(CommentButton);

                    //Set up the comment button so it does something
                    IndividualContainer.setOnClickListener(CommentsButtonOnClickOpen(ctx, postID));

                    //Apply the parameters
                    IndividualContainer.setLayoutParams(PostContainerParams);

                    //Add the individual post to the results inner container
                    PostsInnerContainer.addView(IndividualContainer);
                }

                //Add the inner container to the scroller
                PostsScrollingView.addView(PostsInnerContainer);
            }
        });
    }

    View.OnClickListener CommentsButtonOnClickOpen(final Context ctx, final int postID){
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Display the comments container
                RelativeLayout CommentContainer = findViewById(R.id.CommentsContainer);
                CommentContainer.setVisibility(View.VISIBLE);
                CommentContainer.setBackgroundColor(Color.parseColor("#FFFFFF"));

                //Set the posts scroller to false so it doesn't move about
                ScrollView PostsScroller = findViewById(R.id.ResultsScrollView);
                PostsScroller.setFocusable(false);

                //Set the global post ID so the program knows which comments to display
                PostCommentsId = postID;

                //Set viewmode to 2 so that the comments start refreshing
                ViewMode = 2;

                System.out.println("The post ID is =======> " + postID);
            }
        };
    }

    public void PrettyPrintComments(final Context ctx){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //Retrieve the posts from storage
                JSONArray PostsArray = new JSONArray();
                try{
                    PostsArray = new JSONArray(sharedPrefs(ctx).getString("MyReassuredPosts",""));
                } catch (Exception e){
                    e.printStackTrace();
                }

                //This is where all the comments go
                JSONArray Comments = new JSONArray();

                //Get all the posts so we can do stuff with them
                for(int i=0;i<PostsArray.length();i++){
                    //Get each individual posts
                    JSONObject Post = new JSONObject();
                    try{
                        Post = PostsArray.getJSONObject(i);

                        if(Integer.parseInt(Post.getString("postID")) == PostCommentsId){
                            Comments = new JSONArray(Post.getString("comments"));
                        }
                    } catch (Exception e){
                        e.printStackTrace();
                    }
                }

                //Put all those comments into the comments scrolling view
                ScrollView CommentsScrollingView = (ScrollView)findViewById(R.id.CommentsScrollView);
                CommentsScrollingView.removeAllViews();
                TextView CommentsText = new TextView(ctx);
                CommentsText.setText(Comments.toString());
                CommentsScrollingView.addView(CommentsText);

                System.out.println("Refreshed view for the comments on post ID: " + PostCommentsId);
            }
        });
    }

    public class timedTask extends TimerTask{
        @Override
        public void run() {
            if(ViewMode == 1){
                PrettyPrintPosts(CompanyBulletin.this);
            } else if (ViewMode == 2){
                PrettyPrintComments(CompanyBulletin.this);
            }
        }
    }
}
