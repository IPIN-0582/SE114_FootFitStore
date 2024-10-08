package com.example.footfitstore;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class OnBoard2Activity extends AppCompatActivity {

    Button btnOnboard2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_on_board2);
        initializeComponent();

    }

    private void initializeComponent() {

        btnOnboard2 =findViewById(R.id.btnOnboard2);

        btnOnboard2.setOnClickListener(view -> {
            startActivity(new Intent(OnBoard2Activity.this, OnBoard3Activity.class));
        });

    }
}