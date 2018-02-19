package uk.co.reassured.reassuredmobileapp;

import android.*;
import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by hfletcher on 19/02/2018.
 */

public class LiftSharingView extends AppCompatActivity {

    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lift_sharing);

        //There needs to be a button so the user can go back
        TextView GoBack = (TextView)findViewById(R.id.GoBackLink);
        GoBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        //First, check if location is enabled
        CheckLocationPermissions();
    }

    public void CheckLocationPermissions(){
        boolean has_permission = (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED);

        //If we don't have the permission, request it.
        if(!has_permission){
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
            alertDialogBuilder.setTitle("Permission Required");
            alertDialogBuilder.setMessage("This feature requires access to your location in order to find nearby lift sharing.\n \nYour location will not be shared with other employees, but will be shared with our server and the google location service.\n\nYou will now be asked for permission.");
            alertDialogBuilder.setPositiveButton("Proceed", new DialogInterface.OnClickListener(){
                public void onClick(DialogInterface dialogInterface, int id){
                    ActivityCompat.requestPermissions(LiftSharingView.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 100);
                }
            });
            alertDialogBuilder.setNegativeButton("No Thanks", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    Toast.makeText(LiftSharingView.this, "You must enable location to use this feature.", Toast.LENGTH_LONG).show();
                    finish();
                }
            });
            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        CheckLocationPermissions();
    }
}
