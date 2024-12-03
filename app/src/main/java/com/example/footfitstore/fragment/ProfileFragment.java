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
import com.squareup.picasso.Picasso;

import android.widget.Toast;

public class ProfileFragment extends Fragment {

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private TextView recoveryPassword, etEmail, etUsername;
    private ImageButton btnEditProfile;
    private ImageView btnBack, imgProfilePicture;
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
        btnBack = view.findViewById(R.id.btnBack);
        imgProfilePicture = view.findViewById(R.id.img_profile_picture);

        initializeComponent();

        // Set click listeners
        btnEditProfile.setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), EditProfileActivity.class));
        });

        recoveryPassword.setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), ResetPasswordActivity.class));
        });

        btnBack.setOnClickListener(v -> {
            // Chuyển đổi sang ExploreFragment
            ExploreFragment exploreFragment = new  ExploreFragment();

            // Sử dụng FragmentManager để thay thế fragment hiện tại bằng ExploreFragment
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.main_frame, exploreFragment)  // R.id.main_frame là ID của FrameLayout trong MainActivity
                    .addToBackStack(null)  // Nếu muốn cho phép quay lại
                    .commit();

            // Cập nhật BottomNavigationView
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).setSelectedNavItem(R.id.nav_explore);  // Cập nhật trạng thái bottom nav
            }
        });
        return view;
    }
    @Override
    public void onResume() {
        super.onResume();
        initializeComponent(); // Gọi lại hàm này để làm mới dữ liệu
    }

    private void initializeComponent() {
        FirebaseUser user = mAuth.getCurrentUser();

        if (user != null) {
            etEmail.setText(user.getEmail());
            String userUid = user.getUid();

            // Lấy và hiển thị ảnh đại diện từ Firebase Database
            mDatabase.child("Users").child(userUid).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    // Lấy tên và hiển thị
                    String usernameTextview = dataSnapshot.child("firstName").getValue(String.class)
                            + " " + dataSnapshot.child("lastName").getValue(String.class);
                    etUsername.setText(usernameTextview);

                    // Lấy URL ảnh đại diện từ Database
                    String avatarUrl = dataSnapshot.child("avatarUrl").getValue(String.class);
                    if (avatarUrl != null) {
                        // Sử dụng Picasso để tải ảnh từ URL
                        Picasso.get().load(avatarUrl).placeholder(R.drawable.onboard1).into(imgProfilePicture);
                    } else {
                        // Hiển thị ảnh mặc định nếu không có URL
                        imgProfilePicture.setImageResource(R.drawable.onboard1);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    // Xử lý lỗi khi không thể truy cập dữ liệu
                    Toast.makeText(getActivity(), "Lỗi khi lấy dữ liệu", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}