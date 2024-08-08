package com.example.text_detection;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
//import android.os.Bundle;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Toast;

import com.example.text_detection.databinding.ActivityMainBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;

import java.util.BitSet;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ){
            requestPermissions(new String[]{Manifest.permission.CAMERA},101);
        }
    }

    public void doProcess(View view) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent,101);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //processs img to extrract data
        Bundle bundle= data.getExtras();
        Bitmap bitmap= (Bitmap) bundle.get("data");
        binding.imageId.setImageBitmap(bitmap);

        FirebaseVisionImage firebaseVisionImage = FirebaseVisionImage.fromBitmap(bitmap);

        FirebaseVision firebaseVision = FirebaseVision.getInstance();

        FirebaseVisionTextRecognizer firebaseVisionTextRecognizer = firebaseVision.getOnDeviceTextRecognizer();

        Task<FirebaseVisionText> task = firebaseVisionTextRecognizer.processImage(firebaseVisionImage);

         task.addOnSuccessListener(new OnSuccessListener<FirebaseVisionText>() {
             @Override
             public void onSuccess(FirebaseVisionText firebaseVisionText) {
                 String s=firebaseVisionText.getText();
                 binding.textId.setText(s);
             }
         });

         task.addOnFailureListener(new OnFailureListener() {
             @Override
             public void onFailure(@NonNull Exception e) {
                 Toast.makeText(getApplicationContext(),e.getMessage(),Toast.LENGTH_LONG).show();
             }
         });
    }
}