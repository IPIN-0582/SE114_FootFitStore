package com.example.footfitstore;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class OnBoard1Activity extends AppCompatActivity {

    Button btnOnboard1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_on_board1);

        initializeComponent();

    }

    private void initializeComponent() {

        btnOnboard1 =findViewById(R.id.btnOnboard1);

        btnOnboard1.setOnClickListener(view -> {
            startActivity(new Intent(OnBoard1Activity.this, OnBoard2Activity.class));
        });

    }

}