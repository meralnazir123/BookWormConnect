package com.example.bookwormconnect;

import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class forgetPW extends AppCompatActivity {

    private EditText emailET;
    private Button resetBtn;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forget_pw);

        emailET = findViewById(R.id.emailET);
        resetBtn = findViewById(R.id.resetBtn);

        mAuth = FirebaseAuth.getInstance();

        resetBtn.setOnClickListener(v -> {

            String email = emailET.getText().toString().trim();

            if (email.isEmpty()) {
                emailET.setError("Email is required");
                emailET.requestFocus();
                return;
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                emailET.setError("Enter a valid email");
                emailET.requestFocus();
                return;
            }

            mAuth.sendPasswordResetEmail(email)
                    .addOnCompleteListener(task -> {

                        if (task.isSuccessful()) {

                            Toast.makeText(
                                    forgetPW.this,
                                    "Password reset link sent to your email",
                                    Toast.LENGTH_LONG
                            ).show();

                            finish();

                        } else {

                            String message = task.getException() != null
                                    ? task.getException().getMessage()
                                    : "Failed to send reset email";

                            Toast.makeText(
                                    forgetPW.this,
                                    message,
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                    });
        });
    }
}