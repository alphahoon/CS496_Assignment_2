package com.cs496.secondproject01;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import com.facebook.FacebookSdk;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import android.view.LayoutInflater;

import org.json.JSONException;
import org.json.JSONObject;

import static com.cs496.secondproject01.R.id.container;

public class LoginPop extends Activity {
    private CallbackManager cbmanager;
    private AccessToken mToken = null;
    //App APP = (App) getApplication();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //TextView logout = (TextView) findViewById(R.id.logout);
        super.onCreate(savedInstanceState);

        setContentView(R.layout.login_pop);
        //if (isLoggedIn())
          //  logout.setText("Are you sure you want to log out?");
        FacebookSdk.sdkInitialize(getApplicationContext());
        cbmanager = CallbackManager.Factory.create();
        mToken = AccessToken.getCurrentAccessToken();

        LoginButton loginButton = (LoginButton) findViewById(R.id.login_button);
        loginButton.setReadPermissions("public_profile", "user_friends", "email");
        loginButton.registerCallback(cbmanager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(final LoginResult loginResult) {

                GraphRequest request;
                mToken = loginResult.getAccessToken();
                request = GraphRequest.newMeRequest(mToken, jsonObjectCallback);
                Bundle parameters = new Bundle();
                parameters.putInt("limit", 500);
                parameters.putString("fields", "id,name,email,taggable_friends");
                request.setParameters(parameters);
                request.executeAsync();
            }

            @Override
            public void onCancel() {}

            @Override
            public void onError(FacebookException error) {}
        });


        //Popup Window Size
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        int width = dm.widthPixels;
        int height = dm.heightPixels;
        getWindow().setLayout((int) (width*0.7),(int) (height*0.4));
        getWindow().setBackgroundDrawable(new ColorDrawable(0xb0000000));
        //RelativeLayout back_dim_layout = (RelativeLayout) findViewById(R.id.bac_dim_layout);
        //getLayoutInflater().inflate(R.layout.popup_dim_effect,null);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        cbmanager.onActivityResult(requestCode, resultCode, data);
    }

    GraphRequest.GraphJSONObjectCallback jsonObjectCallback =
            new GraphRequest.GraphJSONObjectCallback() {
        @Override
        public void onCompleted(final JSONObject user, GraphResponse response) {
            App.userFBinfo = user;
            if (response.getError() == null) {
                setResult(RESULT_OK);
            }
        }
    };

    public boolean isLoggedIn() {
        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        return accessToken != null;
    }



}
