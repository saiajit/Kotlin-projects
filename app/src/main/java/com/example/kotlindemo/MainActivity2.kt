package com.example.kotlindemo

import android.graphics.Color
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.constraint.ConstraintLayout
import android.view.View
import android.widget.Button
import android.widget.Spinner

class MainActivity2 : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main3)

       var layout = findViewById<ConstraintLayout>(R.id.container)
        var spinner= findViewById<Spinner>(R.id.spinner)
        var btn= findViewById<Button>(R.id.button)

        btn.setOnClickListener(View.OnClickListener {
            var color:String = spinner.selectedItem.toString()

            when(color){
                "Red" -> layout.setBackgroundColor(Color.RED)
                "Yellow" -> layout.setBackgroundColor(Color.YELLOW)
                "Blue" -> layout.setBackgroundColor(Color.BLUE)
                "Green" -> layout.setBackgroundColor(Color.GREEN)
            }
        })

    }
}
