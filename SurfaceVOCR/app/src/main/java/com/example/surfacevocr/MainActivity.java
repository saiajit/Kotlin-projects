package com.example.surfacevocr;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Camera;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;
import com.google.android.material.badge.BadgeUtils;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

     private SurfaceView surfaceView;
     private TextView textView;

     private CameraSource cm;
     private ImageView iv;
     private Button captureButton;

//     private Camera mCam;
private  static final int Camcode=100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        surfaceView = findViewById(R.id.camera_preview);
        textView = findViewById(R.id.text);
        iv = findViewById(R.id.imageView);
//        Button captureButton = findViewById(R.id.captureButton);
//        captureButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                cm.takePicture(null, pictureCallback);
//            }
//        });

        CameraOpened();

//        cm.setFocusMode(Camera.Parameters.FOCUS_MODE_MACRO); //If you want to change the focus mode manually
//        surfaceView.getHolder().addCallback(this);

    }

    private void CameraOpened(){
        final TextRecognizer TR = new TextRecognizer.Builder(this).build();

        if(!TR.isOperational()){
            Log.w("Myy Tag", "Dependencies Nott Loaded");
        }else {
            cm= new CameraSource.Builder(this,TR)
                    .setFacing(CameraSource.CAMERA_FACING_BACK).setRequestedPreviewSize(1280,1024)
                    .setAutoFocusEnabled(true).setRequestedFps(2.0f).build();

            surfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
                @Override
                public void surfaceCreated(@NonNull SurfaceHolder holder) {
                    try {
                        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA)!= PackageManager.PERMISSION_GRANTED){
                            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CAMERA},Camcode );
                            return;
                        }
                        cm.start(surfaceView.getHolder());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {

                    //Release
                }

                @Override
                public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
                    cm.stop();
                }
            });
            TR.setProcessor(new Detector.Processor<TextBlock>() {
                @Override
                public void release() {
                    //Detect
                }

                @Override
                public void receiveDetections(@NonNull Detector.Detections<TextBlock> detections) {
                    final SparseArray<TextBlock> items= detections.getDetectedItems();
                    if(items.size()!=0){
                        textView.post(new Runnable() {
                            @Override
                            public void run() {
                                StringBuilder stringBuilder = new StringBuilder();
                                for (int i=0;i<items.size();i++){
                                    TextBlock item = items.valueAt(i);
                                    stringBuilder.append(item.getValue());
                                    stringBuilder.append("\n");
                                }
                                textView.setText(stringBuilder.toString());
                            }
                        });
                    }
                }
            });

        }
    }
    private CameraSource.PictureCallback pictureCallback = new CameraSource.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] bytes) {
            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            iv.setImageBitmap(bitmap);
            // Save the bitmap to a file or display it in an ImageView
//            cm.takePicture(null,pictureCallback);
        }
    };

}