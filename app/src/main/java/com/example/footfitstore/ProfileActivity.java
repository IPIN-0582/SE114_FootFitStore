package com.example.footfitstore;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import android.widget.Toast;

import java.util.HashMap;


public class ProfileActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private TextView recoveryPassword, etEmail, etUsername;
    private ImageView btnEditProfile;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mAuth = FirebaseAuth.getInstance();

        mDatabase = FirebaseDatabase.getInstance().getReference();

        etUsername = findViewById(R.id.et_name);
        etEmail = findViewById(R.id.et_email);
        recoveryPassword = findViewById(R.id.tv_recovery_password);
        btnEditProfile = findViewById(R.id.btn_edit_profile_picture);

        initializeConponent();

        btnEditProfile.setOnClickListener(view -> {
            startActivity(new Intent(ProfileActivity.this, EditProfileActivity.class));
        });

        recoveryPassword.setOnClickListener(view -> {
            startActivity(new Intent(ProfileActivity.this, ResetPasswordActivity.class));
        });



    }

    private void initializeConponent() {

        FirebaseUser user = mAuth.getCurrentUser();

        etEmail.setText(user.getEmail());

        String userUid = user.getUid();

        mDatabase.child("Users").child(userUid).addListenerForSingleValueEvent(new ValueEventListener() {
           @Override
           public void onDataChange(DataSnapshot dataSnapshot) {
                   // Lấy dữ liệu từ Realtime Database và gán vào EditText UserName
                   String usernameTextview = dataSnapshot.child("firstName").getValue(String.class)
                           + " " + dataSnapshot.child("lastName").getValue(String.class);
                   etUsername.setText(usernameTextview);

           }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Xử lý lỗi khi không thể truy cập dữ liệu
                Toast.makeText(ProfileActivity.this, "Lỗi khi lấy dữ liệu", Toast.LENGTH_SHORT).show();
            }
        });

    }

}