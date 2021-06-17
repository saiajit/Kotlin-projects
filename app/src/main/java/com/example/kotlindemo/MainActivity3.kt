package com.example.kotlindemo

import android.app.Person
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast

class MainActivity3 : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)
        var name = findViewById<EditText>(R.id.et_6_1)
        var gender = findViewById<EditText>(R.id.et_6_2)
        var age = findViewById<EditText>(R.id.et_6_3)
        var btn = findViewById<Button>(R.id.btn_6)

        btn.setOnClickListener(View.OnClickListener {
            val person = person()
            person.name= name.text.toString()
            person.gender= gender.text.toString()
            person.age= age.text.toString().toInt()

            var result= "${person.name}"
            Toast.makeText(this, result, Toast.LENGTH_LONG).show()

        })
    }
}