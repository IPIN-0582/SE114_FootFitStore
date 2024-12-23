package com.example.footfitstore.activity.User;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.footfitstore.R;
import com.example.footfitstore.Utils.CustomDialog;
import com.google.firebase.auth.FirebaseAuth;

public class ResetPasswordActivity extends AppCompatActivity {

    private EditText emailInput;
    private Button resetPasswordButton;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);
        mAuth = FirebaseAuth.getInstance();
        emailInput = findViewById(R.id.email_input);
        resetPasswordButton = findViewById(R.id.reset_password_button);
        resetPasswordButton.setOnClickListener(v -> {
            String email = emailInput.getText().toString().trim();
            if (!email.isEmpty()) {
                sendPasswordResetEmail(email);
            } else {
                new CustomDialog(ResetPasswordActivity.this)
                        .setTitle("Failed")
                        .setMessage("Please Enter Your Email.")
                        .setIcon(R.drawable.error)
                        .setPositiveButton("OK", null)
                        .hideNegativeButton()
                        .show();
            }
        });
    }

    private void sendPasswordResetEmail(String email) {
        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        AlertDialog.Builder builder=new AlertDialog.Builder(this,R.style.CustomAlertDialog);
                        final View customLayout = getLayoutInflater().inflate(R.layout.dialog_reset_password,null);
                        builder.setView(customLayout);
                        Button positiveButton = customLayout.findViewById(R.id.pos_button);
                        AlertDialog alertDialog=builder.create();
                        positiveButton.setOnClickListener(v->{
                            super.onBackPressed();
                            alertDialog.cancel();
                        });
                        builder.show();
                    } else {
                        new CustomDialog(ResetPasswordActivity.this)
                                .setTitle("Failed")
                                .setMessage("Failed To Send Email.")
                                .setIcon(R.drawable.error)
                                .setPositiveButton("OK", null)
                                .hideNegativeButton()
                                .show();
                    }
                });
    }
}
