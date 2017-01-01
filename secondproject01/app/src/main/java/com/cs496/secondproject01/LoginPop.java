package com.cs496.secondproject01;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;

import com.facebook.FacebookSdk;

import android.provider.ContactsContract;
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


import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.Buffer;
import java.util.concurrent.ExecutionException;

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

                //sendPOST getFBContacts;
               /* JSONObject friends = null;
                String graphURL = "https://graph.facebook.com/v2.8/" +loginResult.getAccessToken().getUserId()+"/"+"taggable_friends?limit=500&access_token="+loginResult.getAccessToken().getToken();
                try {
                    friends = new getJSON(graphURL,"","text/plain").execute().get();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
                new sendJSON("http://52.78.200.87:3000",
                        friends.toString(), "application/json").execute();
                setResult(RESULT_OK);
                */
                GraphRequest request;
                mToken = loginResult.getAccessToken();
                request = GraphRequest.newMeRequest(mToken,
                        new GraphRequest.GraphJSONObjectCallback() {
                            @Override
                            public void onCompleted(final JSONObject user, GraphResponse response) {
                                App.userFBinfo = user;
                                JSONArray deviceContacts = getDeviceContacts();
                                new sendJSON("http://52.78.200.87:3000",
                                        user.toString(), "application/json").execute();
                                if (response.getError() == null) {
                                    setResult(RESULT_OK);
                                }
                            }
                        });

                Bundle parameters = new Bundle();
                parameters.putString("taggable_friends.limit", "200");
                parameters.putString("fields", "id,name,email,taggable_friends");
                request.setParameters(parameters);
                request.executeAsync();

            }

            @Override
            public void onCancel() {
            }

            @Override
            public void onError(FacebookException error) {
            }
        });


        //Popup Window Size
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        int width = dm.widthPixels;
        int height = dm.heightPixels;
        getWindow().setLayout((int) (width * 0.7), (int) (height * 0.4));
        getWindow().setBackgroundDrawable(new ColorDrawable(0xb0000000));
        //RelativeLayout back_dim_layout = (RelativeLayout) findViewById(R.id.bac_dim_layout);
        //getLayoutInflater().inflate(R.layout.popup_dim_effect,null);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        cbmanager.onActivityResult(requestCode, resultCode, data);
    }


    public boolean isLoggedIn() {
        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        return accessToken != null;
    }


    // AsyncTask to send JSON to our MongoDB
    private class sendJSON extends AsyncTask<Void, Void, Void> {
        String urlstr;
        String data;
        String contentType;

        public sendJSON(String url, String data, String contentType) {
            this.urlstr = url;
            this.data = data;
            this.contentType = contentType;
        }

        @Override
        protected Void doInBackground(Void... params) {
            HttpURLConnection conn;
            OutputStream os;
            InputStream is;
            BufferedReader reader;

            try {
                URL url = new URL(urlstr);
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", contentType);
                conn.setRequestProperty("Accept-Charset", "UTF-8");
                conn.setRequestProperty("Content-Length", Integer.toString(data.getBytes().length));
                conn.setDoOutput(true);
                conn.setUseCaches(false);
                conn.setDoInput(true);
                //conn.connect();

                os = new BufferedOutputStream(conn.getOutputStream());
                os.write(data.getBytes());
                os.flush();
                os.close();

                is = conn.getInputStream();
                reader = new BufferedReader(new InputStreamReader(is));
                String line;
                StringBuffer response = new StringBuffer();
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                    response.append('\r');
                }
                reader.close();
                App.response = response.toString();
                //is.close();
                conn.disconnect();
            } catch (MalformedURLException ex) {
                ex.printStackTrace();
            } catch (IOException ex) {
                ex.printStackTrace();
            } catch (Exception ex) {
                ex.printStackTrace();
            }


            return null;
        }
    }


    public JSONArray getDeviceContacts() {

        requestPermissions(new String[]{Manifest.permission.READ_CONTACTS}, 1000);
        requestPermissions(new String[]{Manifest.permission.CALL_PHONE}, 1000);
        Uri uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;

        String[] projection = new String[]{
                ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
                ContactsContract.CommonDataKinds.Phone.NUMBER,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME};
        //ContactsContract.CommonDataKinds.Phone.ADDRESS };

        String[] selectionArgs = null;

        String sortOrder = ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME
                + " COLLATE LOCALIZED ASC";

        Cursor contactCursor = getApplicationContext().getContentResolver().
                query(uri, projection, null, selectionArgs, sortOrder);

        //ArrayList<Contact> contactlist = new ArrayList<Contact>();

        JSONArray contactlist = new JSONArray();

        //
        if (contactCursor.moveToFirst()) {
            do {
                String phonenumber = contactCursor.getString(1).replaceAll("-", "");
                if (phonenumber.length() == 10) {
                    phonenumber = phonenumber.substring(0, 3) + "-"
                            + phonenumber.substring(3, 6) + "-"
                            + phonenumber.substring(6);
                } else if (phonenumber.length() > 8) {
                    phonenumber = phonenumber.substring(0, 3) + "-"
                            + phonenumber.substring(3, 7) + "-"
                            + phonenumber.substring(7);
                }

                try {
                    JSONObject obj = new JSONObject();
                    obj.put("name", contactCursor.getString(2));
                    obj.put("mobile", phonenumber);
                    contactlist.put(obj);
                } catch (JSONException e) {
                    e.printStackTrace();
                }


            } while (contactCursor.moveToNext());
        }

        return contactlist;
    }


    // AsyncTask to get JSON from our MongoDB or Facebook Graph
    private class getJSON extends AsyncTask<Void, Void, JSONObject> {
        String urlstr;
        String data;
        String contentType;

        public getJSON(String url, String data, String contentType) {
            this.urlstr = url;
            this.data = data;
            this.contentType = contentType;
        }

        @Override
        protected JSONObject doInBackground(Void... params) {
            HttpURLConnection conn;
            OutputStream os;
            InputStreamReader is;
            BufferedReader br;
            JSONObject json;

            try {
                URL url = new URL(urlstr);
                conn = (HttpURLConnection) url.openConnection();
                conn.setDoOutput(true);
                conn.setRequestMethod("POST");
                //conn.setRequestProperty("Content-Type", contentType);
                //conn.setRequestProperty("Content-Length", Integer.toString(data.length()));
                conn.setDoInput(true);
                conn.connect();

                os = new BufferedOutputStream(conn.getOutputStream());
                //os.write(data.getBytes());
                os.flush();

                is = new InputStreamReader(conn.getInputStream(), "UTF-8");
                br = new BufferedReader(is);
                StringBuilder sb = new StringBuilder();
                String line = null;
                while ((line = br.readLine()) != null) {
                    sb.append(line + "\n");
                }
                json = new JSONObject(sb.toString());

                os.close();
                br.close();
                conn.disconnect();
                return json;
            } catch (MalformedURLException ex) {
                ex.printStackTrace();
                return null;
            } catch (IOException ex) {
                ex.printStackTrace();
                return null;
            } catch (Exception ex) {
                ex.printStackTrace();
                return null;
            }
        }
    }

}






