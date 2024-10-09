package com.example.footfitstore;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class OnBoard3Activity extends AppCompatActivity {

    Button btnOnboard3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_on_board3);


        initializeComponent();

    }

    private void initializeComponent() {

        btnOnboard3 =findViewById(R.id.btnOnboard3);

        btnOnboard3.setOnClickListener(view -> {
            startActivity(new Intent(OnBoard3Activity.this, LoginActivity.class));
        });

    }
}