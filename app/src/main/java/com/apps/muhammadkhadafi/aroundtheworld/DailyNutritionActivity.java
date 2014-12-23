package com.apps.muhammadkhadafi.aroundtheworld;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
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

    private Button btnBack;
    private Button btnFoodJournal;

    private TextView txtNutritionXp;

    private int nutritionScore;

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

        btnBack = (Button) findViewById(R.id.btn_back);
        btnFoodJournal = (Button) findViewById(R.id.btn_foodjournal);

        txtNutritionXp = (TextView) findViewById(R.id.txt_nutritionxp);

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(),HomeActivity.class);
                startActivity(i);
            }
        });
        btnFoodJournal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(),FoodJournalActivity.class);
                startActivity(i);
            }
        });

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
                nutritionScore = -35;
            } else {
                String[] eachNutrition = new String[0];
                int[] nutritionValues = new int[]{0,0,0,0,0,0,0};



                for (int i = 0; i < result.length; i++) {
                    Log.d("response", result[i]);
                    eachNutrition = result[i].split("----");
                    nutritionValues[0] = nutritionValues[0] + ((eachNutrition[0].equals("null")) ? 0 : (int) Float.parseFloat(eachNutrition[0]));
                    nutritionValues[1] = nutritionValues[1] + ((eachNutrition[1].equals("null")) ? 0 : (int) Float.parseFloat(eachNutrition[1]));
                    nutritionValues[2] = nutritionValues[2] + ((eachNutrition[2].equals("null")) ? 0 : (int) Float.parseFloat(eachNutrition[2]));
                    nutritionValues[3] = nutritionValues[3] + ((eachNutrition[3].equals("null")) ? 0 : (int) Float.parseFloat(eachNutrition[3]));
                    nutritionValues[4] = nutritionValues[4] + ((eachNutrition[4].equals("null")) ? 0 : (int) Float.parseFloat(eachNutrition[4]));
                    nutritionValues[5] = nutritionValues[5] + ((eachNutrition[5].equals("null")) ? 0 : (int) Float.parseFloat(eachNutrition[5]));
                    nutritionValues[6] = nutritionValues[6] + ((eachNutrition[6].equals("null")) ? 0 : (int) Float.parseFloat(eachNutrition[6]));

                }

                progCalorie.setProgress(nutritionValues[0]);
                progFat.setProgress(nutritionValues[1]);
                progSaturatedFat.setProgress(nutritionValues[2]);
                progCholesterol.setProgress(nutritionValues[3]);
                progSodium.setProgress(nutritionValues[4]);
                progCarbohydrate.setProgress(nutritionValues[5]);
                progFiber.setProgress(nutritionValues[6]);

                if (nutritionValues[0] > progCalorie.getMax()) {
                    progCalorie.setProgressDrawable(progCalorie.getResources().getDrawable(R.drawable.redprogress));
                    nutritionScore = nutritionScore - 7;
                }
                else if (nutritionValues[0] < (progCalorie.getMax() / 2)) {
                    progCalorie.setProgressDrawable(progCalorie.getResources().getDrawable(R.drawable.yellowprogress));
                    nutritionScore = nutritionScore - 5;
                }
                else {
                    progCalorie.setProgressDrawable(progCalorie.getResources().getDrawable(R.drawable.greenprogress));
                    nutritionScore = nutritionScore + 7;
                }
                if (nutritionValues[1] > progFat.getMax()) {
                    progFat.setProgressDrawable(progFat.getResources().getDrawable(R.drawable.redprogress));
                    nutritionScore = nutritionScore - 7;
                }
                else if (nutritionValues[1] < (progFat.getMax() / 2)) {
                    progFat.setProgressDrawable(progFat.getResources().getDrawable(R.drawable.yellowprogress));
                    nutritionScore = nutritionScore - 5;
                }
                else {
                    progFat.setProgressDrawable(progFat.getResources().getDrawable(R.drawable.greenprogress));
                    nutritionScore = nutritionScore + 7;
                }
                if (nutritionValues[2] > progSaturatedFat.getMax()) {
                    progSaturatedFat.setProgressDrawable(progSaturatedFat.getResources().getDrawable(R.drawable.redprogress));
                    nutritionScore = nutritionScore - 7;
                }
                else if (nutritionValues[2] < (progSaturatedFat.getMax() / 2)) {
                    progSaturatedFat.setProgressDrawable(progSaturatedFat.getResources().getDrawable(R.drawable.yellowprogress));
                    nutritionScore = nutritionScore - 5;
                }
                else {
                    progSaturatedFat.setProgressDrawable(progSaturatedFat.getResources().getDrawable(R.drawable.greenprogress));
                    nutritionScore = nutritionScore + 7;
                }
                if (nutritionValues[3] > progCholesterol.getMax()) {
                    progCholesterol.setProgressDrawable(progCholesterol.getResources().getDrawable(R.drawable.redprogress));
                    nutritionScore = nutritionScore - 7;
                }
                else if (nutritionValues[3] < (progCholesterol.getMax() / 2)) {
                    progCholesterol.setProgressDrawable(progCholesterol.getResources().getDrawable(R.drawable.yellowprogress));
                    nutritionScore = nutritionScore - 5;
                }
                else {
                    progCholesterol.setProgressDrawable(progCholesterol.getResources().getDrawable(R.drawable.greenprogress));
                    nutritionScore = nutritionScore + 7;
                }
                if (nutritionValues[4] > progSodium.getMax()) {
                    progSodium.setProgressDrawable(progSodium.getResources().getDrawable(R.drawable.redprogress));
                    nutritionScore = nutritionScore - 7;
                }
                else if (nutritionValues[4] < (progSodium.getMax() / 2)) {
                    progSodium.setProgressDrawable(progSodium.getResources().getDrawable(R.drawable.yellowprogress));
                    nutritionScore = nutritionScore - 5;
                }
                else {
                    progSodium.setProgressDrawable(progSodium.getResources().getDrawable(R.drawable.greenprogress));
                    nutritionScore = nutritionScore + 7;
                }
                if (nutritionValues[5] > progCarbohydrate.getMax()) {
                    progCarbohydrate.setProgressDrawable(progCarbohydrate.getResources().getDrawable(R.drawable.redprogress));
                    nutritionScore = nutritionScore - 7;
                }
                else if (nutritionValues[5] < (progCarbohydrate.getMax() / 2)) {
                    progCarbohydrate.setProgressDrawable(progCarbohydrate.getResources().getDrawable(R.drawable.yellowprogress));
                    nutritionScore = nutritionScore - 5;
                }
                else {
                    progCarbohydrate.setProgressDrawable(progCarbohydrate.getResources().getDrawable(R.drawable.greenprogress));
                    nutritionScore = nutritionScore + 7;
                }
                if (nutritionValues[6] > progFiber.getMax()) {
                    progFiber.setProgressDrawable(progFiber.getResources().getDrawable(R.drawable.redprogress));
                    nutritionScore = nutritionScore - 7;
                }
                else if (nutritionValues[6] < (progFiber.getMax() / 2)) {
                    progFiber.setProgressDrawable(progFiber.getResources().getDrawable(R.drawable.yellowprogress));
                    nutritionScore = nutritionScore - 5;
                }
                else {
                    progFiber.setProgressDrawable(progFiber.getResources().getDrawable(R.drawable.greenprogress));
                    nutritionScore = nutritionScore + 7;
                }
//                if (nutritionValues[1] > progFat.getMax()) progFat.getProgressDrawable().setColorFilter(Color.RED, PorterDuff.Mode.SRC_IN);
//                if (nutritionValues[2] > progSaturatedFat.getMax()) progSaturatedFat.getProgressDrawable().setColorFilter(Color.RED, PorterDuff.Mode.SRC_IN);
//                if (nutritionValues[3] > progCholesterol.getMax()) progCholesterol.getProgressDrawable().setColorFilter(Color.RED, PorterDuff.Mode.SRC_IN);
//                if (nutritionValues[4] > progSodium.getMax()) progSodium.getProgressDrawable().setColorFilter(Color.RED, PorterDuff.Mode.SRC_IN);
//                if (nutritionValues[5] > progCarbohydrate.getMax()) progCarbohydrate.getProgressDrawable().setColorFilter(Color.RED, PorterDuff.Mode.SRC_IN);
//                if (nutritionValues[6] > progFiber.getMax()) progFiber.getProgressDrawable().setColorFilter(Color.RED, PorterDuff.Mode.SRC_IN);

                txtCalorie.setText(nutritionValues[0] + " / " + progCalorie.getMax() + " kcal");
                txtFat.setText(nutritionValues[1] + " / " + progFat.getMax() + " kcal");
                txtSaturatedFat.setText(nutritionValues[2] + " / " + progSaturatedFat.getMax() + " kcal");
                txtCholesterol.setText(nutritionValues[3] + " / " + progCholesterol.getMax() + " kcal");
                txtSodium.setText(nutritionValues[4] + " / " + progSodium.getMax() + " kcal");
                txtCarbohydrate.setText(nutritionValues[5] + " / " + progCarbohydrate.getMax() + " kcal");
                txtFiber.setText(nutritionValues[6] + " / " + progFiber.getMax() + " kcal");

                if (nutritionScore == 49) nutritionScore++;
                if (nutritionScore == -49) nutritionScore--;



            }

            txtNutritionXp.setText("Nutrition xp:\n" + nutritionScore + " / 50");
        }

    }
}
