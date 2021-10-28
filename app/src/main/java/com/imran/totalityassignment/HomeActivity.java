package com.imran.totalityassignment;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;


import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;

import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.imran.totalityassignment.databinding.ActivityHomeBinding;
import com.imran.totalityassignment.editimage.Image_Display_Activity;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

public class HomeActivity extends AppCompatActivity {
ActivityHomeBinding binding;
MainActivity activity;
public static final int multiple_Permission_code = 101;
public final int RESULT_LOAD_IMAGE =20;
public static String mCurrentPhotoPath;
public Uri pickedImage;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        permission();
        openCamera();
        openGallery();
        setImage();
//        Picasso.get()
//                .load(R.mipmap.ic_cancel_white_48dp)
//                .into(binding.imageView);


    }


    private void permission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                +ContextCompat.checkSelfPermission(this,Manifest.permission.READ_EXTERNAL_STORAGE)
                +ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.CAMERA,
                    Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE}, multiple_Permission_code);
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode== multiple_Permission_code)
        {
            if (grantResults.length > 0 && grantResults[0]==PackageManager.PERMISSION_GRANTED)
            {
                openCamera();
            }
            else
            {
                Toast.makeText(this, "Camera Permission is Required to use app", Toast.LENGTH_SHORT).show();
            }
        }
    }
    private void openGallery() {
        binding.gallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(Intent.ACTION_PICK,android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(i,RESULT_LOAD_IMAGE);
            }
        });

    }

    private void openCamera() {
        binding.selfie.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(HomeActivity.this,OpenCameraActivity.class);
                startActivity(intent);

            }
        });
    }
    File file;
    private void setImage() {
        Log.e("uriii","method");
        if (getIntent().getExtras()!=null)
        {
           file = (File) getIntent().getExtras().get("imagepath");
            Log.e("uriii","home"+file);
            Picasso.get()
                    .load(file)
                    .resize(400,400)
                    .centerInside()
                    .into(binding.imageView);

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RESULT_LOAD_IMAGE && resultCode == Activity.RESULT_OK) {

            pickedImage = data.getData();
            String[] filePath = {MediaStore.Images.Media.DATA};
            Cursor cursor = getContentResolver().query(pickedImage, filePath, null, null, null);
            cursor.moveToFirst();
             mCurrentPhotoPath = cursor.getString(cursor.getColumnIndex(filePath[0]));
            Intent i = new Intent(this, Image_Display_Activity.class);
            int H = this.getWindow().getDecorView().getHeight();
            int W = this.getWindow().getDecorView().getWidth();
            i.putExtra("height", H);
            i.putExtra("width", W);
            startActivity(i);

        }
    }
}