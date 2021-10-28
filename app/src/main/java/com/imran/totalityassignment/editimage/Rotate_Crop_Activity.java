package com.imran.totalityassignment.editimage;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Toast;

import com.imran.totalityassignment.R;
import com.imran.totalityassignment.databinding.ActivityRotateCropBinding;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Rotate_Crop_Activity extends AppCompatActivity {
ActivityRotateCropBinding binding;
Bitmap textBit = Image_Display_Activity.bm;

    int rot = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityRotateCropBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        getSupportActionBar().hide();


        binding.cropImageView.setImageBitmap(textBit);
        binding.cropImageView.setFixedAspectRatio(false);
        binding.cropImageView.setGuidelines(CropImageView.Guidelines.ON);


        binding.cropBitmapIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                textBit = Image_Display_Activity.bm = binding.cropImageView.getCroppedImage();
                (Image_Display_Activity.binding.imageDisplay).setImageBitmap(Image_Display_Activity.bm);
                Rect wh = binding.cropImageView.getCropRect();
                Image_Display_Activity.iHeight = wh.height();
                Toast.makeText(getApplicationContext(),"Crop Applied",Toast.LENGTH_SHORT).show();
            }
        });


        binding.rotateBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                binding.cropImageView.setRotatedDegrees(i);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });


        binding.cancelCropIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = getBaseContext().getPackageManager()
                        .getLaunchIntentForPackage( getBaseContext().getPackageName() );
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
            }
        });


        binding.saveCropIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                textBit = Image_Display_Activity.bm =  binding.cropImageView.getCroppedImage();
                try{saveImage();}catch (Exception e){e.printStackTrace();}
            }
        });


        binding.fixedAspectRatioCheck.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b){binding.cropImageView.setFixedAspectRatio(true);}
                else{binding.cropImageView.setFixedAspectRatio(false);}
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.rot_right_action,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()){
            case R.id.rotate_right:
                rot+=90;
                if(rot>360){rot-=360;}
                binding.cropImageView.setRotatedDegrees(rot);
                return true;
            case R.id.rotate_left:
                rot+=270;
                if(rot>360){rot-=360;}
                binding.cropImageView.setRotatedDegrees(rot);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void saveImage()throws Exception{

        (Image_Display_Activity.binding.imageDisplay).setImageBitmap(Image_Display_Activity.bm);
        FileOutputStream fOut = null;

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "PNG_"+timeStamp+"_";
        File file2 = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File file = File.createTempFile(imageFileName,".png",file2);

        try{
            fOut = new FileOutputStream(file);
        }catch (Exception e){e.printStackTrace();}
        (Image_Display_Activity.bm).compress(Bitmap.CompressFormat.PNG,100,fOut);
        try{
            fOut.flush();
        }catch (Exception e){e.printStackTrace();}
        try{fOut.close();}catch (IOException e){e.printStackTrace();}
        try{
            MediaStore.Images.Media.insertImage(getContentResolver(),file.getAbsolutePath(),file.getName(),file.getName());}
        catch (FileNotFoundException e){e.printStackTrace();}

        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        Uri cUri = Uri.fromFile(file);
        mediaScanIntent.setData(cUri);
        this.sendBroadcast(mediaScanIntent);
        Toast.makeText(getApplicationContext(),"Image Saved to Pictures",Toast.LENGTH_SHORT).show();

    }
}