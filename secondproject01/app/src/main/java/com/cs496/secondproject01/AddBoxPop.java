package com.cs496.secondproject01;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.os.Environment;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.TextInputEditText;
import android.text.InputType;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.MultiAutoCompleteTextView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.yongbeam.y_photopicker.util.photopicker.PhotoPagerActivity;
import com.yongbeam.y_photopicker.util.photopicker.PhotoPickerActivity;
import com.yongbeam.y_photopicker.util.photopicker.utils.YPhotoPickerIntent;

import org.json.JSONArray;
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
import java.util.Calendar;
import java.util.GregorianCalendar;
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
    private String date;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_box_pop);

        //Set typeface for all text in the layout
        TextView txt0 = (TextView) findViewById(R.id.add_msg);
        TextView txt1 = (TextView) findViewById(R.id.with);
        TextView txt2 = (TextView) findViewById(R.id.action);
        CheckBox b_eat = (CheckBox) findViewById(R.id.eat_box);
        CheckBox b_movie = (CheckBox) findViewById(R.id.movie_box);
        CheckBox b_coffee = (CheckBox) findViewById(R.id.coffe_box);
        CheckBox b_travel = (CheckBox) findViewById(R.id.travel_box);
        CheckBox b_game = (CheckBox) findViewById(R.id.game_box);
        CheckBox b_work = (CheckBox) findViewById(R.id.work_box);
        CheckBox b_study = (CheckBox) findViewById(R.id.study_box);
        CheckBox b_ex = (CheckBox) findViewById(R.id.ex_box);
        Button b_complete = (Button) findViewById(R.id.complete);
        Button b_photo = (Button) findViewById(R.id.select_photos);
        Button b_date = (Button) findViewById(R.id.select_date);
        txt0.setTypeface(App.myFont);
        txt1.setTypeface(App.myFont);
        txt2.setTypeface(App.myFont);
        b_eat.setTypeface(App.myFont);
        b_movie.setTypeface(App.myFont);
        b_coffee.setTypeface(App.myFont);
        b_travel.setTypeface(App.myFont);
        b_game.setTypeface(App.myFont);
        b_work.setTypeface(App.myFont);
        b_study.setTypeface(App.myFont);
        b_ex.setTypeface(App.myFont);
        b_complete.setTypeface(App.myFont);
        b_photo.setTypeface(App.myFont);
        b_date.setTypeface(App.myFont);

        RelativeLayout bg = (RelativeLayout) findViewById(R.id.activity_add_box_pop);
        Random rand = new Random();
        //bg.setBackgroundResource(cards.get(rand.nextInt(8)));

        // DatePicker setup
        Button getDate = (Button) findViewById(R.id.select_date);
        GregorianCalendar calendar = new GregorianCalendar();
        final int year = calendar.get(Calendar.YEAR);
        final int month = calendar.get(Calendar.MONTH);
        final int day = calendar.get(Calendar.DAY_OF_MONTH);
        getDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog dialog = new DatePickerDialog(AddBoxPop.this,
                        listener, year,month,day);
                dialog.show();
            }
        });

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
        /*ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_dropdown_item_1line, names);
        MultiAutoCompleteTextView with_input = (MultiAutoCompleteTextView) findViewById(R.id.with_input);
        with_input.setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer());
        with_input.setInputType(InputType.TYPE_CLASS_TEXT);
        with_input.setThreshold(1);
        with_input.setAdapter(adapter);*/

        final MultiAutoCompleteTextView mt=(MultiAutoCompleteTextView)
                findViewById(R.id.with_input);
        mt.setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer());

        ArrayAdapter<String> adp=new ArrayAdapter<String>(this,
                android.R.layout.simple_dropdown_item_1line,names);

        mt.setThreshold(1);
        mt.setAdapter(adp);
