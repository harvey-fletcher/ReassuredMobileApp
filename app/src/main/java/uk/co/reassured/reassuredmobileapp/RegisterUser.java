package uk.co.reassured.reassuredmobileapp;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

/**
 * Created by hfletcher on 30/01/2018.
 */

public class RegisterUser extends AppCompatActivity {

    @Override
    public void onCreate(Bundle savedInstanceState){
        //Display the user registration
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_user);

        //The "Go Back" link
        final TextView goBackLink = (TextView)findViewById(R.id.GoBackLink);

        goBackLink.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                finish();
            }
        });
    }
}
