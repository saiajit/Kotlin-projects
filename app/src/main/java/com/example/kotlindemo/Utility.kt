package com.example.kotlindemo

class Utility {

    fun getQuestion():ArrayList<Questions>{
        var list = ArrayList<Questions>()
        //logic
        var q1 = Questions(1,"what is kotlin","script lang","prog lang","modern lang","comp lang")
        var q2 = Questions(2,"what is C","script lang","prog lang","modern lang","comp lang")
        var q3 = Questions(3,"what is C++","script lang","prog lang","modern lang","comp lang")
        list.add(q1)
        list.add(q2)

        return list
    }
}