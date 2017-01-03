package com.cs496.secondproject01;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
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
                                JSONObject obj = new JSONObject();
                                JSONObject result = new JSONObject();

                                try {
                                    // Create new Database for this user
                                    obj.put("type", "NEW_USER");
                                    obj.put("name", user.getString("name"));
                                    obj.put("email", user.getString("email"));
                                    //App.userFBinfo = user;
                                    result = new sendJSON("http://52.78.200.87:3000",
                                            obj.toString(), "application/json").execute().get();
                                    App.db_user_id = result.getString("user_id");

                                    // Send Device Contact
                                    JSONArray deviceContacts = getDeviceContacts();
                                    obj = new JSONObject();
                                    obj.put("type", "ADD_CONTACTS");
                                    obj.put("user_id", App.db_user_id);
                                    obj.put("contacts", deviceContacts);
                                    new sendJSON("http://52.78.200.87:3000",
                                            obj.toString(), "application/json").execute();

                                    // Send Facebook Contact
                                    sendFBcontacts(user.getJSONObject("taggable_friends"));

                                    // Retrieve Contacts from DB
                                    obj = new JSONObject();
                                    obj.put("type","GET_CONTACTS");
                                    obj.put("user_id", App.db_user_id);
                                    result = new sendJSON("http://52.78.200.87:3000",
                                            obj.toString(), "application/json").execute().get();

                                    //Update Information in App variables
                                    App.friends = result.getJSONArray("contacts");
                                    for (int i = 0; i < App.friends.length(); i++) {
                                        JSONObject f = App.friends.getJSONObject(i);
                                        App.names[i] = f.getString("name");
                                        App.friend_map.put(f.getString("name"),
                                                f.getString("friend_id"));
                                    }

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                } catch (ExecutionException e) {
                                    e.printStackTrace();
                                }

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
    private class sendJSON extends AsyncTask<Void, Void, JSONObject> {
        String urlstr;
        String data;
        String contentType;

        public sendJSON(String url, String data, String contentType) {
            this.urlstr = url;
            this.data = data;
            this.contentType = contentType;
        }

        @Override
        protected JSONObject doInBackground(Void... params) {
            HttpURLConnection conn;
            OutputStream os;
            InputStream is;
            BufferedReader reader;
            JSONObject json = new JSONObject();

            try {
                URL url = new URL(urlstr);
                conn = (HttpURLConnection) url.openConnection();
                conn.setDoInput(true);

                // If sending to our DB
                if (urlstr.contains("52.78.200.87")) {
                    conn.setDoOutput(true);
                    conn.setUseCaches(false);
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-Type", contentType);
                    conn.setRequestProperty("Accept-Charset", "UTF-8");
                    conn.setRequestProperty("Content-Length", Integer.toString(data.getBytes().length));

                    os = new BufferedOutputStream(conn.getOutputStream());
                    os.write(data.getBytes());
                    os.flush();
                    os.close();
                }

                int statusCode = conn.getResponseCode();
                is = conn.getInputStream();
                reader = new BufferedReader(new InputStreamReader(is));
                String line;
                StringBuffer response = new StringBuffer();
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                    response.append("\n");
                }
                reader.close();
                App.response = response.toString();
                json = new JSONObject(response.toString());

                conn.disconnect();


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

            return json;
        }

    }

    public void sendFBcontacts (JSONObject tag_friend) {
        try {
            JSONObject obj = new JSONObject();
            JSONArray contact_arr = new JSONArray();
            obj.put("type", "ADD_CONTACTS");
            obj.put("user_id", App.db_user_id);
            while (true) {
                JSONArray friends = tag_friend.getJSONArray("data");
                for (int i = 0; i < friends.length(); i++) {
                    JSONObject contact = new JSONObject();
                    JSONObject person = friends.getJSONObject(i);
                    String p_name = person.getString("name");
                    String p_pic = person.getJSONObject("picture").getJSONObject("data").getString("url");
                    contact.put("friend_id", App.md5(p_name+p_pic));
                    contact.put("name", p_name);
                    contact.put("pic", p_pic);
                    contact_arr.put(contact);
                }

                // If need to fetch more pages
                if (tag_friend.getJSONObject("paging").has("next")) {
                    String url = tag_friend.getJSONObject("paging").getString("next");
                    tag_friend = new sendJSON(url, "", "").execute().get();
                } else {break;}
            }
            obj.put("contacts", contact_arr);
            JSONObject result = new sendJSON("http://52.78.200.87:3000",
                    obj.toString(), "application/json").execute().get();
            Log.v("Sent FB contacts", result.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    public JSONArray getDeviceContacts() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{Manifest.permission.READ_CONTACTS}, 1000);
            requestPermissions(new String[]{Manifest.permission.CALL_PHONE}, 1000);
        }
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
                    obj.put("friend_id", App.md5(contactCursor.getString(2) + phonenumber));
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



}






