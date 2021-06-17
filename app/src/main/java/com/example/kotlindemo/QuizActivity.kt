package com.example.kotlindemo

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import org.w3c.dom.Text

class QuizActivity : AppCompatActivity() {

    private var listOfQuestions: ArrayList<Questions>? = null
    var currentPosition=1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_quiz)
        var utility= Utility()
        listOfQuestions = utility.getQuestion()


        setQuestion();
    }
    fun setQuestion(){
        var questionObject = listOfQuestions!!.get(currentPosition-1)
        var question= findViewById<TextView>(R.id.q_question)
            question.text= questionObject.question

        var opt1= findViewById<TextView>(R.id.tv_op_1)
        opt1.text = questionObject.opt1

        var opt2= findViewById<TextView>(R.id.tv_op_2)
        opt2.text = questionObject.opt2

        var opt3= findViewById<TextView>(R.id.tv_op_3)
        opt3.text = questionObject.opt3

        var opt4= findViewById<TextView>(R.id.tv_op_4)
        opt4.text = questionObject.opt4


    }
}