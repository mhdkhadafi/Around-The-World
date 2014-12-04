package com.apps.muhammadkhadafi.aroundtheworld;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mhd.khadafi on 10/24/2014.
 */
public class ArticleActivity extends Activity {

    EditText user_name;
    EditText article_title;
    EditText article_content;
    Button submit_button;
    Button login_button;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_article);

        user_name = (EditText) findViewById(R.id.user_name_article);
        article_title = (EditText) findViewById(R.id.article_title);
        article_content = (EditText) findViewById(R.id.article_content);
        submit_button = (Button) findViewById(R.id.submit_article);
        login_button = (Button) findViewById(R.id.login);

        article_content.setText("https://s3.amazonaws.com/my-unique-nameakiajn46spjhkwzxslvabucketone/Test2");

        //new MyAsyncTask().execute("", "", "login");

        login_button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                new MyAsyncTask().execute("", "", "login");
                // Do something in response to button click
            }
        });

        submit_button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                new MyAsyncTask().execute(article_title.getText() + "", article_content.getText() + "", "not login");
                // Do something in response to button click
            }
        });
    }

    private class MyAsyncTask extends AsyncTask<String, Void, String> {

        SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPrefs", 0);
        String responseText;

        @Override
        protected String doInBackground(String... params) {
            if (params[2] == "login") {
                login();
            }
            else {
                postData(params[0], params[1]);
            }

            return responseText;
        }

        public void login() {
            // Create a new HttpClient and Post Header
            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost("http://test-mean-heroku.herokuapp.com/login");

            try {
                // Add your data

                JSONObject user = new JSONObject();
                try {
                    user.put("email", "mk2333@cornell.edu");
                    user.put("password", "mhdkhadafi");
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                //JSONArray jsonArray = new JSONArray();
                //jsonArray.put(user);

                //JSONObject user_list = new JSONObject();
                //try {
                //    user_list.put("user", jsonArray);
                //} catch (JSONException e) {
                //    e.printStackTrace();
                //}

                StringEntity se = new StringEntity(user.toString());
                se.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));

                List nameValuePairs = new ArrayList();
                httppost.setEntity(se);

                // Execute HTTP Post Request
                ResponseHandler responseHandler = new BasicResponseHandler();
                HttpResponse response = httpclient.execute(httppost);

                //This is the response from a php application
                //responseText = response.toString();
                //String responseID = responseText.substring(responseText.indexOf("\"id\":") + 5,
                //        responseText.indexOf("}"));

                Log.d("response", EntityUtils.toString(response.getEntity()));

            } catch(Exception e) {
                e.printStackTrace();
                Log.d("Error", "Cannot Estabilish Connection");
            }
        }

        public void postData(String article_title_send, String article_content_send) {
            // Create a new HttpClient and Post Header
            HttpClient httpclient1 = new DefaultHttpClient();
            HttpPost httppost1 = new HttpPost("http://test-mean-heroku.herokuapp.com/login");

            try {
                // Add your data

                JSONObject user = new JSONObject();
                try {
                    user.put("email", "mk2333@cornell.edu");
                    user.put("password", "mhdkhadafi");
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                StringEntity se = new StringEntity(user.toString());
                se.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));

                List nameValuePairs = new ArrayList();
                httppost1.setEntity(se);

                // Execute HTTP Post Request
                HttpResponse response = httpclient1.execute(httppost1);

                Log.d("response", EntityUtils.toString(response.getEntity()));

            } catch(Exception e) {
                e.printStackTrace();
                Log.d("Error", "Cannot Estabilish Connection");
            }
            //-------------------------------------
            // Create a new HttpClient and Post Header
            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost("http://test-mean-heroku.herokuapp.com/articles");

            try {
                // Add your data

                JSONObject user = new JSONObject();
                try {
                    //user.put("user", {\"user\":{\"email\":\"mk2333@cornell.edu\",\"hashed_password\":\"LVfbOkMEyCXig4aVh9lYorStck03aNOL2NZdEkIgxV6zSGdyYgX6pLiEM4ZGZq0e60Z2o0mIKlSdjzlmPfYvLw==\",\"salt\":\"V/3GWpu9lKYXVuQmFP41Ig==\",\"username\":\"mhdkhadafi2\",\"name\":\"Muhammad Khadafi\",\"_id\":\"544a6627e806d708007f3462\",\"__v\":0,\"provider\":\"local\",\"roles\":[\"authenticated\"]},\"redirect\":false}\n")
                    user.put("user", "544a6627e806d708007f3462");
                    user.put("title", article_title_send);
                    user.put("content", article_content_send);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                //JSONArray jsonArray = new JSONArray();
                //jsonArray.put(user);

                //JSONObject user_list = new JSONObject();
                //try {
                //    user_list.put("user", jsonArray);
                //} catch (JSONException e) {
                //    e.printStackTrace();
                //}

                StringEntity se = new StringEntity(user.toString());
                se.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));

                List nameValuePairs = new ArrayList();
                httppost.setEntity(se);

                // Execute HTTP Post Request
                ResponseHandler responseHandler = new BasicResponseHandler();
                HttpResponse response = httpclient.execute(httppost);

                //This is the response from a php application
                //responseText = response.toString();
                //String responseID = responseText.substring(responseText.indexOf("\"id\":") + 5,
                //        responseText.indexOf("}"));

                Log.d("response", EntityUtils.toString(response.getEntity()));

            } catch(Exception e) {
                e.printStackTrace();
                Log.d("Error", "Cannot Estabilish Connection");
            }
        }
    }
}
