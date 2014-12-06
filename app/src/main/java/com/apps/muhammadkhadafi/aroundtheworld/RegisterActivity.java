package com.apps.muhammadkhadafi.aroundtheworld;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
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
 * Created by mhd.khadafi on 10/29/2014.
 */
public class RegisterActivity extends Activity {

    EditText full_name;
    EditText user_name;
    EditText email;
    EditText password;
    Button submit_button;
    Button btnSignIn;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_register);

        full_name = (EditText) findViewById(R.id.full_name);
        user_name = (EditText) findViewById(R.id.username);
        email = (EditText) findViewById(R.id.email);
        password = (EditText) findViewById(R.id.password);
        submit_button = (Button) findViewById(R.id.submit_user);
        btnSignIn = (Button) findViewById(R.id.btn_signin);

        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(),LoginActivity.class);
                startActivity(i);
            }
        });
        submit_button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                new MyAsyncTask().execute(full_name.getText() + "", user_name.getText() + "",
                        email.getText() + "", password.getText() + "");
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
            postData(params[0], params[1], params[2], params[3]);

            return responseText;
        }

        public void postData(String full_name_send, String user_name_send, String email_send, String password_send) {
            // Create a new HttpClient and Post Header
            HttpClient httpclient = new DefaultHttpClient();
                HttpPost httppost = new HttpPost("http://shielded-taiga-6664.herokuapp.com/register");

            try {
                // Add your data

                JSONObject user = new JSONObject();
                try {
                    user.put("email", email_send);
                    user.put("password", password_send);
                    user.put("confirmPassword", password_send);
                    user.put("username", user_name_send);
                    user.put("name", full_name_send);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                StringEntity se = new StringEntity(user.toString());
                se.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));

                List nameValuePairs = new ArrayList();
                httppost.setEntity(se);

                // Execute HTTP Post Request
                ResponseHandler responseHandler = new BasicResponseHandler();
                HttpResponse response = httpclient.execute(httppost);

                Log.d("response", EntityUtils.toString(response.getEntity()));

            } catch(Exception e) {
                e.printStackTrace();
                Log.d("Error", "Cannot Estabilish Connection");
            }
        }

        @Override
        protected void onPostExecute(String response) {
            super.onPostExecute(response);

        }
    }
}
