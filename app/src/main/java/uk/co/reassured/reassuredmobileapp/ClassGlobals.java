package uk.co.reassured.reassuredmobileapp;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by hfletcher on 02/05/2018.
 */

public class ClassGlobals {

    //This is where the API is being hosted.
    public String AppHost = "http://rmobileapp.co.uk/";

    //These are the coordinates for the office of Reassured Basingstoke
    public String[] OfficeCoordinates = new String[]{
            "51.2686289",
            "-1.0736336"
        };

    //Used for reading data from the local cache
    public SharedPreferences sharedPrefs(){
        return PreferenceManager.getDefaultSharedPreferences(ReassuredMobileApp.getAppContext());
    }
}
