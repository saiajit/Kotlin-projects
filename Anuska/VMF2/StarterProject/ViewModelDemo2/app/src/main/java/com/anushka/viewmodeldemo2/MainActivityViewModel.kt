package com.anushka.viewmodeldemo2

import androidx.lifecycle.ViewModel

class MainActivityViewModel(startTotal :Int) : ViewModel() {
    private var total = 0
    init {
        total = startTotal
    }
    fun getTotal():Int{
        return total
    }

    fun setTotal(input:Int){
        total +=input
    }
}