/*
        mt.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                mt.showDropDown();
                return true;
            }
        });*/

        //"상자에 추가하기" button
        Button complete = (Button) findViewById(R.id.complete);
        complete.setOnClickListener(completeOnClickListener);


        //Popup Window Size
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        int width = dm.widthPixels;
        int height = dm.heightPixels;
        getWindow().setLayout((int) (width * 0.8), (int) (height * 0.72));
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

    private DatePickerDialog.OnDateSetListener listener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            String msg = year + "-" + monthOfYear+1 + "-" + dayOfMonth;
            if (dayOfMonth < 10)
                msg = year + "-" + monthOfYear+1 + "-0" + dayOfMonth;
            date = msg;
            Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
        }
    };

    //============================================================================================//

    //"상자에 추가하기" 버튼 눌렀을 때
    Button.OnClickListener completeOnClickListener = new View.OnClickListener() {
        public void onClick(View v) {
            JSONArray photos = new JSONArray();

            //선택한 사진 DB에 보내고 id 가져오기
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
                    photos.put(result.getString("img_id"));
                    //selectedPhotos.set(i,result.getString("img_id"));
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


            //날짜 받아오기
            Button getDate = (Button) findViewById(R.id.select_date);



            //앨범 이름 받아오기
            EditText album_name = (EditText) findViewById(R.id.album_name);
            String album = album_name.getText().toString();

            //친구 태그 가져오기
            MultiAutoCompleteTextView with_input = (MultiAutoCompleteTextView) findViewById(R.id.with_input);
            String s = with_input.getText().toString();
            String[] friendNames = s.split(",");
            JSONArray friend_ids = new JSONArray();
            for (int i = 0; i < friendNames.length ; i++)
                friend_ids.put(App.friend_map.get(friendNames[i].trim()));


            //활동 태그 가져오기
            CheckBox b_eat = (CheckBox) findViewById(R.id.eat_box);
            CheckBox b_movie = (CheckBox) findViewById(R.id.movie_box);
            CheckBox b_coffee = (CheckBox) findViewById(R.id.coffe_box);
            CheckBox b_travel = (CheckBox) findViewById(R.id.travel_box);
            CheckBox b_game = (CheckBox) findViewById(R.id.game_box);
            CheckBox b_work = (CheckBox) findViewById(R.id.work_box);
            CheckBox b_study = (CheckBox) findViewById(R.id.study_box);
            CheckBox b_ex = (CheckBox) findViewById(R.id.ex_box);
            JSONObject activity = new JSONObject();
            try {
                activity.put("work",bool2int(b_work.isChecked()));
                activity.put("study",bool2int(b_study.isChecked()));
                activity.put("food",bool2int(b_eat.isChecked()));
                activity.put("cafe",bool2int(b_coffee.isChecked()));
                activity.put("sports",bool2int(b_ex.isChecked()));
                activity.put("game",bool2int(b_game.isChecked()));
                activity.put("travel",bool2int(b_travel.isChecked()));
                //activity.put("movie",bool2int(b_movie.isChecked()));
            } catch (JSONException e) {
                e.printStackTrace();
            }

            //Create Album Request
            try {
                JSONObject req = new JSONObject();
                req.put("type", "CREATE_ALBUM");
                req.put("user_id",App.db_user_id);
                req.put("album_name",album);
                req.put("date",date);
                req.put("activity",activity);
                req.put("friend_id_list",friend_ids);
                req.put("img_id_list",photos);
                JSONObject result = new sendJSON("http://52.78.200.87:3000",
                        req.toString(), "application/json").execute().get();
                Log.v("create album", result.toString());
                if (result.getString("result") == "success") {
                    Toast.makeText(getApplicationContext(), "추가 완료:)", Toast.LENGTH_SHORT).show();
                    onCreate(null);
                } else {
                    Toast.makeText(getApplicationContext(), "추가 실패:(", Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }
    };


    String bool2int(Boolean bool) {
        String s = (bool) ? "1" : "0";
        return s;
    }
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
