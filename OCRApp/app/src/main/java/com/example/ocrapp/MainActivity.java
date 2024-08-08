package com.example.ocrapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    Button btn_cap,btn_cop;
    TextView data;

    private static int req_code=100;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btn_cap= findViewById(R.id.btn_capture);
        btn_cop= findViewById(R.id.btn_copy);
        data= findViewById(R.id.data_given);

        if(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA)!= PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(MainActivity.this,new String[]{
                    Manifest.permission.CAMERA
            },req_code);
        }
        btn_cap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                CropImage.
            }
        });
    }
}