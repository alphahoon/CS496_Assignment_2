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

import com.cs496.secondproject01.dummy.DummyContent;
import com.cs496.secondproject01.dummy.DummyContent.DummyItem;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.login.widget.LoginButton;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.io.InputStream;

import static com.cs496.secondproject01.R.id.container;


public class tab1contacts extends Fragment {
    InputStream contacts;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.tab1contacts, container, false);

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
            contacts = getActivity().getAssets().open("contacts.json");
        } catch (IOException e) {
            e.printStackTrace();
        }

        JSONArray contactsjson = JSONparse(contacts);

        //Handle ListView
        ListView conList = (ListView) view.findViewById(R.id.contact_list);
        ContactViewAdapter adapter = new ContactViewAdapter(getActivity(), R.layout.contact_item, contactsjson);
        conList.setAdapter(adapter);
        return view;
    }


    JSONArray JSONparse (InputStream raw) {
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

    public void onResume() {
        super.onResume();
    }

    public boolean isLoggedIn() {
        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        return accessToken != null;
    }




}



