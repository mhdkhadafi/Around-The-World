package com.apps.muhammadkhadafi.aroundtheworld;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.widget.Button;
import android.widget.ExpandableListView;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
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

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;

/**
 * Created by muhammadkhadafi on 12/6/14.
 */
public class DailyJournalActivity extends Activity {

    ExpandableListView expandableFoodList;
    SparseArray<Group> groups = new SparseArray<Group>();
    private FoodListAdapter adapter;
    Button btnBack;
    String selectedID = "";
    int day;
    int month;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_detail);

        Bundle b = getIntent().getExtras();
        day = b.getInt("day");
        month = b.getInt("month");

        Log.d("response", day+"-"+month);

        btnBack = (Button) findViewById(R.id.btn_back);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), FoodJournalActivity.class);
                startActivity(intent);
            }
        });

        expandableFoodList = (ExpandableListView) findViewById(R.id.elv_dailyfood);
        adapter = new FoodListAdapter(this, groups);
        expandableFoodList.setAdapter(adapter);
        expandableFoodList.setVisibility(View.INVISIBLE);

        expandableFoodList.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener() {
            int previousGroup = -1;

            @Override
            public void onGroupExpand(int groupPosition) {
                if(groupPosition != previousGroup)
                    expandableFoodList.collapseGroup(previousGroup);
                previousGroup = groupPosition;

                selectedID = "" + groupPosition;
                expandableFoodList.setSelectedGroup(groupPosition);
            }
        });

        expandableFoodList.setOnGroupCollapseListener(new ExpandableListView.OnGroupCollapseListener()
        {
            @Override
            public void onGroupCollapse(int groupPosition) {
                selectedID = "";
            }
        });

        SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPrefs", 0);
        String userid = pref.getString("id", "");
        String username = pref.getString("username", "");

        new FoodAsync().execute(userid, username, (month+1) + "", day + "");
    }

    private class FoodAsync extends AsyncTask<String, Void, String[]> {

        @Override
        protected String[] doInBackground(String... params) {

            String[] result = new String[0];

            try {
                result = getFood(params[0], params[1], params[2], params[3]);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            return result;
        }

        public String[] getFood(String userid, String username, String month, String date) throws UnsupportedEncodingException {
            // Create a new HttpClient and Post Header

            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost("http://shielded-taiga-6664.herokuapp.com/articles/date");
            String[] foodAttibutesArray = new String[0];

            try {

                JSONObject user = new JSONObject();
                try {
                    user.put("user", userid);
                    user.put("username", username);
                    user.put("month", month);
                    user.put("day", date);
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
                foodAttibutesArray = new String[jsonArray.length()];

                Log.d("respinse", "jsonlength" + jsonArray.length());

                for (int i = 0; i < jsonArray.length(); i++) {
                    String foodName = jsonArray.getJSONObject(i).getString("title");
                    String pictureUrl = jsonArray.getJSONObject(i).getString("content");
                    String pictureUrlThumb = jsonArray.getJSONObject(i).getString("thumbnail");
                    String dateTimeSubmitted = jsonArray.getJSONObject(i).getString("created");
                    nutritions = jsonArray.getJSONObject(i).getString("nutrition");
                    JSONObject nutritionJson = new JSONObject(nutritions);

                    foodNutrition = foodNutrition + nutritionJson.getString("serving_amt") + "----";
                    foodNutrition = foodNutrition + nutritionJson.getString("serving_type") + "----";
                    foodNutrition = foodNutrition + nutritionJson.getString("serving_size") + "----";
                    foodNutrition = foodNutrition + nutritionJson.getString("calorie") + "----";
                    foodNutrition = foodNutrition + nutritionJson.getString("calorie_from_fat") + "----";
                    foodNutrition = foodNutrition + nutritionJson.getString("total_fat") + "----";
                    foodNutrition = foodNutrition + nutritionJson.getString("saturated_fat") + "----";
                    foodNutrition = foodNutrition + nutritionJson.getString("trans_fat") + "----";
                    foodNutrition = foodNutrition + nutritionJson.getString("cholesterol") + "----";
                    foodNutrition = foodNutrition + nutritionJson.getString("sodium") + "----";
                    foodNutrition = foodNutrition + nutritionJson.getString("total_carbohydrate") + "----";
                    foodNutrition = foodNutrition + nutritionJson.getString("dietary_fiber") + "----";
                    foodNutrition = foodNutrition + nutritionJson.getString("sugars") + "----";
                    foodNutrition = foodNutrition + nutritionJson.getString("protein") + "----";
                    foodNutrition = foodNutrition + nutritionJson.getString("vit_a") + "----";
                    foodNutrition = foodNutrition + nutritionJson.getString("vit_c") + "----";
                    foodNutrition = foodNutrition + nutritionJson.getString("calcium") + "----";
                    foodNutrition = foodNutrition + nutritionJson.getString("iron") + "----";

                    foodAttibutesArray[i] = foodName + "--att--" + pictureUrl + "--att--" +
                            dateTimeSubmitted + "--att--" + pictureUrlThumb + "--att--" + foodNutrition;
                    foodNutrition = "";
                }

            } catch (Exception e) {
                e.printStackTrace();
//                foodAttibutesArray = new String[0];
            }

            return foodAttibutesArray;
        }

        @Override
        protected void onPostExecute(String[] result) {
            super.onPostExecute(result);

            // TextView resultText = (TextView) findViewById(R.id.result_text);

            // show result in textView
            if (result.length == 0){
                // TODO : Do something to handle no results
            } else {


                expandableFoodList.setVisibility(View.VISIBLE);
                expandableFoodList.invalidateViews();
                expandableFoodList.scrollBy(0, 0);

                for (int j = 0; j < result.length; j++) {
                    Log.d("response", result[j]);
                    String[] splitFoodAttributes = result[j].split("--att--");
                    Group group = new Group(splitFoodAttributes[0], "");
                    group.bitmapUrlFull = splitFoodAttributes[1];
                    group.bitmapUrl = splitFoodAttributes[3];

                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss'.000Z'");
                    SimpleDateFormat sdf1 = new SimpleDateFormat("EEEE, MMMM dd yyyy");
                    SimpleDateFormat sdf2 = new SimpleDateFormat("hh:mm:ss");
                    try {
                        group.dateConsumed = sdf1.format(sdf.parse(splitFoodAttributes[2]));
                        group.timeConsumed = sdf2.format(sdf.parse(splitFoodAttributes[2]));
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

                    group.children.add(nutritionString(splitFoodAttributes[4].split("----")));
                    groups.append(j, group);
                }
            }
        }
    }

    public String nutritionString(String[] n) {

        String returnedNutritionString = "Per serving size of " + n[0] + " " + n[1]
                + " (" + n[2] + "g)\nCalories: " + n[3] + "kcal\n   Calories from Fat: " + n[4]
                + "kcal\nTotal Fat: " + n[5] + "g\n   Saturated Fat: " + n[6] + "g\n   Trans Fat: "
                + n[7] + "g\nCholesterol: " + n[8] + "mg\nSodium: " + n[9] + "mg\nTotal Carbohydrate: "
                + n[10] + "g\n   Dietary Fiber: " + n[11] + "g\n   Sugars: " + n[12] + "g\nProtein: "
                + n[13] + "g\nVitamin A: " + n[14] + "%\nVitamin C: " + n[15] + "%\nCalcium: " + n[16]
                + "%\nIron: " + n[17] + "%\n*based on a 2000 calorie diet";

        returnedNutritionString = returnedNutritionString.replaceAll("null", "-");
        return returnedNutritionString;
    }

}
