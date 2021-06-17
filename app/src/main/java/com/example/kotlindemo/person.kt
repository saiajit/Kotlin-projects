package com.example.kotlindemo

import java.lang.Exception

class person {
    var name:String? = null
    get() = field
    set(value){
        field = value?.toUpperCase()
    }
    var gender:String? = null
    var age:Int= 0
    get()= field
    set (value)= if(value<18) print("Person is Mio") else field= value
}
