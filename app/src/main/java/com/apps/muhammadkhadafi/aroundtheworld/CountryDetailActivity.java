package com.apps.muhammadkhadafi.aroundtheworld;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;

/**
 * Created by muhammadkhadafi on 12/7/14.
 */
public class CountryDetailActivity extends Activity {

    TextView txtCityName;
    ImageView imgCity;
    ImageView imgFlag;
    TextView txtCityStory;
    Button btnBack;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_country_detail);

        txtCityName = (TextView) findViewById(R.id.txt_cityname);
        imgCity = (ImageView) findViewById(R.id.img_countrypic);
        imgFlag = (ImageView) findViewById(R.id.img_flagpic);
        txtCityStory = (TextView) findViewById(R.id.txt_description);
        btnBack = (Button) findViewById(R.id.btn_back);

        Bundle b = getIntent().getExtras();
        txtCityName.setText(b.getString("currentcity"));

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(),HomeActivity.class);
                startActivity(i);
            }
        });

        new GetCountryDetailsAsync().execute(txtCityName.getText().toString());
    }

    private class GetCountryDetailsAsync extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {

            String result = "";

            try {
                result = getCountryDetails(params[0]);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            return result;
        }

        public String getCountryDetails(String city) throws UnsupportedEncodingException {
            // Create a new HttpClient and Post Header

            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost("http://shielded-taiga-6664.herokuapp.com/articles/getcountryfood");

            try {

                JSONObject user = new JSONObject();
                try {
                    user.put("city", city);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                StringEntity se = new StringEntity(user.toString());
                se.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));

                httppost.setEntity(se);

                // Execute HTTP Post Request
                HttpResponse response = httpclient.execute(httppost);
                String responseString = EntityUtils.toString(response.getEntity());
                Log.d("response", responseString);

                JSONObject jsonObject = new JSONObject(responseString);
                String pictureUrl = jsonObject.getJSONObject("city").getString("picture");
                String story =  jsonObject.getJSONObject("city").getString("story");

                return pictureUrl + "----" + story;
            } catch (Exception e) {
                e.printStackTrace();
                return "";
            }
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            // TextView resultText = (TextView) findViewById(R.id.result_text);

            // show result in textView
            if (result == "no results") {
                // TODO : Do something to handle no results
            } else {
                txtCityStory.setText(result.split("----")[1]);
                new DownloadImageTask(imgCity).execute(result.split("----")[0]);
            }
        }
    }

    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView imgCity;

        public DownloadImageTask(ImageView imgCity) {
            this.imgCity = imgCity;
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            float scaleY = (float) imgCity.getHeight() / result.getHeight();
            float scaleX = (float) imgCity.getWidth() / result.getWidth();
            float scale = (scaleX <= scaleY) ? scaleX : scaleY;
            Matrix matrix = new Matrix();
            matrix.postScale(scale, scale);
            Bitmap scaledBitmap = Bitmap.createBitmap(result, 0, 0, result.getWidth(), result.getHeight(), matrix, true);

            imgCity.setImageBitmap(scaledBitmap);
        }
    }
}
