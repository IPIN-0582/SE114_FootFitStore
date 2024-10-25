package com.example.footfitstore.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.footfitstore.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class EditProfileActivity extends AppCompatActivity {

    private EditText etFirstName, etLastName, etAddress, etMobileNumber;
    private RadioButton rbMale;
    private ImageView btnBack;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private TextView tvFullName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

// Khởi tạo Firebase Auth và Database
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        // Khởi tạo các view
        etFirstName = findViewById(R.id.et_first_name);
        etLastName = findViewById(R.id.et_last_name);
        etAddress = findViewById(R.id.et_address);
        etMobileNumber = findViewById(R.id.et_mobile_number);
        rbMale = findViewById(R.id.rb_male);
        btnBack = findViewById(R.id.btn_back_profile);
        tvFullName = findViewById(R.id.tv_full_name);

        //Back về trang Profile
        btnBack.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                //startActivity(new Intent(EditProfileActivity.this, MainActivity.class));
                EditProfileActivity.super.onBackPressed();
            }
        });

        //Load toàn bộ dữ liệu từ DB lên và gán vào các thẻ
        setValueActivity();

        // Thiết lập sự kiện khi người dùng nhấn nút "Done"
        findViewById(R.id.btn_done).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveUserProfile();
            }

        });

    }

    private void setValueActivity() {
        String userUid = mAuth.getCurrentUser().getUid();

        mDatabase.child("Users").child(userUid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Lấy dữ liệu từ Realtime Database và gán vào TextView FullName
                String fullName = dataSnapshot.child("firstName").getValue(String.class)
                        + " " + dataSnapshot.child("lastName").getValue(String.class);
                tvFullName.setText(fullName);

                //gán vào firstname
                etFirstName.setText(dataSnapshot.child("firstName").getValue(String.class));

                //gán vào lastname
                etLastName.setText(dataSnapshot.child("lastName").getValue(String.class));

                //gán vào address
                etAddress.setText(dataSnapshot.child("address").getValue(String.class));

                //gán vào number phone
                etMobileNumber.setText(dataSnapshot.child("mobileNumber").getValue(String.class));

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Xử lý lỗi khi không thể truy cập dữ liệu
                Toast.makeText(EditProfileActivity.this, "Lỗi khi lấy dữ liệu", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveUserProfile() {
        // Lấy giá trị từ các trường nhập liệu
        String firstName = etFirstName.getText().toString().trim();
        String lastName = etLastName.getText().toString().trim();
        String address = etAddress.getText().toString().trim();
        String mobileNumber = etMobileNumber.getText().toString().trim();

        // Lấy giá trị giới tính từ RadioGroup
        int gender = rbMale.isChecked() ? 0 : 1; // 0 cho Nam (Male), 1 cho Nữ (Female)

        // Kiểm tra các trường nhập liệu không rỗng
        if (TextUtils.isEmpty(firstName)) {
            etFirstName.setError("First Name is required");
            return;
        }

        if (TextUtils.isEmpty(lastName)) {
            etLastName.setError("Last Name is required");
            return;
        }

        if (TextUtils.isEmpty(address)) {
            etAddress.setError("Address is required");
            return;
        }

        if (TextUtils.isEmpty(mobileNumber)) {
            etMobileNumber.setError("Mobile Number is required");
            return;
        }

        // Lấy người dùng hiện tại
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            String uid = user.getUid();

            // Tạo HashMap để cập nhật thông tin
            HashMap<String, Object> userProfile = new HashMap<>();
            userProfile.put("firstName", firstName);
            userProfile.put("lastName", lastName);
            userProfile.put("address", address);
            userProfile.put("mobileNumber", mobileNumber);
            userProfile.put("gender", gender);

            // Cập nhật thông tin vào Realtime Database
            mDatabase.child("Users").child(uid).updateChildren(userProfile)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(EditProfileActivity.this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(EditProfileActivity.this, "Failed to update profile", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

}