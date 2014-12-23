package com.apps.muhammadkhadafi.aroundtheworld;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;


public class HomeActivity extends Activity {

    Button btnFoodRec;
    Button btnAddFood;
    Button btnFoodJournal;
    Button btnDailyNutrition;
    Button btnSignOut;
    ProgressBar progXp;
    TextView tvCountry;
    TextView tvXp;
    SharedPreferences preferences;
    ImageButton imgMap;
    String sumbittedFood = "";
    int currentXp;

    String[] cityList = new String[]{"New York, USA", "London, UK", "Stockholm, Sweden",
            "Paris, France", "Rome, Italy", "Cairo, Egypt", "Victoria Falls, Zimbabwe",
            "Tehran, Iran", "New Delhi, India", "Beijing, China", "Seoul, South Korea",
            "Tokyo, Japan", "Bangkok, Thailand", "Bali, Indonesia", "Sydney, Australia",
            "Apia, Samoa", "Buenos Aires, Argentina", "Rio de Janeiro, Brazil",
            "Mexico City, Mexico", "San Francisco, USA"};

    String[] localFoods = new String[0];


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        Calendar calendar = Calendar.getInstance();
        int dayOfYear = calendar.get(Calendar.DAY_OF_YEAR);

        preferences = getApplicationContext().getSharedPreferences("MyPrefs", 0);
//        SharedPreferences.Editor editor1 = preferences.edit();
//        editor1.putInt("xp", 0);
//        editor1.commit();

        currentXp = preferences.getInt("xp", 0);
        if (dayOfYear > preferences.getInt("daylastopened", dayOfYear)) new NutritionScoreAsync().execute();



        btnFoodRec = (Button) findViewById(R.id.btn_recommendfood);
        btnAddFood = (Button) findViewById(R.id.btn_inputfood);
        btnFoodJournal = (Button) findViewById(R.id.btn_foodjournal);
        btnDailyNutrition = (Button) findViewById(R.id.btn_nutritiondata);
        btnSignOut = (Button) findViewById(R.id.btn_signout);
        progXp = (ProgressBar) findViewById(R.id.prog_countryprogression);
        tvCountry = (TextView) findViewById(R.id.txt_country);
        tvXp = (TextView) findViewById(R.id.txt_currentxp);
        imgMap = (ImageButton) findViewById(R.id.ibtn_worldmap);





        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("currentcity", tvCountry.getText().toString());
        // TODO delete when publish
        editor.putString("id", "54504c40febf73080027af93");
        editor.putString("username", "daffi");
        editor.putInt("daylastopened", dayOfYear);
        editor.commit();

//        new GetXpAsync().execute();

        tvCountry.setText(cityList[currentXp / 100]);
        progXp.setMax(100);
        progXp.setProgress(currentXp % 100);
        tvXp.setText(currentXp + " / " + (100*((currentXp / 100) + 1)) + " xp");

        tvCountry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new NutritionScoreAsync().execute();
            }
        });
        tvXp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // debug add xp manually
