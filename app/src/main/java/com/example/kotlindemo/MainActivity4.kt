package com.example.kotlindemo

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast

class MainActivity4 : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main4)

        var q_btn = findViewById<Button>(R.id.button7)
        var q_name= findViewById<EditText>(R.id.stu_id)

        q_btn.setOnClickListener(View.OnClickListener {
            Log.i("-----", "Hello")
            if (q_name.toString().isEmpty()){
                Toast.makeText(this@MainActivity4,"Please enter name",Toast.LENGTH_SHORT).show()
            }
            else{
                var next:Intent = Intent(this,QuizActivity::class.java)
                startActivity(next)
                finish()
            }
        })
    }
}