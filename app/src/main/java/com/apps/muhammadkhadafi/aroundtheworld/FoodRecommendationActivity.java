package com.apps.muhammadkhadafi.aroundtheworld;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Random;

/**
 * Created by muhammadkhadafi on 12/6/14.
 */
public class FoodRecommendationActivity extends Activity {

    SparseArray<Group> groups = new SparseArray<Group>();
    private RecListAdapter adapter;
    ExpandableListView expandableRecList;
    TextView txtCityFood;
    private int resultsPerPage = 5;
    private Button btnMoreRec;
    private Button btnGetRecipe;
    private Button btnBack;

    private final String APP_KEY = "eccace2cc3502d825d841e2c403d7856";
    private String[] foodLinks;

    private String recipeUrl = "";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recommend);

        btnGetRecipe = (Button) findViewById(R.id.btn_getrecipe);
        btnMoreRec = (Button) findViewById(R.id.btn_morerec);
        btnBack = (Button) findViewById(R.id.btn_back);
        expandableRecList = (ExpandableListView) findViewById(R.id.elv_foodrecommendation);
        adapter = new RecListAdapter(this, groups);
        expandableRecList.setAdapter(adapter);

        expandableRecList.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener() {
            int previousGroup = -1;
            @Override
            public void onGroupExpand(int groupPosition) {if(groupPosition != previousGroup)
                expandableRecList.collapseGroup(previousGroup);
                previousGroup = groupPosition;

                recipeUrl = foodLinks[groupPosition];
            }
        });

        expandableRecList.setOnGroupCollapseListener(new ExpandableListView.OnGroupCollapseListener()
        {

            @Override
            public void onGroupCollapse(int groupPosition) {
                recipeUrl = "";
            }
        });

        txtCityFood = (TextView) findViewById(R.id.txt_cityfood);

        Bundle b = getIntent().getExtras();
        txtCityFood.setText(b.getString("currentcity"));

        foodLinks = new String[resultsPerPage];

        new RecAsync().execute(getLocalFoodName(txtCityFood.getText().toString()));

        btnMoreRec.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new RecAsync().execute(getLocalFoodName(txtCityFood.getText().toString()));
            }
        });
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(),HomeActivity.class);
                startActivity(i);
            }
        });
        btnGetRecipe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!recipeUrl.equals("")) {
                    Intent intent = new Intent(getApplicationContext(), ActivityRecipeView.class);
                    Bundle b = new Bundle();
                    b.putString("currentcity", txtCityFood.getText().toString()); //Your id\
                    b.putString("recipeUrl", recipeUrl);
                    intent.putExtras(b); //Put your id to your next Intent
                    startActivity(intent);
                    finish();
                }
                else Toast.makeText(getApplicationContext(), "Select food first", Toast.LENGTH_SHORT).show();
            }
        });
//        Group group = new Group("Karelian Borscht", "Finland");
//        group.children.add("Nutrition Karelian Borscht");
//        group.bitmapUrl = "https://d2k9njawademcf.cloudfront.net/indeximages/30004/full/_TJH7053_1.jpg?1403272429";
//        groups.append(0, group);
//
//        group = new Group("Kedgeree", "UK");
//        group.children.add("Nutrition Kedgeree");
//        group.bitmapUrl = "http://www.drdobbin.co.uk/sites/all/sites/drdobbin.co.uk/files/image/kedgeree.jpg";
//        groups.append(1, group);
    }

    private String getLocalFoodName(String locality) {

        String[] localFoods = new String[5];
        if (locality.equals("New York, USA")) {
            localFoods = new String[]{"manhattan clam chowder", "knish", "waldorf salad",
                    "new york strip", "eggs benedict"};
        }
        else if (locality.equals("London, UK")) {
            localFoods = new String[]{"welsh rarebit", "beef wellington", "kedgeree", "kidney pie",
                    "lancashire hotpot"};
        }
        else if (locality.equals("Stockholm, Sweden")) {
            localFoods = new String[]{"salmon", "swedish meatballs", "danish meatballs",
                    "norwegian fish", "mojakka"};
        }
        else {

        }
        Random random = new Random();
        return localFoods[random.nextInt(5)];
    }
    private class RecAsync extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {

            String result = "";

            try {
                result = getFood(params[0]);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            return result;
        }

        public String getFood(String foodName) throws UnsupportedEncodingException {
            // Create a new HttpClient and Post Header

            String searchURL = "http://food2fork.com/api/search";
            String searchTerm = foodName;
            String resultReturns = "";

            String urlResult = searchURL + "?key=" + APP_KEY + "&q=" + URLEncoder.encode(searchTerm, "UTF-8");

            Log.d("response", urlResult);

            HttpClient httpclient = new DefaultHttpClient();
            HttpGet httpGet = new HttpGet(urlResult);

            try {
                HttpResponse response = httpclient.execute(httpGet);
                String responseString = EntityUtils.toString(response.getEntity());
                Log.d("response", responseString);

                JSONObject jsonObject = new JSONObject(responseString);
                int totalHits = jsonObject.getInt("count");
                int loopNumber = 0;
                if (totalHits < resultsPerPage) loopNumber = totalHits;
                else loopNumber = resultsPerPage;


                JSONArray foodResults = jsonObject.getJSONArray("recipes");
                String food_array[] = new String[loopNumber];
                String image_url_array[] = new String[loopNumber];
                for (int i = 0; i < loopNumber; i++) {
                    food_array[i] = foodResults.getJSONObject(i).getString("title")
                            + "----" +foodResults.getJSONObject(i).getString("publisher");
                    foodLinks[i] = foodResults.getJSONObject(i).getString("source_url");
                    image_url_array[i] = foodResults.getJSONObject(i).getString("image_url");
                }

                String foodResult = "";
                String imageUrlResult = "";
                for (int i = 0; i < food_array.length; i++) {
                    if (i != 0) {foodResult = foodResult + "\n"; imageUrlResult = imageUrlResult + "\n";}
//                    Log.d("response", food_array[i]);
                    foodResult = foodResult + food_array[i];
                    imageUrlResult = imageUrlResult + image_url_array[i];
                }

                resultReturns = foodResult + "--url--" + imageUrlResult;

                return resultReturns;


            } catch (Exception e) {
                e.printStackTrace();
                Log.d("Error", "Cannot Estabilish Connection");

                return null;
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

                expandableRecList.setVisibility(View.VISIBLE);
                expandableRecList.invalidateViews();
                expandableRecList.scrollBy(0, 0);

                String[] foods = result.split("--url--")[0].split("\\n");
                String[] urls = result.split("--url--")[1].split("\\n");

                for (int j = 0; j < foods.length; j++) {
                    String[] splitFoodBrand = foods[j].split("----");
                    Group group = new Group(splitFoodBrand[0], splitFoodBrand[1]);
                    group.children.add("");
                    group.bitmapUrl = urls[j];
                    groups.append(j, group);

                }

                if (foods.length < resultsPerPage) {
                    for (int k = foods.length; k < resultsPerPage; k++) {
                        groups.remove(k);
                    }
                }
            }
        }
    }

}
