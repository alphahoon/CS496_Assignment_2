package com.cs496.secondproject01;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.os.Environment;
import android.support.design.widget.CoordinatorLayout;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.MultiAutoCompleteTextView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.yongbeam.y_photopicker.util.photopicker.PhotoPagerActivity;
import com.yongbeam.y_photopicker.util.photopicker.PhotoPickerActivity;
import com.yongbeam.y_photopicker.util.photopicker.utils.YPhotoPickerIntent;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutionException;

public class AddBoxPop extends Activity {
    private ArrayList<Integer> cards =
            new ArrayList<Integer>(Arrays.asList(R.drawable.card0,R.drawable.card1,R.drawable.card2,
                                                R.drawable.card3,R.drawable.card4,R.drawable.card5,
                                                R.drawable.card6,R.drawable.card7));


    public final static int REQUEST_CODE = 1;
    private Button getphoto;
    public static ArrayList<String> selectedPhotos = new ArrayList<>();



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_box_pop);

        //Set typeface for all text in the layout
        TextView txt0 = (TextView) findViewById(R.id.add_msg);
        TextView txt1 = (TextView) findViewById(R.id.with);
        TextView txt2 = (TextView) findViewById(R.id.action);
        txt0.setTypeface(App.myFont);
        txt1.setTypeface(App.myFont);
        txt2.setTypeface(App.myFont);

        RelativeLayout bg = (RelativeLayout) findViewById(R.id.activity_add_box_pop);
        Random rand = new Random();
        //bg.setBackgroundResource(cards.get(rand.nextInt(8)));

        // Select photos from the gallery
        getphoto = (Button) findViewById(R.id.select_photos);
        getphoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                YPhotoPickerIntent intent = new YPhotoPickerIntent(AddBoxPop.this);
                intent.setMaxSelectCount(20);
                intent.setShowCamera(true);
                intent.setShowGif(true);
                intent.setSelectCheckBox(false);
                intent.setMaxGrideItemCount(3);
                startActivityForResult(intent, REQUEST_CODE);
            }
        });

        // Select With Friends
        String[] names = App.names;
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_dropdown_item_1line, names);
        MultiAutoCompleteTextView with_input = (MultiAutoCompleteTextView) findViewById(R.id.with_input);
        with_input.setAdapter(adapter);
        with_input.setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer());

        //"상자에 추가하기" button
        Button complete = (Button) findViewById(R.id.complete);
        complete.setOnClickListener(completeOnClickListener);


        //Popup Window Size
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        int width = dm.widthPixels;
        int height = dm.heightPixels;
        getWindow().setLayout((int) (width * 0.8), (int) (height * 0.7));
    }

    //============================================================================================//
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        List<String> photos = null;
        selectedPhotos = new ArrayList<>();
        if (resultCode == RESULT_OK && requestCode == REQUEST_CODE) {
            if (data != null) {
                photos = data.getStringArrayListExtra(PhotoPickerActivity.KEY_SELECTED_PHOTOS);
            }
            if (photos != null) {
                selectedPhotos.addAll(photos);
            }


            // start image viewer
            //Intent startActivity = new Intent(this , PhotoPagerActivity.class);
            //startActivity.putStringArrayListExtra("photos" , selectedPhotos);
            //startActivity(startActivity);
        }
    }

    //============================================================================================//

    //"상자에 추가하기" 버튼 눌렀을 때
    Button.OnClickListener completeOnClickListener = new View.OnClickListener() {
        public void onClick(View v) {
            for (int i = 0; i < selectedPhotos.size(); i++) {
                //File sd = Environment.getExternalStorageDirectory();
                String filePath = selectedPhotos.get(i);
                //File image = new File(filePath, "img"+i);
                //BitmapFactory.Options bmOptions = new BitmapFactory.Options();

                Bitmap bitmap = BitmapFactory.decodeFile(filePath);
                //bitmap = Bitmap.createScaledBitmap(bitmap, dstWidth, dstHeight, true);
                Log.v("bitmap",bitmap.toString());
                ByteArrayOutputStream ByteStream= new  ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG,20, ByteStream);
                byte [] b=ByteStream.toByteArray();
                String encoded =Base64.encodeToString(b, Base64.NO_WRAP);
                encoded = "data:image/jpeg;base64,"+encoded;

                Log.v("encoded", encoded);


                try {
                    JSONObject req = new JSONObject();
                    req.put("type", "UPLOAD_IMG");
                    req.put("user_id", App.db_user_id);
                    req.put("img", encoded);
                    JSONObject result = new sendJSON("http://52.78.200.87:3000",
                            req.toString(), "application/json").execute().get();
                    //Log.v("Sent Image", result.toString());
                    Log.v("db_user_id", App.db_user_id);
                    selectedPhotos.set(i,result.getString("img_id"));
                    Log.v("photo ids", selectedPhotos.toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }


                //bitmap = Bitmap.createScaledBitmap(bitmap,parent.getWidth(),parent.getHeight(),true);
            }
        }
    };


    //============================================================================================//
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
    //============================================================================================//


}
