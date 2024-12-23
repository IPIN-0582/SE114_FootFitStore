package com.example.footfitstore.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.example.footfitstore.R;

public class FootFitActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_foot_fit);

        new Handler().postDelayed(()->{
            Intent intent = new Intent(FootFitActivity.this, OnBoard1Activity.class);
            startActivity(intent);
            finish();
        },1500);
    }
}