//                new AddXpAsync().execute(20);
                int newXp = currentXp + 20;

                if (newXp / 100 > currentXp / 100) {

                    tvCountry.setText(cityList[newXp / 100]);
                    progXp.setMax(100);
                    progXp.setProgress(newXp % 100);
                    tvXp.setText(newXp + " / " + (100*((newXp / 100) + 1)) + " xp");

                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(HomeActivity.this);

                    String message = "You've progressed to " + tvCountry.getText().toString();
                    String title = "Congratulations";
                    String buttonString = "WooHoo!";

                    alertDialogBuilder.setTitle(title);
                    alertDialogBuilder.setMessage(message);

                    alertDialogBuilder.setNeutralButton(buttonString, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // exit the app and go to the HOME
                            dialog.cancel();
                        }
                    });

                    AlertDialog alertDialog = alertDialogBuilder.create();
                    // show alert
                    alertDialog.show();
                }


                tvCountry.setText(cityList[currentXp / 100]);
                progXp.setMax(100);
                progXp.setProgress(currentXp % 100);
                tvXp.setText(currentXp + " / " + (100*((currentXp / 100) + 1)) + " xp");

                currentXp = newXp;
                SharedPreferences.Editor editor = preferences.edit();
                editor.putInt("xp", currentXp);
                editor.commit();

            }
        });

        btnFoodRec.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), FoodRecommendationActivity.class);
                Bundle b = new Bundle();
                b.putString("currentcity", tvCountry.getText().toString()); //Your id\
                intent.putExtras(b); //Put your id to your next Intent
                startActivity(intent);
                finish();
            }
        });
        btnAddFood.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(),MealAddActivity.class);
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
        btnDailyNutrition.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(),DailyNutritionActivity.class);
                startActivity(i);
            }
        });
        btnSignOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString("id", "");
                editor.putString("username", "");
                editor.commit();

                Intent i = new Intent(getApplicationContext(),LoginActivity.class);
                startActivity(i);
            }
        });
        imgMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), CountryDetailActivity.class);
                Bundle b = new Bundle();
                b.putString("currentcity", tvCountry.getText().toString()); //Your id\
                intent.putExtras(b); //Put your id to your next Intent
                startActivity(intent);
                finish();
            }
        });


        Bundle b = getIntent().getExtras();
        if (b != null) {
            sumbittedFood = b.getString("submittedfood", "");

            new GetCountryFoodsAsync().execute(tvCountry.getText().toString());
        }
    }

    private boolean checkLocalFood(String food, String[] foods) {
        boolean result = false;

        for (int i = 0; i < foods.length; i++) {
            if (foods[i].toLowerCase().contains(food.toLowerCase()) || food.toLowerCase().contains(foods[i].toLowerCase())) result = true;
            Log.d("response", food + "-" + foods[i]);
        }

        return result;
    }


    private class GetCountryFoodsAsync extends AsyncTask<String, Void, String[]> {

        @Override
        protected String[] doInBackground(String... params) {

            String[] result = new String[0];

            try {
                result = getCountryDetails(params[0]);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            return result;
        }

        public String[] getCountryDetails(String city) throws UnsupportedEncodingException {
            // Create a new HttpClient and Post Header

            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost("http://shielded-taiga-6664.herokuapp.com/articles/getcountryfood");

            String[] allFoods = new String[5];

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
                allFoods[0] = jsonObject.getJSONObject("city").getJSONObject("foods").getString("one");
                allFoods[1] = jsonObject.getJSONObject("city").getJSONObject("foods").getString("two");
                allFoods[2] = jsonObject.getJSONObject("city").getJSONObject("foods").getString("three");
                allFoods[3] = jsonObject.getJSONObject("city").getJSONObject("foods").getString("four");
                allFoods[4] = jsonObject.getJSONObject("city").getJSONObject("foods").getString("five");

            } catch (Exception e) {
                e.printStackTrace();
            }

            return allFoods;
        }

        @Override
        protected void onPostExecute(String[] result) {
            super.onPostExecute(result);

            // TextView resultText = (TextView) findViewById(R.id.result_text);

            // show result in textView
            if (result.length == 0) {
                // TODO : Do something to handle no results
            } else {
                localFoods = result;

                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(HomeActivity.this);

                String message = "You just submitted your meal!\nYou get 20 xp";
                if (checkLocalFood(sumbittedFood, localFoods)) {
                    message = message + "\n\n+20 more xp because " + sumbittedFood
                            + " is local food from " + tvCountry.getText().toString();
                }
                alertDialogBuilder.setTitle("Congratulations");
                alertDialogBuilder.setMessage(message);

                alertDialogBuilder.setNeutralButton("Awesome", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // exit the app and go to the HOME
                        dialog.cancel();
                    }
                });

                AlertDialog alertDialog = alertDialogBuilder.create();
                // show alert
                alertDialog.show();
            }
        }

    }

    private class NutritionScoreAsync extends AsyncTask<String, Void, String[]> {


        protected String[] doInBackground(String... params) {

            SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPrefs", 0);
            String userid = pref.getString("id", "");
            String username = pref.getString("username", "");

            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost("http://shielded-taiga-6664.herokuapp.com/articles/date");

            String[] foodNutritionArray = new String[0];

            try {
                // Add your data

                Date d = new Date(System.currentTimeMillis() - (1000 * 60 * 60 * 24));
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
            int nutritionScore = 0;
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

                if (nutritionValues[0] > 2000) {
                    nutritionScore = nutritionScore - 7;
                }
                else if (nutritionValues[0] < (2000 / 2)) {
                    nutritionScore = nutritionScore - 5;
                }
                else {
                    nutritionScore = nutritionScore + 7;
                }
                if (nutritionValues[1] > 65) {
                    nutritionScore = nutritionScore - 7;
                }
                else if (nutritionValues[1] < (65 / 2)) {
                    nutritionScore = nutritionScore - 5;
                }
                else {
                    nutritionScore = nutritionScore + 7;
                }
                if (nutritionValues[2] > 20) {
                    nutritionScore = nutritionScore - 7;
                }
                else if (nutritionValues[2] < (20 / 2)) {
                    nutritionScore = nutritionScore - 5;
                }
                else {
                    nutritionScore = nutritionScore + 7;
                }
                if (nutritionValues[3] > 300) {
                    nutritionScore = nutritionScore - 7;
                }
                else if (nutritionValues[3] < (300 / 2)) {
                    nutritionScore = nutritionScore - 5;
                }
                else {
                    nutritionScore = nutritionScore + 7;
                }
                if (nutritionValues[4] > 2400) {
                    nutritionScore = nutritionScore - 7;
                }
                else if (nutritionValues[4] < (2400 / 2)) {
                    nutritionScore = nutritionScore - 5;
                }
                else {
                    nutritionScore = nutritionScore + 7;
                }
                if (nutritionValues[5] > 300) {
                    nutritionScore = nutritionScore - 7;
                }
                else if (nutritionValues[5] < (300 / 2)) {
                    nutritionScore = nutritionScore - 5;
                }
                else {
                    nutritionScore = nutritionScore + 7;
                }
                if (nutritionValues[6] > 25) {
                    nutritionScore = nutritionScore - 7;
                }
                else if (nutritionValues[6] < (25 / 2)) {
                    nutritionScore = nutritionScore - 5;
                }
                else {
                    nutritionScore = nutritionScore + 7;
                }

                if (nutritionScore == 49) nutritionScore++;
                if (nutritionScore == -49) nutritionScore--;



            }

            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(HomeActivity.this);

            String message = "You received your nutrition points from yesterday!\nYou received " + nutritionScore + " out of 50 xp points";
            String title = "";
            String buttonString = "";
            if (nutritionScore < 0) {
                message = message + "\n\nYou need to eat healthier";
                title = "Sorry";
                buttonString = "I'll do better!";
            }
            else {
                message = message + "\n\nKeep eating healthy!";
                title = "Congratulations";
                buttonString = "Yeah!";
            }
            alertDialogBuilder.setTitle(title);
            alertDialogBuilder.setMessage(message);

            alertDialogBuilder.setNeutralButton(buttonString, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    // exit the app and go to the HOME
                    dialog.cancel();
                }
            });

            AlertDialog alertDialog = alertDialogBuilder.create();
            // show alert
            alertDialog.show();
        }

    }

    private class GetXpAsync extends AsyncTask<String, Void, Integer> {

        protected Integer doInBackground(String... params) {

            SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPrefs", 0);
            String username = pref.getString("username", "");
            String userid = pref.getString("id", "");

            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost("http://shielded-taiga-6664.herokuapp.com/articles/allxp");

            int newxp = 0;

            try {
                // Add your data
                Log.d("response", username + "user");

                JSONObject user = new JSONObject();
                try {
                    user.put("username", username);
                    user.put("user", userid);
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
                newxp = jsonObject.getInt("xp");

            } catch(Exception e) {
                e.printStackTrace();
                Log.d("Error", "Cannot Estabilish Connection");
            }

            return newxp;
        }

        @Override
        protected void onPostExecute(Integer result) {
            super.onPostExecute(result);

            Log.d("response", result + "");

            if (currentXp / 100 < result / 100) {

                currentXp = result;

                tvCountry.setText(cityList[currentXp / 100]);
                progXp.setMax(100);
                progXp.setProgress(currentXp % 100);
                tvXp.setText(currentXp + " / " + (100*((currentXp / 100) + 1)) + " xp");

                new GetCountryDetailsAsync().execute(tvCountry.getText().toString());
            }

            currentXp = result;

            tvCountry.setText(cityList[currentXp / 100]);
            progXp.setMax(100);
            progXp.setProgress(currentXp % 100);
            tvXp.setText(currentXp + " / " + (100*((currentXp / 100) + 1)) + " xp");


            SharedPreferences.Editor editor = preferences.edit();
            editor.putInt("xp", currentXp);
        }

    }

    private class AddXpAsync extends AsyncTask<Integer, Void, String> {

        protected String doInBackground(Integer... params) {

            SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPrefs", 0);
            String username = pref.getString("username", "");
            String userid = pref.getString("id", "");

            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost("http://shielded-taiga-6664.herokuapp.com/articles/allxp");


            try {
                // Add your data
                Log.d("response", username + "user");

                JSONObject user = new JSONObject();
                try {
                    user.put("username", username);
                    user.put("user", userid);
                    user.put("points", params[0]);
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


            } catch(Exception e) {
                e.printStackTrace();
                Log.d("Error", "Cannot Estabilish Connection");
            }

            return "";

        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            new GetXpAsync().execute();

            Log.d("response", result + "");

        }

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
                String story =  jsonObject.getJSONObject("city").getString("story");

                return story;
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

                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(HomeActivity.this);

                String message = "You've progressed to " + tvCountry.getText().toString() + "\n\n" + result;
                String title = "Congratulations";
                String buttonString = "WooHoo!";

                alertDialogBuilder.setTitle(title);
                alertDialogBuilder.setMessage(message);

                alertDialogBuilder.setNeutralButton(buttonString, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // exit the app and go to the HOME
                        dialog.cancel();
                    }
                });

                AlertDialog alertDialog = alertDialogBuilder.create();
                // show alert
                alertDialog.show();

            }
        }
    }
}