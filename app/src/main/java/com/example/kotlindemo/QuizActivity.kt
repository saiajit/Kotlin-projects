package com.example.kotlindemo

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import org.w3c.dom.Text

class QuizActivity : AppCompatActivity(), View.OnClickListener {

    private var listOfQuestions: ArrayList<Questions>? = null
    var currentPosition = 1
    lateinit var question: TextView
    lateinit var opt1: TextView
    lateinit var opt2: TextView
    lateinit var opt3: TextView
    lateinit var opt4: TextView
    lateinit var submitBtn: Button
    var selectedChoice: Int = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_quiz)
        var utility = Utility()
        listOfQuestions = utility.getQuestion()

        var name: String? = intent.getStringExtra("name")
        var q_name = findViewById<TextView>(R.id.q_name)
        q_name.text = name


        var question = findViewById<TextView>(R.id.q_question)
        var opt1 = findViewById<TextView>(R.id.tv_op_1)
        var opt2 = findViewById<TextView>(R.id.tv_op_2)
        var opt3 = findViewById<TextView>(R.id.tv_op_3)
        var opt4 = findViewById<TextView>(R.id.tv_op_4)
        submitBtn = findViewById(R.id.q_btn_nxt)

        setQuestion()

        opt1.setOnClickListener(this)
        opt2.setOnClickListener(this)
        opt3.setOnClickListener(this)
        opt4.setOnClickListener(this)

        submitBtn.setOnClickListener(View.OnClickListener {
            var ans = listOfQuestions!!.get(currentPosition - 1).answer
            checkAnswer(ans, selectedChoice)
        })
    }

    private fun checkAnswer(ans: Int, id: Int) {
        when (ans) {
            1 -> {
                Log.i("---->", id.toString())
                if (ans == id)
                    opt1.background =
                        ContextCompat.getDrawable(this, R.drawable.default_option_border_bd)
                else
                    opt1.background =
                        ContextCompat.getDrawable(this, R.drawable.wrong_option_border_bd)
            }
            2 -> {
                if (ans == id)
                    opt2.background =
                        ContextCompat.getDrawable(this, R.drawable.default_option_border_bd)
                else
                    opt2.background =
                        ContextCompat.getDrawable(this, R.drawable.wrong_option_border_bd)
            }
            3 -> {
                if (ans == id)
                    opt3.background =
                        ContextCompat.getDrawable(this, R.drawable.default_option_border_bd)
                else
                    opt3.background =
                        ContextCompat.getDrawable(this, R.drawable.wrong_option_border_bd)
            }
            4 -> {
                if (ans == id)
                    opt4.background =
                        ContextCompat.getDrawable(this, R.drawable.default_option_border_bd)
                else
                    opt4.background =
                        ContextCompat.getDrawable(this, R.drawable.wrong_option_border_bd)

            }


        }

    }


    fun setQuestion() {
        var questionObject = listOfQuestions!!.get(currentPosition - 1)

        question.text = questionObject.question


        opt1.text = questionObject.opt1


        opt2.text = questionObject.opt2


        opt3.text = questionObject.opt3


        opt4.text = questionObject.opt4


    }

    override fun onClick(v: View) {
        //logic
        when(v.id){
            R.id.tv_op_1 ->{
                selectedOption(opt1,R.drawable.select_option_border_bd)
            }
            R.id.tv_op_2 ->{
                selectedOption(opt2,R.drawable.select_option_border_bd)
            }
            R.id.tv_op_3->{
                selectedOption(opt3,R.drawable.select_option_border_bd)
            }
            R.id.tv_op_4 ->{
                selectedOption(opt4,R.drawable.select_option_border_bd)
            }
        }
    }

    private fun selectedOption(tv: TextView,id:Int){
        defaultOption();
        tv.background = ContextCompat.getDrawable(this,id)
        selectedChoice = id;
    }

    private fun defaultOption(){
        opt1.background = ContextCompat.getDrawable(this,R.drawable.default_option_border_bd)
        opt2.background = ContextCompat.getDrawable(this,R.drawable.default_option_border_bd)
        opt3.background = ContextCompat.getDrawable(this,R.drawable.default_option_border_bd)
        opt4.background = ContextCompat.getDrawable(this,R.drawable.default_option_border_bd)
    }

}


