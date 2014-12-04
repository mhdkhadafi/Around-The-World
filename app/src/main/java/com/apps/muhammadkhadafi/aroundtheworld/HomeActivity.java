package com.apps.muhammadkhadafi.aroundtheworld;

import android.app.Activity;
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
import android.widget.TextView;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;


public class HomeActivity extends Activity {

    private Button buttonSearch;
    private Button buttonNext;
    private Button buttonPrev;
    private EditText foodName;
    private TextView resultText;
    private TextView resultPage;
    private ExpandableListView expandableFoodList;
    private static String APPID = "0c780600";
    private static String APPKEY = "9ac341cc5667fde1fa36f683ac4ea15e";

    private int currentPage = 0;
    private int totalPage = 0;
    private int resultsPerPage = 5;

    SparseArray<Group> groups = new SparseArray<Group>();
    private MyExpandableListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_input);

        buttonSearch = (Button) findViewById(R.id.menu_search);
        buttonNext = (Button) findViewById(R.id.next_button);
        buttonPrev = (Button) findViewById(R.id.prev_button);
        resultPage = (TextView) findViewById(R.id.result_page);
        foodName = (EditText) findViewById(R.id.food_name_input);

        buttonNext.setVisibility(View.INVISIBLE);
        buttonNext.setActivated(false);
        buttonPrev.setVisibility(View.INVISIBLE);
        buttonPrev.setActivated(false);

        buttonSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new FoodAsync().execute(foodName.getText().toString(), "0", "start");
            }
        });
        buttonNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new FoodAsync().execute(foodName.getText().toString(), currentPage + "", "next");
            }
        });
        buttonPrev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new FoodAsync().execute(foodName.getText().toString(), currentPage + "", "prev");
            }
        });

