package com.apps.muhammadkhadafi.aroundtheworld;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.media.ThumbnailUtils;
import android.net.Uri;
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

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

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

    private Bitmap mImageBitmap;
    private Bitmap mThumbnailBitmap;

    private String mCurrentPhotoPath = "";

    private static final String JPEG_FILE_PREFIX = "MEAL_";
    private static final String JPEG_FILE_SUFFIX = ".jpg";

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
                    b.putString("photopath", mCurrentPhotoPath);
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
                // TODO Save to server
            }
        });

        mAlbumStorageDirFactory = new BaseAlbumDirFactory();

        Bundle b = getIntent().getExtras();
        if (b != null) {
            mMealName.setText(b.getString("foodid"));
            mCurrentPhotoPath = b.getString("photopath");
        }

        // TODO : Get the photo back coming back from food selection
        // TODO get food name and nutrition


//        Log.d("response", mCurrentPhotoPath);
//
//        if (!mCurrentPhotoPath.equals("")) try {
//            setPic();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }

    @Override
    protected void onResume() {
        super.onResume();



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
            mCurrentPhotoPath = null;
        }
    }

    private void setPic() throws IOException {

		/* There isn't enough memory to open up more than a couple camera photos */
		/* So pre-scale the target bitmap into which the file is decoded */

		/* Get the size of the ImageView */
        int targetW = mMealImageButton.getWidth();
        int targetH = mMealImageButton.getHeight();

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
}
