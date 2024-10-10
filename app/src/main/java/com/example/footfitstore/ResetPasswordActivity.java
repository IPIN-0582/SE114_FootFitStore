package com.example.footfitstore;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;

public class ResetPasswordActivity extends AppCompatActivity {

    private EditText emailInput;
    private Button resetPasswordButton;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        // Khởi tạo FirebaseAuth
        mAuth = FirebaseAuth.getInstance();

        // Liên kết UI
        emailInput = findViewById(R.id.email_input);
        resetPasswordButton = findViewById(R.id.reset_password_button);

        // Xử lý sự kiện khi người dùng nhấn nút gửi email khôi phục mật khẩu
        resetPasswordButton.setOnClickListener(v -> {
            String email = emailInput.getText().toString().trim();
            if (!email.isEmpty()) {
                sendPasswordResetEmail(email);
            } else {
                Toast.makeText(ResetPasswordActivity.this, "Please enter your email", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendPasswordResetEmail(String email) {
        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        AlertDialog.Builder builder=new AlertDialog.Builder(this);
                        final View customLayout = getLayoutInflater().inflate(R.layout.reset_password_dialog,null);
                        builder.setView(customLayout);
                        Button positiveButton = customLayout.findViewById(R.id.pos_button);
                        AlertDialog alertDialog=builder.create();

                        positiveButton.setOnClickListener(v->{
                            startActivity(new Intent(ResetPasswordActivity.this, LoginActivity.class));
                            alertDialog.cancel();
                        });
                        builder.show();
                        //Toast.makeText(ResetPasswordActivity.this, "Password reset email sent.", Toast.LENGTH_SHORT).show();
                        // Sau khi gửi email thành công, bạn có thể điều hướng người dùng trở lại màn hình login
                    } else {
                        Toast.makeText(ResetPasswordActivity.this, "Failed to send reset email.", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
