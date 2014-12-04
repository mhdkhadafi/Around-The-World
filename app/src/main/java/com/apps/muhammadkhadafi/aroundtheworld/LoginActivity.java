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

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_login);

        email = (EditText) findViewById(R.id.email_login);
        password = (EditText) findViewById(R.id.password_login);
        login_button = (Button) findViewById(R.id.login_user);

        login_button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                new MyAsyncTask().execute(email.getText() + "", password.getText() + "");

                Intent i = new Intent(getApplicationContext(),PhotoIntentActivity.class);
                startActivity(i);
                setContentView(R.layout.add_new_meal);
                // Do something in response to button click
            }
        });
    }

    private class MyAsyncTask extends AsyncTask<String, Void, String> {

        SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPrefs", 0);
        String responseText;

        @Override
        protected String doInBackground(String... params) {
            postData(params[0], params[1]);

            return responseText;
        }

        public void postData(String email_send, String password_send) {
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

                JSONObject jsonObject = new JSONObject(responseString);
                JSONObject userObject = jsonObject.getJSONObject("user");
                String userName = userObject.getString("username");
                String userId = userObject.getString("_id");

                SharedPreferences.Editor editor = pref.edit();
                editor.putString("id", userId);
                editor.commit();

            } catch(Exception e) {
                e.printStackTrace();
                Log.d("Error", "Cannot Estabilish Connection");
            }
        }
    }
}
