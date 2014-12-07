package com.apps.muhammadkhadafi.aroundtheworld;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.PutObjectResult;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by muhammadkhadafi on 12/6/14.
 */
public class DailyNutritionActivity extends Activity {

    private ProgressBar progCalorie;
    private ProgressBar progFat;
    private ProgressBar progSaturatedFat;
    private ProgressBar progCholesterol;
    private ProgressBar progSodium;
    private ProgressBar progCarbohydrate;
    private ProgressBar progFiber;

    private TextView txtCalorie;
    private TextView txtFat;
    private TextView txtSaturatedFat;
    private TextView txtCholesterol;
    private TextView txtSodium;
    private TextView txtCarbohydrate;
    private TextView txtFiber;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nutrition);

        txtCalorie = (TextView) findViewById(R.id.txt_cal);
        txtFat = (TextView) findViewById(R.id.txt_fat);
        txtSaturatedFat = (TextView) findViewById(R.id.txt_satfat);
        txtCholesterol = (TextView) findViewById(R.id.txt_cholesterol);
        txtSodium = (TextView) findViewById(R.id.txt_sodium);
        txtCarbohydrate = (TextView) findViewById(R.id.txt_carbohydrate);
        txtFiber = (TextView) findViewById(R.id.txt_fiber);

        progCalorie = (ProgressBar) findViewById(R.id.prog_cal);
        progFat = (ProgressBar) findViewById(R.id.prog_fat);
        progSaturatedFat = (ProgressBar) findViewById(R.id.prog_satfat);
        progCholesterol = (ProgressBar) findViewById(R.id.prog_cholesterol);
        progSodium = (ProgressBar) findViewById(R.id.prog_sodium);
        progCarbohydrate = (ProgressBar) findViewById(R.id.prog_carbohydrate);
        progFiber = (ProgressBar) findViewById(R.id.prog_fiber);

        new NutritionAsyncTask().execute();

        // TODO Everything about this
    }

    private class NutritionAsyncTask extends AsyncTask<String, Void, String[]> {


        protected String[] doInBackground(String... params) {

            SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPrefs", 0);
            String userid = pref.getString("id", "");
            String username = pref.getString("username", "");

            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost("http://shielded-taiga-6664.herokuapp.com/articles/date");

            String[] foodNutritionArray = new String[0];

            try {
                // Add your data

                Date d = new Date(System.currentTimeMillis());
                SimpleDateFormat sdmMonth = new SimpleDateFormat("MM");
                SimpleDateFormat sdmDay = new SimpleDateFormat("dd");

                JSONObject user = new JSONObject();
                try {
                    user.put("user", userid);
                    user.put("username", username);
                    user.put("day", sdmDay.format(d));
                    user.put("month", sdmMonth.format(d));
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

                JSONArray jsonArray = new JSONArray(responseString);
                String nutritions = "";
                String foodNutrition = "";
                foodNutritionArray = new String[jsonArray.length()];

                for (int i = 0; i < jsonArray.length(); i++) {
                    nutritions = jsonArray.getJSONObject(i).getString("nutrition");
                    JSONObject nutritionJson = new JSONObject(nutritions);

                    foodNutrition = foodNutrition + nutritionJson.getString("calorie") + "----";
                    foodNutrition = foodNutrition + nutritionJson.getString("total_fat") + "----";
                    foodNutrition = foodNutrition + nutritionJson.getString("saturated_fat") + "----";
                    foodNutrition = foodNutrition + nutritionJson.getString("cholesterol") + "----";
                    foodNutrition = foodNutrition + nutritionJson.getString("sodium") + "----";
                    foodNutrition = foodNutrition + nutritionJson.getString("total_carbohydrate") + "----";
                    foodNutrition = foodNutrition + nutritionJson.getString("dietary_fiber") + "----";

                    foodNutritionArray[i] = foodNutrition;
                    foodNutrition = "";
                }

//                Log.d("response", nutritions);

//                Log.d("response", "nut" + EntityUtils.toString(response.getEntity()));

            } catch(Exception e) {
                e.printStackTrace();
                Log.d("Error", "Cannot Estabilish Connection");
            }

            return foodNutritionArray;
        }

        @Override
        protected void onPostExecute(String[] result) {
            super.onPostExecute(result);

            // TextView resultText = (TextView) findViewById(R.id.result_text);

            // show result in textView
            if (result.length == 0) {
                // TODO : Do something to handle no results
            } else {
                String[] eachNutrition = new String[0];

                progCalorie.setProgress(0);
                progFat.setProgress(0);
                progSaturatedFat.setProgress(0);
                progCholesterol.setProgress(0);
                progSodium.setProgress(0);
                progCarbohydrate.setProgress(0);
                progFiber.setProgress(0);

                for (int i = 0; i < result.length; i++) {
                    Log.d("response", result[i]);
                    eachNutrition = result[i].split("----");
                    progCalorie.setProgress(progCalorie.getProgress() + ((eachNutrition[0].equals("null")) ? 0 : (int) Float.parseFloat(eachNutrition[0])));
                    progFat.setProgress(progFat.getProgress() + ((eachNutrition[1].equals("null")) ? 0 : (int) Float.parseFloat(eachNutrition[1])));
                    progSaturatedFat.setProgress(progSaturatedFat.getProgress() + ((eachNutrition[2].equals("null")) ? 0 : (int) Float.parseFloat(eachNutrition[2])));
                    progCholesterol.setProgress(progCholesterol.getProgress() + ((eachNutrition[3].equals("null")) ? 0 : (int) Float.parseFloat(eachNutrition[3])));
                    progSodium.setProgress(progSodium.getProgress() + ((eachNutrition[4].equals("null")) ? 0 : (int) Float.parseFloat(eachNutrition[4])));
                    progCarbohydrate.setProgress(progCarbohydrate.getProgress() + ((eachNutrition[5].equals("null")) ? 0 : (int) Float.parseFloat(eachNutrition[5])));
                    progFiber.setProgress(progFiber.getProgress() + ((eachNutrition[6].equals("null")) ? 0 : (int) Float.parseFloat(eachNutrition[6])));
                }

                txtCalorie.setText(progCalorie.getProgress() + " / 2000 kcal");
                txtFat.setText(progFat.getProgress() + " / 65 gr");
                txtSaturatedFat.setText(progSaturatedFat.getProgress() + " / 20 gr");
                txtCholesterol.setText(progCholesterol.getProgress() + " / 300 mg");
                txtSodium.setText(progSodium.getProgress() + " / 2400 mg");
                txtCarbohydrate.setText(progCarbohydrate.getProgress() + " / 300 gr");
                txtFiber.setText(progFiber.getProgress() + " / 20 gr");

            }
        }

    }
}
