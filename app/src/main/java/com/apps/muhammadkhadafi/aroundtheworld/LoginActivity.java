package com.apps.muhammadkhadafi.aroundtheworld;

import android.app.Activity;
import android.content.Entity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by mhd.khadafi on 10/24/2014.
 */
public class LoginActivity extends Activity {

    EditText email;
    EditText password;
    Button login_button;
    Button btnSignUp;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_login);

        email = (EditText) findViewById(R.id.email_login);
        password = (EditText) findViewById(R.id.password_login);
        login_button = (Button) findViewById(R.id.login_user);
        btnSignUp = (Button) findViewById(R.id.btn_signup);

        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(),RegisterActivity.class);
                startActivity(i);
            }
        });
        login_button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                new MyAsyncTask().execute(email.getText() + "", password.getText() + "");
            }
        });

        SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPrefs", 0);
        if (!pref.getString("id", "").equals("")) {
            Intent i = new Intent(getApplicationContext(),HomeActivity.class);
            startActivity(i);
        }
    }

    private class MyAsyncTask extends AsyncTask<String, Void, String> {

        SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPrefs", 0);
        String responseText;

        @Override
        protected String doInBackground(String... params) {
            responseText = postData(params[0], params[1]);

            return responseText;
        }

        public String postData(String email_send, String password_send) {
            // Create a new HttpClient and Post Header
            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost("http://shielded-taiga-6664.herokuapp.com/login");

            try {
                // Add your data

                JSONObject user = new JSONObject();
                try {
                    user.put("email", email_send);
                    user.put("password", password_send);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                StringEntity se = new StringEntity(user.toString());
                se.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));

                List nameValuePairs = new ArrayList();
                httppost.setEntity(se);

                // Execute HTTP Post Request
                HttpResponse response = httpclient.execute(httppost);
                String responseString = EntityUtils.toString(response.getEntity());
                Log.d("response", responseString);

                return responseString;

            } catch(Exception e) {
                e.printStackTrace();
                Log.d("Error", "Cannot Estabilish Connection");
                return "Cannot Estabilish Connection";
            }
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            if (result.equals("Unauthorized")) {
                Toast.makeText(getApplicationContext(), "Login failed", Toast.LENGTH_SHORT).show();
            }
            else {
                Intent i = new Intent(getApplicationContext(),HomeActivity.class);
                startActivity(i);

                JSONObject jsonObject = null;
                String userId = null;
                String userName = null;
                JSONObject userObject = null;
                try {
                    jsonObject = new JSONObject(result);
                    userObject = jsonObject.getJSONObject("user");
                    userName = userObject.getString("username");
                    userId = userObject.getString("_id");
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                SharedPreferences.Editor editor = pref.edit();
                editor.putString("id", userId);
                editor.putString("username", userName);
                editor.commit();

                Toast.makeText(getApplicationContext(), "Logged in", Toast.LENGTH_SHORT);
            }

        }
    }
}
