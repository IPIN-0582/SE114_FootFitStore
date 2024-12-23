package com.example.footfitstore.activity.User;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.footfitstore.R;
import com.example.footfitstore.Utils.CustomDialog;
import com.example.footfitstore.activity.LoginActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

public class EditProfileActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;

    private EditText etFirstName, etLastName, etAddress, etMobileNumber;
    private RadioButton rbMale;
    private ImageView btnBack, btnEditAvatar, imgProfilePicture;
    private TextView tvFullName, btnDone;
    private Uri avatarUri;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private boolean isAvatarChanged;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        etFirstName = findViewById(R.id.et_first_name);
        etLastName = findViewById(R.id.et_last_name);
        etAddress = findViewById(R.id.et_address);
        etMobileNumber = findViewById(R.id.et_mobile_number);
        rbMale = findViewById(R.id.rb_male);
        btnBack = findViewById(R.id.btn_back_profile);
        tvFullName = findViewById(R.id.tv_full_name);
        btnEditAvatar = findViewById(R.id.btn_edit_profile_picture);
        btnDone = findViewById(R.id.btn_done);
        imgProfilePicture = findViewById(R.id.img_profile_picture);

        btnBack.setOnClickListener(view -> finish());

        setValueActivity();

        btnDone.setOnClickListener(v -> {
            new CustomDialog(EditProfileActivity.this)
                    .setTitle("Warning")
                    .setMessage("Are You Sure To Change Your Profile?")
                    .setIcon(R.drawable.warning)
                    .setPositiveButton("OK", new CustomDialog.OnDialogClickListener() {
                        @Override
                        public void onPositiveClick() {
                            saveUserProfile();
                        }

                        @Override
                        public void onNegativeClick() {

                        }
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });
        btnEditAvatar.setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "Select Avatar"), PICK_IMAGE_REQUEST);
            isAvatarChanged = true;
        });
    }

    private void setValueActivity() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            new CustomDialog(EditProfileActivity.this)
                    .setTitle("Load Information Failed")
                    .setMessage("User hasn't logged in")
                    .setIcon(R.drawable.error)
                    .setPositiveButton("OK", null)
                    .hideNegativeButton()
                    .show();
            finish();
            return;
        }

        String userUid = user.getUid();
        mDatabase.child("Users").child(userUid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String firstName = dataSnapshot.child("firstName").getValue(String.class) != null ? dataSnapshot.child("firstName").getValue(String.class) : "";
                String lastName = dataSnapshot.child("lastName").getValue(String.class) != null ? dataSnapshot.child("lastName").getValue(String.class) : "";
                int gender = dataSnapshot.child("gender").getValue(Integer.class) != null ? dataSnapshot.child("gender").getValue(Integer.class) : 0;
                String fullName = firstName + " " + lastName;
                tvFullName.setText(fullName);
                etFirstName.setText(dataSnapshot.child("firstName").getValue(String.class));
                etLastName.setText(dataSnapshot.child("lastName").getValue(String.class));
                etAddress.setText(dataSnapshot.child("address").getValue(String.class));
                etMobileNumber.setText(dataSnapshot.child("mobileNumber").getValue(String.class));
                if (dataSnapshot.child("gender").getValue(Integer.class) != null) {
                    rbMale.setChecked(dataSnapshot.child("gender").getValue(Integer.class) == 0);
                }

                String avatarUrl = dataSnapshot.child("avatarUrl").getValue(String.class);
                if (avatarUrl != null) {
                    Picasso.get().load(avatarUrl).into(imgProfilePicture);
                }
                else
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
                new CustomDialog(EditProfileActivity.this)
                        .setTitle("Failed")
                        .setMessage("Failed To Fetch Data.")
                        .setIcon(R.drawable.error)
                        .setPositiveButton("OK", null)
                        .hideNegativeButton()
                        .show();
            }
        });
    }

    private void saveUserProfile() {
        String firstName = etFirstName.getText().toString().trim();
        String lastName = etLastName.getText().toString().trim();
        String address = etAddress.getText().toString().trim();
        String mobileNumber = etMobileNumber.getText().toString().trim();
        int gender = rbMale.isChecked() ? 0 : 1;

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
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            String uid = user.getUid();
            HashMap<String, Object> userProfile = new HashMap<>();
            userProfile.put("firstName", firstName);
            userProfile.put("lastName", lastName);
            userProfile.put("address", address);
            userProfile.put("mobileNumber", mobileNumber);
            userProfile.put("gender", gender);
        if (isAvatarChanged) {
            uploadAvatar(user.getUid(), new uploadImageCallback() {
                @Override
                public void success(String imageUrl) {
                    userProfile.put("avatarUrl", imageUrl);
                    mDatabase.child("Users").child(user.getUid()).updateChildren(userProfile)
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    new CustomDialog(EditProfileActivity.this)
                                            .setTitle("Success")
                                            .setMessage("Successfully Update Profile.")
                                            .setIcon(R.drawable.congrat)
                                            .setPositiveButton("OK", new CustomDialog.OnDialogClickListener() {
                                                @Override
                                                public void onPositiveClick() {
                                                    finish();
                                                }

                                                @Override
                                                public void onNegativeClick() {

                                                }
                                            })
                                            .hideNegativeButton()
                                            .show();
                                } else {
                                    new CustomDialog(EditProfileActivity.this)
                                            .setTitle("Success")
                                            .setMessage("Successfully Update Profile.")
                                            .setIcon(R.drawable.congrat)
                                            .setPositiveButton("OK", new CustomDialog.OnDialogClickListener() {
                                                @Override
                                                public void onPositiveClick() {
                                                    finish();
                                                }

                                                @Override
                                                public void onNegativeClick() {

                                                }
                                            })
                                            .hideNegativeButton()
                                            .show();                           }
                            });
                }

                @Override
                public void failure(String errorMessage) {

                }
            });
        }
        else
        {
            mDatabase.child("Users").child(uid).updateChildren(userProfile)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            new CustomDialog(EditProfileActivity.this)
                                    .setTitle("Success")
                                    .setMessage("Successfully Update Profile.")
                                    .setIcon(R.drawable.congrat)
                                    .setPositiveButton("OK", new CustomDialog.OnDialogClickListener() {
                                        @Override
                                        public void onPositiveClick() {
                                            finish();
                                        }

                                        @Override
                                        public void onNegativeClick() {

                                        }
                                    })
                                    .hideNegativeButton()
                                    .show();
                        } else {
                            new CustomDialog(EditProfileActivity.this)
                                    .setTitle("Failed")
                                    .setMessage("Failed To Update Profile.")
                                    .setIcon(R.drawable.error)
                                    .setPositiveButton("OK", null)
                                    .hideNegativeButton()
                                    .show();
                        }
                    });
        }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            avatarUri = data.getData();
            imgProfilePicture.setImageURI(avatarUri);
        }
    }

    private void uploadAvatar(String uid,uploadImageCallback callback)
    {
        {
            if (avatarUri != null) {
                ProgressDialog progressDialog = new ProgressDialog(this);
                progressDialog.setMessage("Uploading avatar...");
                progressDialog.show();
                StorageReference storageRef = FirebaseStorage.getInstance().getReference("avatars/" + uid + ".jpg");
                storageRef.putFile(avatarUri)
                    .addOnSuccessListener(taskSnapshot -> storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        callback.success(uri.toString());
                    }))
                    .addOnFailureListener(e -> {
                        progressDialog.dismiss();
                        callback.failure("Failed to update avatar");
                        new CustomDialog(EditProfileActivity.this)
                                .setTitle("Failed")
                                .setMessage("Failed To Upload Image.")
                                .setIcon(R.drawable.error)
                                .setPositiveButton("OK", null)
                                .hideNegativeButton()
                                .show();
                    });
            } else {
                new CustomDialog(EditProfileActivity.this)
                        .setTitle("Failed")
                        .setMessage("No Avatar Selected.")
                        .setIcon(R.drawable.error)
                        .setPositiveButton("OK", null)
                        .hideNegativeButton()
                        .show();
            }
        }
    }
    public interface uploadImageCallback {
        void success(String imageUrl);
        void failure(String errorMessage);
    }
}
