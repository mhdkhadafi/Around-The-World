package com.apps.muhammadkhadafi.aroundtheworld;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.PutObjectResult;

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

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Created by muhammadkhadafi on 12/5/14.
 */
public class MealAddActivity extends Activity {

    private static final int ACTION_TAKE_PHOTO = 1;

    private static final String ALBUM_NAME = "Around_The_World";

    private ImageButton mMealImageButton;
    private EditText mMealName;
    private Button mSearchButton;
    private Button mBackButton;
    private Button mSaveButton;
    private TextView mNutritionText;
    private String[] mNutritionData;

    private Bitmap mImageBitmap;
    private Bitmap mThumbnailBitmap;

    private String mCurrentPhotoPath = null;
    private String mPassedPhotoPath = "";
    private String todayDate = "";

    private static final String JPEG_FILE_PREFIX = "MEAL_";
    private static final String JPEG_FILE_SUFFIX = ".jpg";

    private static String APPID = "0c780600";
    private static String APPKEY = "9ac341cc5667fde1fa36f683ac4ea15e";

    private AlbumStorageDirFactory mAlbumStorageDirFactory = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_new_meal);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        mMealImageButton = (ImageButton) findViewById(R.id.btn_mealphoto);
        mSaveButton = (Button) findViewById(R.id.btn_save);
        mSearchButton = (Button) findViewById(R.id.btn_search);
        mBackButton = (Button) findViewById(R.id.btn_back);
        mMealName = (EditText) findViewById(R.id.txt_mealname);
        mNutritionText = (TextView) findViewById(R.id.txt_mealnutrition);
        mImageBitmap = null;

        Bundle b = getIntent().getExtras();
        if (b != null) {
//            mMealName.setText(b.getString("foodid"));
            mCurrentPhotoPath = b.getString("photopath");
            Log.d("response", mCurrentPhotoPath);
            new FoodAsync().execute(b.getString("foodid"));
        }

        mMealImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dispatchTakePictureIntent(ACTION_TAKE_PHOTO);
            }
        });

        mSearchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mMealName.getText().toString().equals("")) {
                    Toast.makeText(getApplicationContext(), "Fill search term first", Toast.LENGTH_LONG);
                }
                else {
                    Intent intent = new Intent(getApplicationContext(), SearchFoodActivity.class);
                    Bundle b = new Bundle();
                    b.putString("search", mMealName.getText().toString());
                    b.putString("photopath", mPassedPhotoPath);
                    intent.putExtras(b);
                    startActivity(intent);
                    finish();
                }
            }
        });

        mBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
                startActivity(intent);
            }
        });

        mSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {




                SimpleDateFormat sdf = new SimpleDateFormat("MMddyyyy_hhmmss");
                Date d = new Date(System.currentTimeMillis());
                todayDate = sdf.format(d);

                new S3PutObjectTask().execute();

                SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPrefs", 0);
                String userid = pref.getString("id", "");
                String username = pref.getString("username", "");

                new MeanPostArticle().execute(mMealName.getText().toString(),
                        "https://s3.amazonaws.com/around-the-world/"
                                + "food_"
                                + todayDate, "https://s3.amazonaws.com/around-the-world/"
                                + mMealName.getText().toString() + "_" + todayDate + "thumb",
                        userid, username);
                // TODO Save to server
            }
        });

        mAlbumStorageDirFactory = new BaseAlbumDirFactory();



        // TODO : Get the photo back coming back from food selection
        // TODO get food name and nutrition

    }

    @Override
    protected void onResume() {
        super.onResume();

        if (mCurrentPhotoPath != null) {

//            try {
//                setPic();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }

            Log.d("response", mMealImageButton.getHeight() + "");
        }

    }

    private void dispatchTakePictureIntent(int actionCode) {

        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        switch(actionCode) {
            case ACTION_TAKE_PHOTO:
                File f = null;

                try {
                    f = setUpPhotoFile();
                    mCurrentPhotoPath = f.getAbsolutePath();
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
                } catch (IOException e) {
                    e.printStackTrace();
                    f = null;
                    mCurrentPhotoPath = null;
                }
                break;

            default:
                break;
        } // switch

        startActivityForResult(takePictureIntent, actionCode);
    }

    private File setUpPhotoFile() throws IOException {

        File f = createImageFile();
        mCurrentPhotoPath = f.getAbsolutePath();

        return f;
    }

    private File createImageFile() throws IOException {

        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = JPEG_FILE_PREFIX + timeStamp;
        File albumF = getAlbumDir();
        File imageF = File.createTempFile(imageFileName, JPEG_FILE_SUFFIX, albumF);
        return imageF;
    }

    private File getAlbumDir() {
        File storageDir = null;

        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {

            storageDir = mAlbumStorageDirFactory.getAlbumStorageDir(ALBUM_NAME);

            if (storageDir != null) {
                if (! storageDir.mkdirs()) {
                    if (! storageDir.exists()){
                        Log.d("CameraSample", "failed to create directory");
                        return null;
                    }
                }
            }

        } else {
            Log.v(getString(R.string.app_name), "External storage is not mounted READ/WRITE.");
        }

        return storageDir;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case ACTION_TAKE_PHOTO: {
                if (resultCode == RESULT_OK) {
                    try {
                        handleBigCameraPhoto();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                break;
            } // ACTION_TAKE_PHOTO_B
        } // switch
    }

    private void handleBigCameraPhoto() throws IOException {

        if (mCurrentPhotoPath != null) {
            setPic();
            galleryAddPic();
            mPassedPhotoPath = mCurrentPhotoPath;
            mCurrentPhotoPath = null;
        }
    }

    private void setPic() throws IOException {

		/* There isn't enough memory to open up more than a couple camera photos */
		/* So pre-scale the target bitmap into which the file is decoded */

//        mMealImageButton = (ImageButton) findViewById(R.id.btn_mealphoto);
		/* Get the size of the ImageView */
        int targetW = mMealImageButton.getWidth();
        int targetH = mMealImageButton.getHeight();
        Log.d("response", targetW + "-" + targetH);

        ExifInterface exif = new ExifInterface(mCurrentPhotoPath);
        int rotation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
        int rotationInDegrees;

        if (rotation == ExifInterface.ORIENTATION_ROTATE_90) { rotationInDegrees = 90; }
        else if (rotation == ExifInterface.ORIENTATION_ROTATE_180) {  rotationInDegrees = 180; }
        else if (rotation == ExifInterface.ORIENTATION_ROTATE_270) {  rotationInDegrees = 270; }
        else { rotationInDegrees = 0; }

        Matrix matrix = new Matrix();
        if (rotation != 0f) {matrix.preRotate(rotationInDegrees);}

        Bitmap originalBitmap = BitmapFactory.decodeFile(mCurrentPhotoPath);
        Bitmap rotatedBitmap = Bitmap.createBitmap(originalBitmap, 0, 0,
                originalBitmap.getWidth() - 1, originalBitmap.getHeight() - 1, matrix, true);
        Bitmap thumbnailBmp = ThumbnailUtils.extractThumbnail(rotatedBitmap, targetW, targetH);

		/* Associate the Bitmap to the ImageView */
        mMealImageButton.setImageBitmap(thumbnailBmp);
    }

    private void galleryAddPic() throws IOException {
        Intent mediaScanIntent = new Intent("android.intent.action.MEDIA_SCANNER_SCAN_FILE");
        File f = new File(mCurrentPhotoPath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);

        mThumbnailBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), contentUri);
    }

    private class FoodAsync extends AsyncTask<String, Void, String> {

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

        public String getFood(String foodId) throws UnsupportedEncodingException {
            // Create a new HttpClient and Post Header

            String searchURL = "https://api.nutritionix.com/v1_1/item/";
            String searchTerm = foodId;

            String resultReturns = "";

            String urlResult = searchURL + "?id=" +
                    URLEncoder.encode(searchTerm, "UTF-8") + "&appId=" + APPID + "&appKey=" + APPKEY;

            Log.d("response", urlResult);

//            String header = "&" + args + "&oauth_signature=" + URLEncoder.encode(signature, "UTF-8");
            HttpClient httpclient = new DefaultHttpClient();
            HttpGet httpGet = new HttpGet(urlResult);

            try {
                HttpResponse response = httpclient.execute(httpGet);
                String responseString = EntityUtils.toString(response.getEntity());
                Log.d("response", responseString);

                JSONObject jsonObject = new JSONObject(responseString);

                String foodName = jsonObject.getString("item_name");
                String nutritionData = "";

;
                nutritionData = jsonObject.getString("nf_serving_size_qty")
                            + "----" +jsonObject.getString("nf_serving_size_unit")
                            + "----" +jsonObject.getString("nf_serving_weight_grams")
                            + "----" +jsonObject.getString("nf_calories")
                            + "----" +jsonObject.getString("nf_calories_from_fat")
                            + "----" +jsonObject.getString("nf_total_fat")
                            + "----" +jsonObject.getString("nf_saturated_fat")
                            + "----" +jsonObject.getString("nf_trans_fatty_acid")
                            + "----" +jsonObject.getString("nf_cholesterol")
                            + "----" +jsonObject.getString("nf_sodium")
                            + "----" +jsonObject.getString("nf_total_carbohydrate")
                            + "----" +jsonObject.getString("nf_dietary_fiber")
                            + "----" +jsonObject.getString("nf_sugars")
                            + "----" +jsonObject.getString("nf_protein")
                            + "----" +jsonObject.getString("nf_vitamin_a_dv")
                            + "----" +jsonObject.getString("nf_vitamin_c_dv")
                            + "----" +jsonObject.getString("nf_calcium_dv")
                            + "----" +jsonObject.getString("nf_iron_dv");

                resultReturns = foodName + "--nutrition--" + nutritionData;

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
                Log.d("response", result);
                mMealName.setText(result.split("--nutrition--")[0]);
                mNutritionData = result.split("--nutrition--")[1].split("----");
                mNutritionText.setText(nutritionString(result.split("--nutrition--")[1].split("----")));
                try {
                    setPic();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private class MeanPostArticle extends AsyncTask<String, Void, String> {

        SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPrefs", 0);
        String responseText;

        @Override
        protected String doInBackground(String... params) {

            postData(params[0], params[1], params[2], params[3], params[4]);

            return responseText;
        }

        public void postData(String article_title_send, String article_content_send,
                             String article_thumbnail_send, String article_user,
                             String article_username) {
            // Create a new HttpClient and Post Header
            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost("http://shielded-taiga-6664.herokuapp.com/articles");

            try {
                // Add your data

                JSONObject nutrition = new JSONObject();
                try {
                    nutrition.put("serving_amt", mNutritionData[0]);
                    nutrition.put("serving_type", mNutritionData[1]);
                    nutrition.put("serving_size", mNutritionData[2]);
                    nutrition.put("calorie", mNutritionData[3]);
                    nutrition.put("calorie_from_fat", mNutritionData[4]);
                    nutrition.put("total_fat", mNutritionData[5]);
                    nutrition.put("saturated_fat", mNutritionData[6]);
                    nutrition.put("trans_fat", mNutritionData[7]);
                    nutrition.put("cholesterol", mNutritionData[8]);
                    nutrition.put("sodium", mNutritionData[9]);
                    nutrition.put("total_carbohydrate", mNutritionData[10]);
                    nutrition.put("dietary_fiber", mNutritionData[11]);
                    nutrition.put("sugars", mNutritionData[12]);
                    nutrition.put("protein", mNutritionData[13]);
                    nutrition.put("vit_a", mNutritionData[14]);
                    nutrition.put("vit_c", mNutritionData[15]);
                    nutrition.put("calcium", mNutritionData[16]);
                    nutrition.put("iron", mNutritionData[17]);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                JSONObject user = new JSONObject();
                try {
                    user.put("user", article_user);
                    user.put("username", article_username);
                    user.put("content", article_content_send);
                    user.put("title", article_title_send);
                    user.put("thumbnail", article_thumbnail_send);
                    user.put("nutrition", nutrition.toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                StringEntity se = new StringEntity(user.toString());
                se.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));

                httppost.setEntity(se);

                // Execute HTTP Post Request
                HttpResponse response = httpclient.execute(httppost);

                Log.d("response", EntityUtils.toString(response.getEntity()));

            } catch(Exception e) {
                e.printStackTrace();
                Log.d("Error", "Cannot Estabilish Connection");
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

    private class S3PutObjectTask extends AsyncTask<Uri, Void, S3TaskResult> {

        ProgressDialog dialog;
        AmazonS3Client s3Client = new AmazonS3Client(
                new BasicAWSCredentials( "AKIFQ",
                        "zlqOd46Ergt2+Nfn" ) );



        protected S3TaskResult doInBackground(Uri... uris) {
            EditText meal_name = (EditText) findViewById(R.id.txt_mealname);

            Uri selectedImage;
            if (uris == null || uris.length != 1) {
                Log.d("response", "photopath"+mCurrentPhotoPath);
                File f = new File(mCurrentPhotoPath);
                Uri contentUri = Uri.fromFile(f);
                selectedImage = contentUri;
            }
            else {
                selectedImage = uris[0];
            }


            S3TaskResult result = new S3TaskResult();


            // Put the image data into S3.
            //try {
            s3Client.createBucket(getPictureBucket());

            // Content type is determined by file extension.
            PutObjectRequest por = null;
            por = new PutObjectRequest(
                    getPictureBucket(), "food_" + todayDate,
                    new File(selectedImage.getPath())).withCannedAcl(CannedAccessControlList.PublicRead);


            PutObjectResult putResult = s3Client.putObject(por);
            ObjectListing ol = s3Client.listObjects("around-the-world");
            Log.d("returns", ol.getObjectSummaries().toString());
            Log.d("returns", putResult.getETag());

            return result;
        }

        protected void onPostExecute(S3TaskResult result) {

            if (result.getErrorMessage() != null) {

            }
        }
    }

    private class S3TaskResult {
        String errorMessage = null;
        Uri uri = null;

        public String getErrorMessage() {
            return errorMessage;
        }

        public void setErrorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
        }

        public Uri getUri() {
            return uri;
        }

        public void setUri(Uri uri) {
            this.uri = uri;
        }
    }

    public static String getPictureBucket() {
        return ("around-the-world").toLowerCase(Locale.US);
    }
}
