package com.apps.muhammadkhadafi.aroundtheworld;

import android.app.Activity;
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
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

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

    Button btnFoodRec;
    Button btnAddFood;
    Button btnFoodJournal;
    Button btnDailyNutrition;
    Button btnSignOut;
    ProgressBar progXp;
    TextView tvCountry;
    TextView tvXp;
    SharedPreferences preferences;

    String[] cityList = new String[]{"New York, USA", "London, UK", "Stockholm, Sweden",
            "Paris, France", "Rome, Italy", "Cairo, Egypt", "Victoria Falls, Zimbabwe",
            "Tehran, Iran", "New Delhi, India", "Beijing, China", "Seoul, South Korea",
            "Tokyo, Japan", "Bangkok, Thailand", "Bali, Indonesia", "Sydney, Australia",
            "Apia, Samoa", "Buenos Aires, Argentina", "Rio de Janeiro, Brazil",
            "Mexico City, Mexico", "San Francisco, USA"};


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        preferences = getApplicationContext().getSharedPreferences("MyPrefs", 0);

        int currentXp = preferences.getInt("xp", 0);

        btnFoodRec = (Button) findViewById(R.id.btn_recommendfood);
        btnAddFood = (Button) findViewById(R.id.btn_inputfood);
        btnFoodJournal = (Button) findViewById(R.id.btn_foodjournal);
        btnDailyNutrition = (Button) findViewById(R.id.btn_nutritiondata);
        btnSignOut = (Button) findViewById(R.id.btn_signout);
        progXp = (ProgressBar) findViewById(R.id.prog_countryprogression);
        tvCountry = (TextView) findViewById(R.id.txt_country);
        tvXp = (TextView) findViewById(R.id.txt_currentxp);

        tvCountry.setText(cityList[currentXp / 100]);
        progXp.setMax(100);
        progXp.setProgress(currentXp % 100);
        tvXp.setText(currentXp + " / 100 xp");

        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("currentcity", tvCountry.getText().toString());
        editor.putString("id", "54504c40febf73080027af93");
        editor.putString("username", "daffi");
        editor.commit();

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


    }
}