//        createData();
        expandableFoodList = (ExpandableListView) findViewById(R.id.expandable_food_list);
        adapter = new MyExpandableListAdapter(this, groups);
        expandableFoodList.setAdapter(adapter);
        expandableFoodList.setVisibility(View.INVISIBLE);

        expandableFoodList.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener() {
            int previousGroup = -1;

            @Override
            public void onGroupExpand(int groupPosition) {
                if(groupPosition != previousGroup)
                    expandableFoodList.collapseGroup(previousGroup);
                previousGroup = groupPosition;

                expandableFoodList.setSelectedGroup(groupPosition);
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private class FoodAsync extends AsyncTask<String, Void, String[]> {

        @Override
        protected String[] doInBackground(String... params) {

            String result = "";

            try {
                if (params[2] == "prev") result = getFood(params[0], Integer.parseInt(params[1]) - 1);
                else result = getFood(params[0], Integer.parseInt(params[1]) + 1);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            return new String[] {result, params[1], params[2]};
        }

        public String getFood(String foodName, int page) throws UnsupportedEncodingException {
            // Create a new HttpClient and Post Header

            String searchURL = "https://api.nutritionix.com/v1_1/search/";
            String searchTerm = foodName;
            String fields = "*";
            Log.d("response", "page = " + page);
            int maxResult = resultsPerPage * page;
            int minResult = resultsPerPage * (page - 1)      ;
            int rangeResult = maxResult - minResult;
            String maxResultsString = minResult + ":" + maxResult;

            String resultReturns = "";

            String urlResult = searchURL + URLEncoder.encode(searchTerm, "UTF-8") + "?results=" +
                    URLEncoder.encode(maxResultsString, "UTF-8") + "&fields=" +
                    URLEncoder.encode(fields, "UTF-8") + "&appId=" + APPID + "&appKey=" + APPKEY;

            Log.d("response", urlResult);

//            String header = "&" + args + "&oauth_signature=" + URLEncoder.encode(signature, "UTF-8");
            HttpClient httpclient = new DefaultHttpClient();
            HttpGet httpGet = new HttpGet(urlResult);

            try {
                HttpResponse response = httpclient.execute(httpGet);
                String responseString = EntityUtils.toString(response.getEntity());
                Log.d("response", responseString);

                JSONObject jsonObject = new JSONObject(responseString);
                int totalHits = jsonObject.getInt("total_hits");

                totalPage = (int) Math.ceil((double)totalHits / (double)resultsPerPage);

                int loopNumber = 0;
                if (totalHits - minResult > rangeResult) loopNumber = rangeResult;
                else loopNumber = totalHits - minResult;

                JSONArray foodResults = jsonObject.getJSONArray("hits");

                String food_array[] = new String[loopNumber];
                String nutrition_array[] = new String[loopNumber];
                for (int i = 0; i < loopNumber; i++) {
                    food_array[i] = foodResults.getJSONObject(i).getJSONObject("fields").getString("item_name")
                            + "----" +foodResults.getJSONObject(i).getJSONObject("fields").getString("brand_name");
                    nutrition_array[i] = foodResults.getJSONObject(i).getJSONObject("fields").getString("nf_serving_size_qty")
                            + "----" +foodResults.getJSONObject(i).getJSONObject("fields").getString("nf_serving_size_unit")
                            + "----" +foodResults.getJSONObject(i).getJSONObject("fields").getString("nf_serving_weight_grams")
                            + "----" +foodResults.getJSONObject(i).getJSONObject("fields").getString("nf_calories")
                            + "----" +foodResults.getJSONObject(i).getJSONObject("fields").getString("nf_calories_from_fat")
                            + "----" +foodResults.getJSONObject(i).getJSONObject("fields").getString("nf_total_fat")
                            + "----" +foodResults.getJSONObject(i).getJSONObject("fields").getString("nf_saturated_fat")
                            + "----" +foodResults.getJSONObject(i).getJSONObject("fields").getString("nf_trans_fatty_acid")
                            + "----" +foodResults.getJSONObject(i).getJSONObject("fields").getString("nf_cholesterol")
                            + "----" +foodResults.getJSONObject(i).getJSONObject("fields").getString("nf_sodium")
                            + "----" +foodResults.getJSONObject(i).getJSONObject("fields").getString("nf_total_carbohydrate")
                            + "----" +foodResults.getJSONObject(i).getJSONObject("fields").getString("nf_dietary_fiber")
                            + "----" +foodResults.getJSONObject(i).getJSONObject("fields").getString("nf_sugars")
                            + "----" +foodResults.getJSONObject(i).getJSONObject("fields").getString("nf_protein")
                            + "----" +foodResults.getJSONObject(i).getJSONObject("fields").getString("nf_vitamin_a_dv")
                            + "----" +foodResults.getJSONObject(i).getJSONObject("fields").getString("nf_vitamin_c_dv")
                            + "----" +foodResults.getJSONObject(i).getJSONObject("fields").getString("nf_calcium_dv")
                            + "----" +foodResults.getJSONObject(i).getJSONObject("fields").getString("nf_iron_dv");

                }

                String foodResult = "";
                for (int i = 0; i < food_array.length; i++) {
                    if (i != 0) foodResult = foodResult + "\n";
//                    Log.d("response", food_array[i]);
                    foodResult = foodResult + food_array[i];
                }

                String nutritionResult = "";
                for (int i = 0; i < nutrition_array.length; i++) {
                    if (i != 0) nutritionResult = nutritionResult + "\n";
//                    Log.d("response", food_array[i]);
                    nutritionResult = nutritionResult + nutrition_array[i];
                }

                resultReturns = foodResult + "--nutrition--" + nutritionResult;

                return resultReturns;


            } catch (Exception e) {
                e.printStackTrace();
                Log.d("Error", "Cannot Estabilish Connection");

                return null;
            }
        }

        @Override
        protected void onPostExecute(String[] result) {
            super.onPostExecute(result);

            // TextView resultText = (TextView) findViewById(R.id.result_text);

            // show result in textView
            if (result == null) {
//                resultText.setText("Error in downloading. Please try again.");
            } else {
                buttonNext.setVisibility(View.VISIBLE);
                if (result[2] == "prev") currentPage = Integer.parseInt(result[1]) - 1;
                else currentPage = Integer.parseInt(result[1]) + 1;
                resultPage.setText(currentPage + "/" + totalPage);

                expandableFoodList.setVisibility(View.VISIBLE);
                expandableFoodList.invalidateViews();
                expandableFoodList.scrollBy(0, 0);

//                Log.d("response", result[0].split("\\n").length + "");
                createData(result[0].split("--nutrition--")[0].split("\\n"), result[0].split("--nutrition--")[1].split("\\n"));
                if (totalPage == currentPage) {buttonNext.setVisibility(View.INVISIBLE); buttonNext.setActivated(true);}
                else {buttonNext.setVisibility(View.VISIBLE); buttonNext.setActivated(false);}

                if (currentPage > 1) {buttonPrev.setVisibility(View.VISIBLE); buttonPrev.setActivated(true);}
                else {buttonPrev.setVisibility(View.INVISIBLE); buttonPrev.setActivated(false);}
            }
        }
    }

    public void createData(String[] foods, String[] nutrition) {
        for (int j = 0; j < foods.length; j++) {
            String[] splitFoodBrand = foods[j].split("----");
            Group group = new Group(splitFoodBrand[0], splitFoodBrand[1]);
            group.children.add(nutritionString(nutrition[j].split("----")));
            groups.append(j, group);
        }

        if (foods.length < resultsPerPage) {
            for (int k = foods.length; k < resultsPerPage; k++) {
                groups.remove(k);
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
