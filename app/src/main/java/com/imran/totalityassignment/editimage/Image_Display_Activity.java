package com.imran.totalityassignment.editimage;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.util.Pair;
import androidx.fragment.app.FragmentTransaction;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationItemView;
import com.google.android.material.bottomnavigation.BottomNavigationMenuView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.imran.totalityassignment.HomeActivity;
import com.imran.totalityassignment.OpenCameraActivity;
import com.imran.totalityassignment.R;
import com.imran.totalityassignment.databinding.ActivityImageDisplayBinding;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Image_Display_Activity extends AppCompatActivity {
    public static ActivityImageDisplayBinding binding;
    static Bitmap bm = BitmapFactory.decodeFile(HomeActivity.mCurrentPhotoPath);
    static float vH=0,vW=0;
    static BitmapFactory.Options bmOptions;
    private final static String TAG = "DEBUG_BOTTOM_NAV_UTIL";

    static float iHeight = 0;
    @SuppressLint("RestrictedApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding= ActivityImageDisplayBinding.inflate(getLayoutInflater());
        View decorView = getWindow().getDecorView();
        // Hide the status bar.
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
        setContentView(binding.getRoot());
        getSupportActionBar().hide();


        final float targetW = getIntent().getExtras().getInt("width");
        final float targetH = getIntent().getExtras().getInt("height");
        Log.e(TAG, "onCreate: "+targetH+"  "+targetW);
        // Get the dimensions of the bitmap
        bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(HomeActivity.mCurrentPhotoPath, bmOptions);
        float photoW = bmOptions.outWidth;
        float photoH = bmOptions.outHeight;
        {
            vH = targetH*(0.89f);
            vW = (targetH*(0.89f) / (bm.getHeight())) * (bm.getWidth());
            if(vW>targetW){
                vW = targetW;
                vH=(targetW/(bm.getWidth()))*(bm.getHeight());
            }
        }

        // Determine how much to scale down the image
        float scaleFactor = Math.min(photoW/vW,photoH/vH);

        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = (int)scaleFactor;
        bmOptions.inPurgeable = true;
        bm = rotImage(BitmapFactory.decodeFile(HomeActivity.mCurrentPhotoPath,bmOptions));
        bmOptions.inJustDecodeBounds = true;
        iHeight = bmOptions.outHeight;

        //set scaled image to imageDisplay
        binding.imageDisplay.setImageBitmap(bm);

        BottomNavigationMenuView menuView = (BottomNavigationMenuView) binding.optionNavigation.getChildAt(0);
        try {//Set shifting mode of bottom navigation view as false to see all titles
            Field shiftingMode = menuView.getClass().getDeclaredField("mShiftingMode");
            shiftingMode.setAccessible(true);
            shiftingMode.setBoolean(menuView, false);
            shiftingMode.setAccessible(false);
            for (int i = 0; i < menuView.getChildCount(); i++) {
                BottomNavigationItemView item = (BottomNavigationItemView) menuView.getChildAt(i);
                item.setShifting(false);
                // Set once again checked value, so view will be updated
                item.setChecked(item.getItemData().isChecked());
            }
        } catch (NoSuchFieldException e) {
            Log.d(TAG, "Unable to get shift mode field");
        } catch (IllegalAccessException e){
            Log.d(TAG,"Unable to change value of shift mode");
        }

        //Start respective activities when option is chosen from bottom navigation view
        binding.optionNavigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                switch (item.getItemId()){
                    case R.id.action_addText:
                        Intent addTextIntent = new Intent(Image_Display_Activity.this,Add_Text_Activity.class);
                        addTextIntent.putExtra("height",targetH);
                        addTextIntent.putExtra("width",targetW);
                        ActivityOptionsCompat options  = ActivityOptionsCompat.makeSceneTransitionAnimation(Image_Display_Activity.this,new Pair<View, String>(findViewById(R.id.imageDisplay),(getString(R.string.transition_image))));
                        ActivityCompat.startActivity(Image_Display_Activity.this,addTextIntent,options.toBundle());
                        break;

                    case R.id.action_rotateCrop:
                        Intent rotCropIntent = new Intent(Image_Display_Activity.this,Rotate_Crop_Activity.class);
                        rotCropIntent.putExtra("height",targetH);
                        rotCropIntent.putExtra("width",targetW);
                        ActivityOptionsCompat rotCropOptions = ActivityOptionsCompat.makeSceneTransitionAnimation(Image_Display_Activity.this,new Pair<View, String>(findViewById(R.id.imageDisplay),(getString(R.string.transition_image))));
                        ActivityCompat.startActivity(Image_Display_Activity.this,rotCropIntent,rotCropOptions.toBundle());
                        break;
                    case R.id.action_tune:
                        Intent tuneIntent = new Intent(Image_Display_Activity.this,Tune_Activity.class);
                        tuneIntent.putExtra("height",targetH);
                        tuneIntent.putExtra("width",targetW);
                        tuneIntent.putExtra("iHeight",iHeight);
                        ActivityOptionsCompat tuneOptions = ActivityOptionsCompat.makeSceneTransitionAnimation(Image_Display_Activity.this,new Pair<View, String>(findViewById(R.id.imageDisplay),(getString(R.string.transition_image))));
                        ActivityCompat.startActivity(Image_Display_Activity.this,tuneIntent,tuneOptions.toBundle());
                        break;

                }
                return true;
            }
        });


        binding.saveImageDisplay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try{
                    Log.e("saveeeee", "onClick: " );
                    saveImage();
                }catch(Exception e){
                    e.printStackTrace();
                }
            }
        });


        binding.cancelImageDisplay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = getBaseContext().getPackageManager().getLaunchIntentForPackage( getBaseContext().getPackageName() );
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
            }
        });
    }

    //Function to save image
    private void saveImage()throws Exception{
        Log.e(TAG, "saveImage: openmethod" );
        FileOutputStream fOut = null;
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "IMG_"+timeStamp+"_";
        File file2 = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File file = File.createTempFile(imageFileName,".png",file2);
        try{
            fOut = new FileOutputStream(file);
        }catch (Exception e){e.printStackTrace();}
         bm.compress(Bitmap.CompressFormat.PNG,100,fOut);
        try{
            fOut.flush();
        }catch (Exception e){e.printStackTrace();}
        try{fOut.close();}catch (IOException e){e.printStackTrace();}
        Intent intent =new Intent(Image_Display_Activity.this,HomeActivity.class);
        intent.putExtra("imagepath",file);
        startActivity(intent);
        Log.e(TAG, "saveImage: "+file2 );
    }


    private Bitmap rotImage(Bitmap bitmap){
        try {
            ExifInterface exif = new ExifInterface(HomeActivity.mCurrentPhotoPath);
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION,1);

            Matrix matrix  = new Matrix();

            if(orientation == 3){
                Log.e(TAG, "rotImage: 3" );
                matrix.postRotate(180);}
            else if (orientation == 6){
                Log.e(TAG, "rotImage: 6" );
                matrix.postRotate(90);}
            else if(orientation == 8){
                Log.e(TAG, "rotImage: 8" );
                matrix.preRotate(270);}

            bitmap = Bitmap.createBitmap(bitmap,0,0,bitmap.getWidth(),bitmap.getHeight(),matrix,true);
            return bitmap;
        }catch (IOException e){e.printStackTrace();return null;}
    }


}