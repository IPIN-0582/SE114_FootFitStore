package com.example.footfitstore.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.footfitstore.R;
import com.example.footfitstore.activity.EditProfileActivity;
import com.example.footfitstore.activity.MainActivity;
import com.example.footfitstore.activity.ResetPasswordActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import android.widget.Toast;

public class ProfileFragment extends Fragment {

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private TextView recoveryPassword, etEmail, etUsername;
    private ImageButton btnEditProfile;
    private ImageView btnBack;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        // Initialize Firebase Auth and Database reference
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Find views
        etUsername = view.findViewById(R.id.et_name);
        etEmail = view.findViewById(R.id.et_email);
        recoveryPassword = view.findViewById(R.id.tv_recovery_password);
        btnEditProfile = view.findViewById(R.id.btn_edit_profile);
        btnBack = view.findViewById(R.id.btn_back);

        initializeComponent();

        // Set click listeners
        btnEditProfile.setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), EditProfileActivity.class));
        });

        recoveryPassword.setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), ResetPasswordActivity.class));
        });

        btnBack.setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), MainActivity.class));
        });

        return view;
    }

    private void initializeComponent() {
        FirebaseUser user = mAuth.getCurrentUser();

        if (user != null) {
            etEmail.setText(user.getEmail());
            String userUid = user.getUid();

            // Fetch data from Firebase Realtime Database and set to Username
            mDatabase.child("Users").child(userUid).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    // Get the user's first and last name from the database
                    String usernameTextview = dataSnapshot.child("firstName").getValue(String.class)
                            + " " + dataSnapshot.child("lastName").getValue(String.class);
                    etUsername.setText(usernameTextview);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    // Handle the error when data cannot be retrieved
                    Toast.makeText(getActivity(), "Lỗi khi lấy dữ liệu", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}