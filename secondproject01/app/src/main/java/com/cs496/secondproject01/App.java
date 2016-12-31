package com.cs496.secondproject01;

import android.app.Application;
import android.os.StrictMode;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginManager;

import org.json.JSONObject;

/**
 * Created by q on 2016-12-30.
 */

public class App extends Application {
    public static boolean firstAccess;
    public static JSONObject userFBinfo;


    @Override
    public void onCreate() {
        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(this);
        LoginManager.getInstance().logOut();
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                .permitDiskReads()
                .permitDiskWrites()
                .permitNetwork().build());
        super.onCreate();
        firstAccess = true;
    }

    public boolean isLoggedIn() {
        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        return accessToken != null;
    }

    public void showToast(String message){
        Toast.makeText(this,message,Toast.LENGTH_LONG).show();
    }

}
