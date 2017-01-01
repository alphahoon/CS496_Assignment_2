package com.cs496.secondproject01;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.os.Bundle;
import com.facebook.FacebookSdk;

import com.cs496.secondproject01.dummy.DummyContent;
import com.cs496.secondproject01.dummy.DummyContent.DummyItem;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;

import static android.app.Activity.RESULT_OK;
import static com.cs496.secondproject01.R.id.container;


public class tab1contacts extends Fragment {
    JSONArray contactsjson;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.tab1contacts, container, false);
        //LoginManager.getInstance().logOut();
/*
        FloatingActionButton fb = (FloatingActionButton) view.findViewById(R.id.fab);
        fb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent popupIntent = new Intent(getActivity(), LoginPop.class);
                startActivity(popupIntent);
            }
        });
*/
        //if (!isLoggedIn() && App.firstAccess) {
        //if (!isLoggedIn()) {
        if (true) {
            //LoginButton loginButton = (LoginButton) view.findViewById(R.id.login_button);
            //loginButton.setVisibility(View.INVISIBLE);
            //App.firstAccess = false;
            Intent popupIntent = new Intent(getActivity(), LoginPop.class);
            startActivity(popupIntent);
        }

        //Facebook sdk에서 friends 받아오기

        try {
            InputStream contacts = getActivity().getAssets().open("contacts.json");
            contactsjson = JSONgetContacts(contacts);
        } catch (IOException e) {
            e.printStackTrace();
        }

        //if (App.userFBinfo == null) { fetchContacts(); }
        //fetchContacts();
/*
        try {
            JSONObject friends = App.userFBinfo.getJSONObject("taggable_friends");
            contactsjson = JSONgetContacts(friends);
        } catch (JSONException e) {
            e.printStackTrace();
        }
*/

        //Handle ListView
        ListView conList = (ListView) view.findViewById(R.id.contact_list);
        ContactViewAdapter adapter = new ContactViewAdapter(getActivity(),
                R.layout.contact_item, contactsjson);
        conList.setAdapter(adapter);
        return view;
    }

    JSONArray JSONgetContacts (InputStream raw) {
        String json = null;
        try {
            int size = raw.available();
            byte[] buffer = new byte[size];
            raw.read(buffer);
            raw.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        try {
            JSONArray contactsjson = new JSONArray(json);
            return contactsjson;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    /*
    JSONArray JSONgetContacts (JSONObject data) {
        try {
            JSONArray contactsjson = data.getJSONArray("data");
            return contactsjson;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }*/
/*
    void fetchContacts() {
        new Thread() {
            public void run() {
                AccessToken mToken = AccessToken.getCurrentAccessToken();
                GraphRequest request;
                request = GraphRequest.newMeRequest(mToken,
                        new GraphRequest.GraphJSONObjectCallback() {
                            @Override
                            public void onCompleted(final JSONObject user, GraphResponse response) {
                                App.userFBinfo = user;
                                if (response.getError() == null) {
                                    getActivity().setResult(RESULT_OK);
                                }
                            }
                        });
                Bundle parameters = new Bundle();
                parameters.putInt("limit", 500);
                parameters.putString("fields", "id,name,email,taggable_friends");
                request.setParameters(parameters);
                request.executeAndWait();
            }
        }.start();
    }
    */



    public void onResume() {
        super.onResume();
    }

    public boolean isLoggedIn() {
        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        return accessToken != null;
    }




}



