package com.example.footfitstore;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import androidx.annotation.NonNull;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import android.widget.Toast;



public class ProfileActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private EditText etUsername, etEmail;
    private TextView recoveryPassword;
    private Button btnSave;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mAuth = FirebaseAuth.getInstance();

        etUsername = findViewById(R.id.et_name);
        etEmail = findViewById(R.id.et_email);
        recoveryPassword = findViewById(R.id.tv_recovery_password);
        btnSave = findViewById(R.id.btn_save);

        recoveryPassword.setOnClickListener(view -> {
            startActivity(new Intent(ProfileActivity.this, ResetPasswordActivity.class));
        });

        initializeConponent();

    }

    private void initializeConponent() {

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String username = etUsername.getText().toString().trim();
                String email = etEmail.getText().toString().trim();


                FirebaseUser user = mAuth.getCurrentUser();
                if(user != null) {

                    user.updateEmail(email)
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    Toast.makeText(ProfileActivity.this, "Email đã được cập nhật", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(ProfileActivity.this, "Cập nhật email thất bại", Toast.LENGTH_SHORT).show();
                                }
                            });
                }

            }
        });

    }

}