package uk.co.reassured.reassuredmobileapp;

import android.app.Application;
import android.content.Context;

/**
 * Created by hfletcher on 04/05/2018.
 */

public class ReassuredMobileApp extends Application {

    private static Context context;

    public void onCreate() {
        super.onCreate();
        ReassuredMobileApp.context = getApplicationContext();
    }

    public static Context getAppContext() {
        return ReassuredMobileApp.context;
    }
}
