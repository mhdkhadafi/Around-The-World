package com.apps.muhammadkhadafi.aroundtheworld;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by mhd.khadafi on 10/28/2014.
 */
public class ViewImageActivity extends Activity {

    ImageView imageView;
    String url1;
    String url2;
    Bitmap bmp1;
    Bitmap bmp2;
    Button viewImage1;
    Button viewImage2;
    Button loadImages;
    Button getImage;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_images);

        imageView = (ImageView) findViewById(R.id.show_image);
        viewImage1 = (Button) findViewById(R.id.view_image_1);
        viewImage2 = (Button) findViewById(R.id.view_image_2);
        getImage = (Button) findViewById(R.id.get_image);
        loadImages = (Button) findViewById(R.id.load_images);

//        new MyAsyncTask().execute(url_string);

//        imageView.setImageBitmap(bmp);

        getImage.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                new MyAsyncTask().execute("get", "54504c40febf73080027af93");
            }
        });

        viewImage1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                imageView.setImageBitmap(bmp1);
            }
        });

        viewImage2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                imageView.setImageBitmap(bmp2);
            }
        });

        viewImage2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                new MyAsyncTask().execute("load", url1, url2);
                imageView.setImageBitmap(bmp2);
            }
        });


    }

    private class MyAsyncTask extends AsyncTask<String, Void, String> {

        String responseText;

        @Override
        protected String doInBackground(String... params) {


//            URL url = null;
//            try {
//                url = new URL(params[0]);
//            } catch (MalformedURLException e) {
//                e.printStackTrace();
//            }
//            bmp = null;
//            try {
//                bmp = BitmapFactory.decodeStream(url.openConnection().getInputStream());
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//            // imageView.setImageBitmap(bmp);
//            getData();

            if (params[0] == "get")
                postData(params[1]);
            else if (params[0] == "load")
                loadBitmap(params[1], params[2]);


            return responseText;
        }

        public void loadBitmap(String urlString1, String urlString2) {

            URL url = null;
            try {
                url = new URL(urlString1);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            bmp1 = null;
            try {
                bmp1 = BitmapFactory.decodeStream(url.openConnection().getInputStream());
            } catch (IOException e) {
                e.printStackTrace();
            }

            url = null;
            try {
                url = new URL(urlString2);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            bmp2 = null;
            try {
                bmp2 = BitmapFactory.decodeStream(url.openConnection().getInputStream());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void postData(String user_id_send) {
            // Create a new HttpClient and Post Header
            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost("http://shielded-taiga-6664.herokuapp.com/articles/all");

            try {
                // Add your data

                JSONObject user = new JSONObject();
                try {
                    user.put("user", user_id_send);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                StringEntity se = new StringEntity(user.toString());
                se.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
                httppost.setEntity(se);

                // Execute HTTP Post Request
                ResponseHandler responseHandler = new BasicResponseHandler();
                HttpResponse response = httpclient.execute(httppost);

                InputStream responseIs = response.getEntity().getContent();
                String responseString;

                // convert inputstream to string
                if(responseIs != null)
                    responseString = convertInputStreamToString(responseIs);
                else
                    responseString = "Did not work!";

                JSONArray responseJson = new JSONArray(responseString);
                JSONObject object1 = responseJson.getJSONObject(0);
                JSONObject object2 = responseJson.getJSONObject(1);

//                JSONArray jsonArray = responseJson.getJSONArray("");
                Log.d("response", object1.getString("content"));
                Log.d("response", object2.getString("content"));

                url1 = object1.getString("content");
                url2 = object2.getString("content");

            } catch(Exception e) {
                e.printStackTrace();
                Log.d("Error", "Cannot Estabilish Connection");
            }
        }

        private String convertInputStreamToString(InputStream inputStream) throws IOException{
            BufferedReader bufferedReader = new BufferedReader( new InputStreamReader(inputStream));
            String line = "";
            String result = "";
            while((line = bufferedReader.readLine()) != null)
                result += line;

            inputStream.close();
            return result;

        }
    }
}
