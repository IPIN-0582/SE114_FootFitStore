package com.example.footfitstore.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.footfitstore.R;
import com.example.footfitstore.activity.Admin.MainActivity_Admin;
import com.example.footfitstore.activity.User.MainActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

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
            FirebaseAuth mAuth = FirebaseAuth.getInstance();
            FirebaseUser user = mAuth.getCurrentUser();
            String uid = user != null ? user.getUid() : null;
            if (mAuth.getCurrentUser() != null && mAuth.getCurrentUser().isEmailVerified()) {
                loginSuccess(uid);
                return;
            }
            startActivity(new Intent(OnBoard3Activity.this, LoginActivity.class));
        });

    }
    private void loginSuccess(String uid) {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(uid).child("role");
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    if (Objects.equals(snapshot.getValue(String.class), "admin")) {
                        startActivity(new Intent(OnBoard3Activity.this, MainActivity_Admin.class));
                        finish();
                    } else {
                        startActivity(new Intent(OnBoard3Activity.this, MainActivity.class));
                        finish();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}