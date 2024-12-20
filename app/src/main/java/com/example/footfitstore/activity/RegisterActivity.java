package com.example.footfitstore.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.footfitstore.R;
import com.example.footfitstore.Utils.CustomDialog;
import com.example.footfitstore.activity.User.MainActivity;
import com.example.footfitstore.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RegisterActivity extends AppCompatActivity {
    private EditText usernameRegister, emailRegister, passwordRegister;
    private Button btnRegister;
    private TextView tvLogin;
    private FirebaseAuth mAuth;

    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("Users"); // Firebase Realtime Database

        emailRegister = findViewById(R.id.emailRegister);
        usernameRegister = findViewById(R.id.usernameRegister);
        passwordRegister = findViewById(R.id.passwordRegister);
        btnRegister = findViewById(R.id.btnRegister);
        tvLogin = findViewById(R.id.tvLogin);

        btnRegister.setOnClickListener(v -> registerUser());

        tvLogin.setOnClickListener(v -> {
            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
        });
    }

    private void registerUser() {
        String email = emailRegister.getText().toString().trim();
        String username = usernameRegister.getText().toString();
        String password = passwordRegister.getText().toString().trim();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password) || TextUtils.isEmpty(username)) {
            new CustomDialog(RegisterActivity.this)
                    .setTitle("Register Failed")
                    .setMessage("Please fill all fields")
                    .setIcon(R.drawable.error)
                    .setPositiveButton("OK", null)
                    .hideNegativeButton()
                    .show();
            return;
        }



        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {

                    if (task.isSuccessful()) {

                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                        if (firebaseUser != null) {

                            String uid = firebaseUser.getUid();
                            // Lưu thông tin người dùng vào Firebase Realtime Database
                            User user = new User(email); // Tạo đối tượng người dùng
                            databaseReference.child(uid).setValue(user)
                                    .addOnCompleteListener(task1 -> {
                                        if (task1.isSuccessful()) {
                                            new CustomDialog(RegisterActivity.this)
                                                    .setTitle("Register Failed")
                                                    .setMessage("Registration Successful")
                                                    .setIcon(R.drawable.done)
                                                    .setPositiveButton("OK", null)
                                                    .hideNegativeButton()
                                                    .show();

                                            startActivity(new Intent(RegisterActivity.this, MainActivity.class));
                                            finish();
                                        } else {
                                            new CustomDialog(RegisterActivity.this)
                                                    .setTitle("Register Failed")
                                                    .setMessage("Failed to save user info. Please try again.")
                                                    .setIcon(R.drawable.error)
                                                    .setPositiveButton("OK", null)
                                                    .hideNegativeButton()
                                                    .show();
                                        }
                                    });
                            databaseReference.child(uid).child("role").setValue("user");
                        }
                }

                    else {
                        // Xử lý các lỗi khi đăng ký
                        try {
                            throw task.getException();
                        } catch (FirebaseAuthWeakPasswordException e) {
                            new CustomDialog(RegisterActivity.this)
                                    .setTitle("Register Failed")
                                    .setMessage("Password is too weak. Please choose a stronger password.")
                                    .setIcon(R.drawable.warning)
                                    .setPositiveButton("OK", null)
                                    .hideNegativeButton()
                                    .show();

                        } catch (FirebaseAuthInvalidCredentialsException e) {
                            new CustomDialog(RegisterActivity.this)
                                    .setTitle("Register Failed")
                                    .setMessage("The email address is invalid. Please enter a valid email address.")
                                    .setIcon(R.drawable.warning)
                                    .setPositiveButton("OK", null)
                                    .hideNegativeButton()
                                    .show();
                        } catch (FirebaseAuthUserCollisionException e) {
                            new CustomDialog(RegisterActivity.this)
                                    .setTitle("Register Failed")
                                    .setMessage("The email address is already in use. Please use a different email.")
                                    .setIcon(R.drawable.warning)
                                    .setPositiveButton("OK", null)
                                    .hideNegativeButton()
                                    .show();
                        } catch (Exception e) {
                            Log.e("RegisterActivity", e.getMessage());
                            Toast.makeText(RegisterActivity.this, "Register Failed", Toast.LENGTH_SHORT).show();
                        }
                    }


                });
    }
}