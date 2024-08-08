package com.example.ebookshop.MODEL;

import androidx.databinding.BaseObservable;

public class Category extends BaseObservable {
    private int id;
    private String category;
    private String categoryDesc;

    public Category(){

    }

    public Category(int id, String category, String categoryDesc) {
        this.id = id;
        this.category = category;
        this.categoryDesc = categoryDesc;
    }
}
