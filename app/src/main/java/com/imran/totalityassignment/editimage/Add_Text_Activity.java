package com.imran.totalityassignment.editimage;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.imran.totalityassignment.R;
import com.imran.totalityassignment.databinding.ActivityAddTextBinding;
import com.imran.totalityassignment.databinding.ActivityImageDisplayBinding;
import com.imran.totalityassignment.databinding.CustomTextDialogBinding;
import com.jaredrummler.android.colorpicker.ColorPickerDialog;
import com.jaredrummler.android.colorpicker.ColorPickerDialogListener;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Add_Text_Activity extends AppCompatActivity implements ColorPickerDialogListener {
    private ActivityAddTextBinding binding;
    private DrawingView dv;
    private Paint mPaint;
    float Esize = 105;
    private float vH=0,vW=0;
    boolean flg=false;
    private String userInputValue = "";
    int colorCode = 0xFFFF0000;
    private static final int DIALOG_ID = 0;
    Bitmap textBit = Image_Display_Activity.bm;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding= ActivityAddTextBinding.inflate(getLayoutInflater());
        View decorView = getWindow().getDecorView();
        // Hide the status bar.
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
        setContentView(binding.getRoot());
        getSupportActionBar().hide();

        binding.sizeBar.setEnabled(false);

        float targetW = getIntent().getExtras().getFloat("width");
        float targetH = getIntent().getExtras().getFloat("height");

        {
            vH = targetH*(0.89f);
            vW = (targetH*(0.89f) / ((Image_Display_Activity.bm).getHeight())) * ((Image_Display_Activity.bm).getWidth());
            if(vW>targetW){
                vW = targetW;
                vH=(targetW/((Image_Display_Activity.bm).getWidth()))*((Image_Display_Activity.bm).getHeight());
            }
        }

        dv = new DrawingView(this);
        dv.setBackground(new BitmapDrawable(getResources(),Image_Display_Activity.bm));

        dv.setLayoutParams(new ViewGroup.LayoutParams((int)vW,(int)vH));
        binding.viewDrawingPad.addView(dv);
        binding.dragMsg.setVisibility(View.INVISIBLE);

        binding.addTextOption.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveBitmap();
                userInputValue="";
                dv.invalidate();
                dv.setBackground(new BitmapDrawable(getResources(),textBit));
                flg=true;
                displayEnterTextDialog();
                TextView dragMsg = (TextView)findViewById(R.id.dragMsg);
                dragMsg.setVisibility(View.VISIBLE);
            }
        });
        binding.colorOptionDraw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ColorPickerDialog.newBuilder()
                        .setDialogType(ColorPickerDialog.TYPE_CUSTOM)
                        .setAllowPresets(false)
                        .setDialogId(DIALOG_ID)
                        .setColor(colorCode)
                        .setShowAlphaSlider(true)
                        .show(Add_Text_Activity.this);
            }
        });
        binding.saveOptionDraw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try{saveImage();}catch (Exception e){e.printStackTrace();}
            }
        });
        binding.cancelOptionDraw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = getBaseContext().getPackageManager()
                        .getLaunchIntentForPackage( getBaseContext().getPackageName() );
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
            }
        });
        binding.saveChangesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveBitmap();
                Image_Display_Activity.bm = textBit;
                (Image_Display_Activity.binding.imageDisplay).setImageBitmap(Image_Display_Activity.bm);
                Image_Display_Activity.iHeight = textBit.getHeight();
                Toast.makeText(getApplicationContext(),"Changes Applied",Toast.LENGTH_SHORT).show();
            }
        });
        binding.clearTextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                (dv.mCanvas).drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                userInputValue="";
                dv.invalidate();
            }
        });
        binding.sizeBar.setProgress(50);
        binding.sizeBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
                Esize = progress*(2.1f);
                dv.invalidate();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }
    //Function to save image to a file
    private void saveImage()throws Exception{
        saveBitmap();
        Image_Display_Activity.bm = textBit;
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

        //notify gallery to include saved image to its list
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        Uri cUri = Uri.fromFile(file);
        mediaScanIntent.setData(cUri);
        this.sendBroadcast(mediaScanIntent);
        Toast.makeText(getApplicationContext(),"Image Saved to Pictures",Toast.LENGTH_SHORT).show();

        dv.invalidate();
    }

    //Set color of text after selection
    @Override
    public void onColorSelected(int dialogid,int color){
        colorCode = color;
        dv.invalidate();
    }
    @Override
    public void onDialogDismissed(int dialogid){}

    //Save Bitmap for further editing
    private void saveBitmap(){
        Bitmap bitmap = Bitmap.createBitmap(dv.getWidth(),dv.getHeight(),Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bitmap);
        dv.draw(c);
        textBit = bitmap;
    }
    //Display textbox to get String input
    private void displayEnterTextDialog(){
        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        //final View dialogView = inflater.inflate(R.layout.custom_text_dialog,null);
        CustomTextDialogBinding binding=CustomTextDialogBinding.inflate(LayoutInflater.from(this));
        dialogBuilder.setView(binding.getRoot());
        //final EditText textContent = (EditText)dialogView.findViewById(R.id.add_text_on_image);
        dialogBuilder.setTitle("");
        dialogBuilder.setMessage("Enter Text: ");
        dialogBuilder.setPositiveButton("Done", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                userInputValue = binding.addTextOnImage.getText().toString();
                dv.invalidate();
                if(!userInputValue.equals("") || !userInputValue.isEmpty()){
                    ((SeekBar)findViewById(R.id.sizeBar)).setEnabled(true);
                    dv.invalidate();
                }
            }
        });
        dialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        AlertDialog b = dialogBuilder.create();
        b.show();
    }
    public class DrawingView extends View {


        private Bitmap mBitmap;
        private Canvas mCanvas;
        private Paint mBitmapPaint;
        Context context;
        float xPos=0,yPos=0;

        public DrawingView(Context c){
            super(c);
            context = c;
            mBitmapPaint = new Paint(Paint.DITHER_FLAG);
            mPaint = new Paint();
            mPaint.setColor(colorCode);
            mPaint.setAntiAlias(true);
            mPaint.setDither(true);
            mPaint.setStyle(Paint.Style.FILL);
            mPaint.setTextSize(Esize);
        }
        @Override
        protected void onSizeChanged(int w,int h,int oldw,int oldh){
            super.onSizeChanged((int)vW,(int)vH,oldw,oldh);
            mBitmap = Bitmap.createBitmap((int)vW,(int)vH, Bitmap.Config.ARGB_8888);
            mCanvas = new Canvas(mBitmap);
            xPos = (mCanvas.getWidth()/2)-2;
            yPos = (int)((mCanvas.getHeight()/2)-((mPaint.descent()+mPaint.ascent())/2));
        }

        @Override
        protected void onDraw(Canvas canvas){
            super.onDraw(canvas);

            canvas.drawBitmap(mBitmap,0,0,mBitmapPaint);
            mPaint.setTextSize(Esize);
            mPaint.setColor(colorCode);
            if(flg) {
                canvas.drawText(userInputValue, xPos, yPos, mPaint);
            }
        }
        private float mX,mY;
        private static final float TOUCH_TOLERANCE = 4;

        private void touch_start(float x,float y){

            mX = x;
            mY = y;
            xPos=x;
            yPos=y;
        }

        private void touch_move(float x,float y){
            float dx = Math.abs(x-mX);
            float dy = Math.abs(y-mY);
            if(dx>=TOUCH_TOLERANCE || dy>=TOUCH_TOLERANCE){

                mX=x;
                mY=y;
                xPos=x;
                yPos=y;
            }
        }
        @Override
        public boolean onTouchEvent(MotionEvent event){
            float x = event.getX();
            float y = event.getY();

            switch(event.getAction()){
                case MotionEvent.ACTION_DOWN:
                    touch_start(x,y);
                    invalidate();
                    break;
                case MotionEvent.ACTION_MOVE:
                    touch_move(x,y);
                    invalidate();
                    break;

            }

            return true;
        }


    }
}