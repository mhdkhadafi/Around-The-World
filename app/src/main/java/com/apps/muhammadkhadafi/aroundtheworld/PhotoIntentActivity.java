package com.apps.muhammadkhadafi.aroundtheworld;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.provider.SyncStateContract;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.VideoView;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.PutObjectResult;

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


public class PhotoIntentActivity extends Activity {

	private static final int ACTION_TAKE_PHOTO = 1;

	private static final String BITMAP_STORAGE_KEY = "viewbitmap";
	private static final String IMAGEVIEW_VISIBILITY_STORAGE_KEY = "imageviewvisibility";
	private ImageView mImageView;
	private Bitmap mImageBitmap;
    private Bitmap mThumbnailBitmap;

	private String mCurrentPhotoPath;

	private static final String JPEG_FILE_PREFIX = "IMG_";
	private static final String JPEG_FILE_SUFFIX = ".jpg";

	private AlbumStorageDirFactory mAlbumStorageDirFactory = null;
	
	/* Photo album for this application */
	private String getAlbumName() {
		return "Around The World";
	}
	
	private File getAlbumDir() {
		File storageDir = null;

		if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
			
			storageDir = mAlbumStorageDirFactory.getAlbumStorageDir(getAlbumName());

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

	private File createImageFile() throws IOException {
        EditText meal_name = (EditText) findViewById(R.id.txt_mealname);

		// Create an image file name
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
		String imageFileName = JPEG_FILE_PREFIX + meal_name.getText() + "_";
		File albumF = getAlbumDir();
		File imageF = File.createTempFile(imageFileName, JPEG_FILE_SUFFIX, albumF);
		return imageF;
	}

	private File setUpPhotoFile() throws IOException {
		
		File f = createImageFile();
		mCurrentPhotoPath = f.getAbsolutePath();
		
		return f;
	}

	private void setPic() {

		/* There isn't enough memory to open up more than a couple camera photos */
		/* So pre-scale the target bitmap into which the file is decoded */

		/* Get the size of the ImageView */
		int targetW = mImageView.getWidth();
		int targetH = mImageView.getHeight();

		/* Get the size of the image */
		BitmapFactory.Options bmOptions = new BitmapFactory.Options();
		bmOptions.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
		int photoW = bmOptions.outWidth;
		int photoH = bmOptions.outHeight;
		
		/* Figure out which way needs to be reduced less */
		int scaleFactor = 1;
		if ((targetW > 0) || (targetH > 0)) {
			scaleFactor = Math.min(photoW/targetW, photoH/targetH);	
		}

		/* Set bitmap options to scale the image decode target */
		bmOptions.inJustDecodeBounds = false;
		bmOptions.inSampleSize = scaleFactor;
		bmOptions.inPurgeable = true;

		/* Decode the JPEG file into a Bitmap */
		Bitmap bitmap = BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);

        Bitmap thumb = ThumbnailUtils.extractThumbnail(bitmap, 500, 500);
        File thumb_file = storeThumbnail(thumb);

        Uri thumb_uri = Uri.fromFile(thumb_file);

        SharedPreferences prefs = getApplicationContext().getSharedPreferences("MyPrefs", 0);
        String id = prefs.getString("id", "");
        new S3PutThumbnailTask().execute(thumb_uri);

        EditText meal_name = (EditText) findViewById(R.id.txt_mealname);
        new MeanPostArticle().execute(meal_name.getText() + "", "https://s3.amazonaws.com/my-unique-nameakiajn46spjhkwzxslvabucketone/" + meal_name.getText(),
                "https://s3.amazonaws.com/my-unique-nameakiajn46spjhkwzxslvabucketone/" + meal_name.getText() + "thumb", id,
                "daffi");
		
		/* Associate the Bitmap to the ImageView */
		mImageView.setImageBitmap(bitmap);
		mImageView.setVisibility(View.VISIBLE);
	}

    private File storeThumbnail(Bitmap image) {
        File pictureFile = getOutputMediaFile();
        if (pictureFile == null) {
            Log.d("err",
                    "Error creating media file, check storage permissions: ");// e.getMessage());
            return pictureFile;
        }
        try {
            FileOutputStream fos = new FileOutputStream(pictureFile);
            image.compress(Bitmap.CompressFormat.PNG, 90, fos);
            fos.close();
        } catch (FileNotFoundException e) {
            Log.d("err", "File not found: " + e.getMessage());
        } catch (IOException e) {
            Log.d("err", "Error accessing file: " + e.getMessage());
        }

        return pictureFile;
    }

