package com.example.footfitstore.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

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

import java.util.Objects;

public class RegisterActivity extends AppCompatActivity {
    private EditText firstNameRegister, lastNameRegister, emailRegister, passwordRegister;
    private FirebaseAuth mAuth;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("Users");

        firstNameRegister = findViewById(R.id.firstnameRegister);
        lastNameRegister = findViewById(R.id.lastnameRegister);
        emailRegister = findViewById(R.id.emailRegister);
        passwordRegister = findViewById(R.id.passwordRegister);
        Button btnRegister = findViewById(R.id.btnRegister);
        TextView tvLogin = findViewById(R.id.tvLogin);

        btnRegister.setOnClickListener(v -> registerUser());

        tvLogin.setOnClickListener(v -> startActivity(new Intent(RegisterActivity.this, LoginActivity.class)));
    }

    private void registerUser() {
        String firstName = firstNameRegister.getText().toString().trim();
        String lastName = lastNameRegister.getText().toString().trim();
        String email = emailRegister.getText().toString().trim();
        String password = passwordRegister.getText().toString().trim();

        int gender = 0; // 0 = Male, 1 = Female
        String role = "user";
        String status = "active";

        if (TextUtils.isEmpty(firstName) || TextUtils.isEmpty(lastName) || TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
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

                            User user = new User(email);
                            user.setFirstName(firstName); // Lưu với tên trường là firstName
                            user.setLastName(lastName);   // Lưu với tên trường là lastName
                            user.setGender(gender);
                            user.setRole(role);
                            user.setStatus(status);

                            databaseReference.child(uid).setValue(user)
                                    .addOnCompleteListener(task1 -> {
                                        if (task1.isSuccessful()) {
                                            new CustomDialog(RegisterActivity.this)
                                                    .setTitle("Registration Successful")
                                                    .setMessage("Welcome, " + firstName + " " + lastName + "!")
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
                        }
                    } else {
                        handleFirebaseError(task.getException());
                    }
                });
    }

    private void handleFirebaseError(Exception exception) {
        try {
            throw Objects.requireNonNull(exception);
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
            new CustomDialog(RegisterActivity.this)
                    .setTitle("Failed")
                    .setMessage("Registration failed.")
                    .setIcon(R.drawable.error)
                    .setPositiveButton("OK", null)
                    .hideNegativeButton()
                    .show();
        }
    }
}
