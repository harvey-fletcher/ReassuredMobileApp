package uk.co.reassured.reassuredmobileapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

/**
 * Created by Harvey on 28/01/2018.
 */

public class MyFirebaseInstanceIdService extends FirebaseInstanceIdService {

    private String TOKEN;

    public void onTokenRefresh(){
        String token = FirebaseInstanceId.getInstance().getToken();

        saveUserToken(MyFirebaseInstanceIdService.this, token);
    }

    static SharedPreferences getSharedPreferences(Context ctx){
        return PreferenceManager.getDefaultSharedPreferences(ctx);
    }

    public static void saveUserToken(Context ctx, String token){
        SharedPreferences.Editor userDetails = getSharedPreferences(ctx).edit();
        userDetails.putString("FirebaseToken", token);

        userDetails.commit();
    }

}