    /** Create a File for saving an image or video */
    private  File getOutputMediaFile(){
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.
        File mediaStorageDir = new File(Environment.getExternalStorageDirectory()
                + "/Android/data/"
                + getApplicationContext().getPackageName()
                + "/Files");

        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                return null;
            }
        }
        // Create a media file name
        String timeStamp = new SimpleDateFormat("ddMMyyyy_HHmm").format(new Date());
        File mediaFile;
        String mImageName="MI_"+ timeStamp +"thumb.jpg";
        mediaFile = new File(mediaStorageDir.getPath() + File.separator + mImageName);
        return mediaFile;
    }

    private void galleryAddPic() throws IOException {
        EditText meal_name = (EditText) findViewById(R.id.txt_mealname);

        Intent mediaScanIntent = new Intent("android.intent.action.MEDIA_SCANNER_SCAN_FILE");
        File f = new File(mCurrentPhotoPath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);

        Log.d("testtest", mCurrentPhotoPath);
        Log.d("testtest", contentUri.toString());
        Log.d("testtest", contentUri.getPath());

        mThumbnailBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), contentUri);

        new S3PutObjectTask().execute(contentUri);
        //uploadToAWS(mCurrentPhotoPath);
	}

    public String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } finally {
                cursor.close();
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
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

	private void handleBigCameraPhoto() throws IOException {

		if (mCurrentPhotoPath != null) {
			setPic();
			galleryAddPic();
			mCurrentPhotoPath = null;
		}
	}

	Button.OnClickListener mTakePicOnClickListener = 
		new Button.OnClickListener() {
		@Override
		public void onClick(View v) {
			dispatchTakePictureIntent(ACTION_TAKE_PHOTO);
		}
	};

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.add_new_meal);

