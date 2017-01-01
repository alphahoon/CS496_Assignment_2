package com.cs496.secondproject01;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

/**
 * Created by q on 2016-12-31.
 */

public class ContactViewAdapter extends BaseAdapter {

    Context context;
    int resource;
    JSONArray contactsjson;

    // Adapter에 추가된 데이터를 저장하기 위한 ArrayList
    //private ArrayList<ListViewItem> listViewItemList = new ArrayList<ListViewItem>() ;

    public ContactViewAdapter(Context context, int resource, JSONArray contactsjson) {
        this.context = context;
        this.resource = resource;
        this.contactsjson = contactsjson;
    }

    // position에 위치한 데이터를 화면에 출력하는데 사용될 View를 리턴. : 필수 구현
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final int pos = position;
        final Context context = parent.getContext();

        // "listview_item" Layout을 inflate하여 convertView 참조 획득.
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.contact_item, parent, false);
        }

        // 화면에 표시될 View(Layout이 inflate된)으로부터 위젯에 대한 참조 획득
        //ImageView thumnailView = (ImageView) convertView.findViewById(R.id.list_thumnail) ;
        TextView nameView = (TextView) convertView.findViewById(R.id.list_name) ;
        TextView noView = (TextView) convertView.findViewById(R.id.list_no);
        TextView emailView = (TextView) convertView.findViewById(R.id.list_email);

        try {
            JSONObject jObject = contactsjson.getJSONObject(position);
            String name = jObject.getString("name");
            String number = jObject.getString("number");
            // 아이템 내 각 위젯에 데이터 반영
            //thumnailView.setImageDrawable(listViewItem.getIcon());
            nameView.setText(name);
            noView.setText(number);
            //emailView.setText(email);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return convertView;
    }


    // 지정한 위치(position)에 있는 데이터와 관계된 아이템(row)의 ID를 리턴. : 필수 구현
    @Override
    public long getItemId(int position) {
        return position ;
    }

    @Override
    public int getCount() {
        return contactsjson.length();
    }

    @Override
    public Object getItem(int position) { return position; }



    /*
    public void addItem(Drawable icon, String title, String desc) {
        ListViewItem item = new ListViewItem();
        item.setIcon(icon);
        item.setTitle(title);
        item.setDesc(desc);
        listViewItemList.add(item);
    }*/
}
