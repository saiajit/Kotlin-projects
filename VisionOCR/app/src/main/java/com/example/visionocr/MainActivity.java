package com.example.visionocr;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.SparseArray;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;

public class MainActivity extends AppCompatActivity {

    Button click;
    TextView stext;
    ImageView simage;
    Activity activity;

    private static final int reqCode = 10;
    private static final int pCode = 100;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        activity = MainActivity.this;

        click = findViewById(R.id.button);
        stext = findViewById(R.id.textView);
        simage = findViewById(R.id.imageView);

        click.setOnClickListener(v -> {
            if(checkSelfPermission(Manifest.permission.CAMERA)!= PackageManager.PERMISSION_GRANTED){
                requestPermissions(new String[]{Manifest.permission.CAMERA},pCode);
            }else{
                Intent cameraI =  new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//                cameraI.putExtra("android.intent.extra.camera.FOCUS_MODE","auto");
                startActivityForResult(cameraI, reqCode);
            }
        });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == pCode){
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Toast.makeText(activity,"Granted",Toast.LENGTH_SHORT).show();
            }
             else {
                Toast.makeText(activity,"NOT Granted",Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == reqCode && resultCode == Activity.RESULT_OK){
            Bitmap image = (Bitmap) data.getExtras().get("data");
            simage.setImageBitmap(image);
            textDetect();
        }
    }
    public void textDetect(){
        TextRecognizer tRec = new TextRecognizer.Builder(activity).build();
        Bitmap bitmap = ((BitmapDrawable) simage.getDrawable()).getBitmap();

        Frame frame = new Frame.Builder().setBitmap(bitmap).build();

        SparseArray<TextBlock> sparseArray = tRec.detect(frame);

        StringBuilder sBuilder = new StringBuilder();

        for (int i=0; i<sparseArray.size();i++){
            TextBlock textBlock = sparseArray.get(i);
            String str = textBlock.getValue();
            sBuilder.append(str);
        }

        stext.setText(sBuilder);
    }
}