//		mImageView = (ImageView) findViewById(R.id.meal_photo);
		mImageBitmap = null;

		Button picBtn = (Button) findViewById(R.id.btn_back);
		setBtnListenerOrDisable( 
				picBtn, 
				mTakePicOnClickListener,
				MediaStore.ACTION_IMAGE_CAPTURE
		);

        mAlbumStorageDirFactory = new BaseAlbumDirFactory();
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

	// Some lifecycle callbacks so that the image can survive orientation change
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putParcelable(BITMAP_STORAGE_KEY, mImageBitmap);
		outState.putBoolean(IMAGEVIEW_VISIBILITY_STORAGE_KEY, (mImageBitmap != null) );
		super.onSaveInstanceState(outState);
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		mImageBitmap = savedInstanceState.getParcelable(BITMAP_STORAGE_KEY);
		mImageView.setImageBitmap(mImageBitmap);
		mImageView.setVisibility(
				savedInstanceState.getBoolean(IMAGEVIEW_VISIBILITY_STORAGE_KEY) ? 
						ImageView.VISIBLE : ImageView.INVISIBLE
		);
	}

	/**
	 * Indicates whether the specified action can be used as an intent. This
	 * method queries the package manager for installed packages that can
	 * respond to an intent with the specified action. If no suitable package is
	 * found, this method returns false.
	 * http://android-developers.blogspot.com/2009/01/can-i-use-this-intent.html
	 *
	 * @param context The application's environment.
	 * @param action The Intent action to check for availability.
	 *
	 * @return True if an Intent with the specified action can be sent and
	 *         responded to, false otherwise.
	 */
	public static boolean isIntentAvailable(Context context, String action) {
		final PackageManager packageManager = context.getPackageManager();
		final Intent intent = new Intent(action);
		List<ResolveInfo> list =
			packageManager.queryIntentActivities(intent,
					PackageManager.MATCH_DEFAULT_ONLY);
		return list.size() > 0;
	}

	private void setBtnListenerOrDisable( 
			Button btn, 
			Button.OnClickListener onClickListener,
			String intentName
	) {
		if (isIntentAvailable(this, intentName)) {
			btn.setOnClickListener(onClickListener);        	
		} else {
			btn.setText( 
				"cannot" + " " + btn.getText());
			btn.setClickable(false);
		}
	}

    private class S3PutObjectTask extends AsyncTask<Uri, Void, S3TaskResult> {

        ProgressDialog dialog;
        AmazonS3Client s3Client = new AmazonS3Client(
                new BasicAWSCredentials( "AKIAJN46SPJHKWZXSLVA",
                        "pQXtOT3PE+m8/HGlFbTBun5ggmOHUoGnBVlbkAat" ) );



        protected S3TaskResult doInBackground(Uri... uris) {
            EditText meal_name = (EditText) findViewById(R.id.txt_mealname);

            if (uris == null || uris.length != 1) {
                return null;
            }

            // The file location of the image selected.
            Uri selectedImage = uris[0];

            /*
            String[] filePathColumn = { MediaStore.Images.Media.DATA };

            Cursor cursor = getContentResolver().query(selectedImage,
                    filePathColumn, null, null, null);
            cursor.moveToFirst();

            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String filePath = cursor.getString(columnIndex);
            cursor.close();
            */
            S3TaskResult result = new S3TaskResult();


            // Put the image data into S3.
            //try {
                s3Client.createBucket(getPictureBucket());

                // Content type is determined by file extension.
                PutObjectRequest por = new PutObjectRequest(
                        getPictureBucket(), meal_name.getText() + "",
                        new java.io.File(selectedImage.getPath())).withCannedAcl(CannedAccessControlList.PublicRead);


            PutObjectResult putResult = s3Client.putObject(por);
            ObjectListing ol = s3Client.listObjects(getPictureBucket());
            Log.d("returns", ol.getObjectSummaries().toString());
            Log.d("returns", putResult.getETag());

//            s3Client.bu
//            Log.d("returns", putResult.getContentMd5());
            //} catch (Exception exception) {
             //   Log.d("testtest", "not created");
              //  result.setErrorMessage(exception.getMessage());
            //}

            return result;
        }

        protected void onPostExecute(S3TaskResult result) {

            if (result.getErrorMessage() != null) {

            }
        }
    }

    private class S3PutThumbnailTask extends AsyncTask<Uri, Void, S3TaskResult> {

        ProgressDialog dialog;
        AmazonS3Client s3Client = new AmazonS3Client(
                new BasicAWSCredentials( "AKIAJN46SPJHKWZXSLVA",
                        "pQXtOT3PE+m8/HGlFbTBun5ggmOHUoGnBVlbkAat" ) );



        protected S3TaskResult doInBackground(Uri... uris) {
            EditText meal_name = (EditText) findViewById(R.id.txt_mealname);

            if (uris == null || uris.length != 1) {
                return null;
            }

            // The file location of the image selected.
            Uri selectedImage = uris[0];

            /*
            String[] filePathColumn = { MediaStore.Images.Media.DATA };

            Cursor cursor = getContentResolver().query(selectedImage,
                    filePathColumn, null, null, null);
            cursor.moveToFirst();

            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String filePath = cursor.getString(columnIndex);
            cursor.close();
            */
            S3TaskResult result = new S3TaskResult();


            // Put the image data into S3.
            //try {
            s3Client.createBucket(getPictureBucket());

            // Content type is determined by file extension.
            PutObjectRequest por = new PutObjectRequest(
                    getPictureBucket(), meal_name.getText() + "thumb",
                    new java.io.File(selectedImage.getPath())).withCannedAcl(CannedAccessControlList.PublicRead);


            PutObjectResult putResult = s3Client.putObject(por);
            ObjectListing ol = s3Client.listObjects(getPictureBucket());
            Log.d("returns", ol.getObjectSummaries().toString());
            Log.d("returns", putResult.getETag());

//            s3Client.bu
//            Log.d("returns", putResult.getContentMd5());
            //} catch (Exception exception) {
            //   Log.d("testtest", "not created");
            //  result.setErrorMessage(exception.getMessage());
            //}

            return result;
        }

        protected void onPostExecute(S3TaskResult result) {

            if (result.getErrorMessage() != null) {

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
                             String article_thumbnail_send, String article_user, String article_username) {
            // Create a new HttpClient and Post Header
            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost("http://shielded-taiga-6664.herokuapp.com/articles");

            try {
                // Add your data

                JSONObject user = new JSONObject();
                try {
                    user.put("user", article_user);
                    user.put("username", article_username);
                    user.put("content", article_content_send);
                    user.put("title", article_title_send);
                    user.put("thumbnail", article_thumbnail_send);
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

    private void uploadToAWS(String filePath) {
        AmazonS3Client s3Client = new AmazonS3Client(
                new BasicAWSCredentials( "AKIAJN46SPJHKWZXSLVA",
                        "pQXtOT3PE+m8/HGlFbTBun5ggmOHUoGnBVlbkAat" ) );

        s3Client.createBucket("testbucket");

        EditText meal_name = (EditText) findViewById(R.id.txt_mealname);

        PutObjectRequest por = new PutObjectRequest( getPictureBucket(), meal_name.getText() + "", new java.io.File( filePath) );
        s3Client.putObject( por );
    }

    public static String getPictureBucket() {
        return ("my-unique-name" + "AKIAJN46SPJHKWZXSLVA" + "BUCKETONE").toLowerCase(Locale.US);
    }

}