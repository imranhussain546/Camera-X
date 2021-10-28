package com.imran.totalityassignment;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraX;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureConfig;
import androidx.camera.core.Preview;
import androidx.camera.core.PreviewConfig;
import androidx.lifecycle.LifecycleOwner;

import android.content.Intent;
import android.graphics.Matrix;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.imran.totalityassignment.databinding.ActivityOpenCameraBinding;
import com.imran.totalityassignment.editimage.Image_Display_Activity;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class OpenCameraActivity extends AppCompatActivity {
    private CameraX.LensFacing lensFacing = CameraX.LensFacing.FRONT;

    ActivityOpenCameraBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityOpenCameraBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        getSupportActionBar().hide();
        camera();
    }

    private void camera() {
        CameraX.unbindAll();
//        Rational aspectRatio = new Rational(binding.textureView.getWidth(),binding.textureView.getHeight());
//        Size screen= new Size(binding.textureView.getWidth(),binding.textureView.getHeight());
//        PreviewConfig previewConfig = new PreviewConfig.Builder().setTargetAspectRatio(aspectRatio).setTargetResolution(screen).build();
        PreviewConfig previewConfig = new PreviewConfig.Builder().
                setLensFacing(lensFacing)
                .build();
        Preview preview =new Preview(previewConfig);
        preview.setOnPreviewOutputUpdateListener(
                new Preview.OnPreviewOutputUpdateListener() {
                    @Override
                    public void onUpdated(Preview.PreviewOutput output) {
                        ViewGroup parent= (ViewGroup)binding.textureView.getParent();
                        parent.removeView(binding.textureView);
                        parent.addView(binding.textureView,0);
                        binding.textureView.setSurfaceTexture(output.getSurfaceTexture());
                        updateTransform();
                    }
                }
        );

        ImageCaptureConfig imageCaptureConfig = new ImageCaptureConfig.Builder()
                .setCaptureMode(ImageCapture.CaptureMode.MIN_LATENCY)
                .setLensFacing(lensFacing)
                .setTargetRotation(this.getWindowManager().getDefaultDisplay().getRotation()).build();
        final ImageCapture imgCap = new ImageCapture(imageCaptureConfig);

        binding.capture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                File photoFile = null;
                try{
                    photoFile = createImageFile();
                }catch (IOException e){
                    Log.d("","Couldn't create File");}
                if(photoFile != null){
                    imgCap.takePicture(photoFile, new ImageCapture.OnImageSavedListener() {
                        @Override
                        public void onImageSaved(@NonNull File file) {
                            Intent i = new Intent(OpenCameraActivity.this, Image_Display_Activity.class);
                            int H = getWindow().getDecorView().getHeight();
                            int W = getWindow().getDecorView().getWidth();
                            i.putExtra("height",H);
                            i.putExtra("width",W);
                            startActivity(i);
                        }

                        @Override
                        public void onError(@NonNull ImageCapture.UseCaseError useCaseError, @NonNull String message, @Nullable Throwable cause) {
                            String msg = "Pic capture failed : " + message;
                            Toast.makeText(OpenCameraActivity.this, msg,Toast.LENGTH_LONG).show();
                            if(cause != null){
                                cause.printStackTrace();
                            }
                        }
                    });
                }
            }
        });
        CameraX.bindToLifecycle((LifecycleOwner)this, preview, imgCap);
    }

    //function to create a file to store the image. It creates new file name with time stamp
    private File createImageFile() throws IOException{
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_"+timeStamp+"_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(imageFileName,".jpg",storageDir);

        HomeActivity.mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }
    private void updateTransform() {
        Matrix mx = new Matrix();
        float w = binding.textureView.getMeasuredWidth();
        float h = binding.textureView.getMeasuredHeight();

        float cX = w / 2f;
        float cY = h / 2f;

        int rotationDgr;
        int rotation = (int)binding.textureView.getRotation();

        switch(rotation){
            case Surface.ROTATION_0:
                rotationDgr = 0;
                break;
            case Surface.ROTATION_90:
                rotationDgr = 90;
                break;
            case Surface.ROTATION_180:
                rotationDgr = 180;
                break;
            case Surface.ROTATION_270:
                rotationDgr = 270;
                break;
            default:
                return;
        }

        mx.postRotate((float)rotationDgr, cX, cY);
        binding.textureView.setTransform(mx);
    }
}