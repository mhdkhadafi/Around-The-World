package com.apps.muhammadkhadafi.aroundtheworld;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;

/**
 * Created by muhammadkhadafi on 12/6/14.
 */
public class ActivityRecipeView extends Activity {

    WebView recipeView;
    Button btnBack;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_detail);

        btnBack = (Button) findViewById(R.id.btn_back);
        recipeView = (WebView) findViewById(R.id.web_recipe);

        Bundle b = getIntent().getExtras();
        String recipeUrl = b.getString("recipeurl");
        final String currentCity = b.getString("currentcity");

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), FoodRecommendationActivity.class);
                Bundle b = new Bundle();
                b.putString("currentcity", currentCity); //Your id\
                intent.putExtras(b); //Put your id to your next Intent
                startActivity(intent);
                finish();
            }
        });

        // TODO Webview not working
        recipeView.loadUrl(recipeUrl);
    }
}
