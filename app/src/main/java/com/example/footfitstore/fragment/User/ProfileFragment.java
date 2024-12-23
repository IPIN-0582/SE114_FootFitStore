package com.example.footfitstore.fragment.User;

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
import com.example.footfitstore.Utils.CustomDialog;
import com.example.footfitstore.activity.User.EditProfileActivity;
import com.example.footfitstore.activity.User.MainActivity;
import com.example.footfitstore.activity.User.ResetPasswordActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class ProfileFragment extends Fragment {

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private TextView recoveryPassword, etEmail, etUsername;
    private ImageButton btnEditProfile;
    private ImageView btnBack, imgProfilePicture;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        etUsername = view.findViewById(R.id.et_name);
        etEmail = view.findViewById(R.id.et_email);
        recoveryPassword = view.findViewById(R.id.tv_recovery_password);
        btnEditProfile = view.findViewById(R.id.btn_edit_profile);
        btnBack = view.findViewById(R.id.btnBack);
        imgProfilePicture = view.findViewById(R.id.img_profile_picture);

        initializeComponent();

        btnEditProfile.setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), EditProfileActivity.class));
        });

        recoveryPassword.setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), ResetPasswordActivity.class));
        });

        btnBack.setOnClickListener(v -> {
            ExploreFragment exploreFragment = new  ExploreFragment();

            getParentFragmentManager().beginTransaction()
                    .replace(R.id.main_frame, exploreFragment)
                    .addToBackStack(null)
                    .commit();

            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).setSelectedNavItem(R.id.nav_explore);
            }
        });
        return view;
    }
    @Override
    public void onResume() {
        super.onResume();
        initializeComponent();
    }

    private void initializeComponent() {
        FirebaseUser user = mAuth.getCurrentUser();

        if (user != null) {
            etEmail.setText(user.getEmail());
            String userUid = user.getUid();

            mDatabase.child("Users").child(userUid).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    String usernameTextview = (dataSnapshot.child("firstName").getValue(String.class) != null ? dataSnapshot.child("firstName").getValue(String.class) : "")
                            + " " + (dataSnapshot.child("lastName").getValue(String.class) != null ? dataSnapshot.child("lastName").getValue(String.class) : "");
                    etUsername.setText(usernameTextview);
                    int gender = dataSnapshot.child("gender").getValue(Integer.class) != null ? dataSnapshot.child("gender").getValue(Integer.class) : 0;
                    String avatarUrl = dataSnapshot.child("avatarUrl").getValue(String.class);
                    if (avatarUrl != null) {
                        Picasso.get().load(avatarUrl).into(imgProfilePicture);
                    } else
                    {
                        if (gender == 0) {
                            imgProfilePicture.setImageResource(R.drawable.boy);
                        }
                        else {
                            imgProfilePicture.setImageResource(R.drawable.girl);
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    new CustomDialog(requireContext())
                            .setTitle("Failed")
                            .setMessage("Failed From Fetch Data")
                            .setIcon(R.drawable.error)
                            .setPositiveButton("OK", null)
                            .hideNegativeButton()
                            .show();
                }
            });
        }
    }
}