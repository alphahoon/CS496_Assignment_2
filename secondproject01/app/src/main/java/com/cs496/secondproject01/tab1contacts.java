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
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cs496.secondproject01.dummy.DummyContent;
import com.cs496.secondproject01.dummy.DummyContent.DummyItem;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.login.widget.LoginButton;

import static com.cs496.secondproject01.R.id.container;


public class tab1contacts extends Fragment {

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
        if (!isLoggedIn()) {
            //LoginButton loginButton = (LoginButton) view.findViewById(R.id.login_button);
            //loginButton.setVisibility(View.INVISIBLE);
            App.firstAccess = false;
            Intent popupIntent = new Intent(getActivity(), LoginPop.class);
            startActivity(popupIntent);
        }
        return view;
    }

    public void onResume() {
        super.onResume();
    }

    public boolean isLoggedIn() {
        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        return accessToken != null;
    }




}



