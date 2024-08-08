package com.example.databindingjava;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.example.databindingjava.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding activityMainBinding;

    private ClicksReference clickclick;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        activityMainBinding= DataBindingUtil.setContentView(this,R.layout.activity_main);
        activityMainBinding.setCat(getStudent());

        clickclick = new ClicksReference(this);
        activityMainBinding.setClickers(clickclick);

    }

    private Student getStudent(){
        Student student = new Student();
        student.setName("AJIT");
        student.setEmail("sai.ajit@apptmyz.com");
        return student;

    }



    public class ClicksReference{
        Context context;

        public ClicksReference(Context context) {
            this.context = context;
        }

        public void EnrolledButton(View view){
            Toast.makeText(context,"Enroll clicked",Toast.LENGTH_SHORT).show();
        }
        public void CancelButton(View view){
            Toast.makeText(context,"Cancel clicked",Toast.LENGTH_SHORT).show();
        }
    }
}