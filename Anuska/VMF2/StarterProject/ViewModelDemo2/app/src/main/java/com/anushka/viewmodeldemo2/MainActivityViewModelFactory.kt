package com.anushka.viewmodeldemo2

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class MainActivityViewModelFactory(private val startTotal: Int):ViewModelProvider.Factory{
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if(modelClass.isAssignableFrom(MainActivityViewModel::class.java)){
            return MainActivityViewModel(startTotal) as T
        }
        throw IllegalArgumentException("Unknowwn")
    }
}