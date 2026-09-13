package com.example.bookwormconnect;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.MotionEvent;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    EditText etemail, etusername, etpassword, etcpassword;
    TextView login;
    Button button;

    FirebaseAuth mAuth;

    DatabaseReference databaseReference;
    DatabaseReference usernameReference;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.register_activity);

        etemail = findViewById(R.id.mail);
        etusername = findViewById(R.id.usern);
        etpassword = findViewById(R.id.pwd);
        etcpassword = findViewById(R.id.cpwd);

        button = findViewById(R.id.button);
        login = findViewById(R.id.loginTv);

        mAuth = FirebaseAuth.getInstance();

        databaseReference =
                FirebaseDatabase.getInstance().getReference("users");

        usernameReference =
                FirebaseDatabase.getInstance().getReference("usernames");

        etpassword.setOnTouchListener((v, event) -> {

            if (event.getAction() == MotionEvent.ACTION_UP) {

                if (event.getRawX() >=
                        (etpassword.getRight()
                                - etpassword.getCompoundDrawables()[2]
                                .getBounds()
                                .width())) {

                    if (etpassword.getTransformationMethod()
                            instanceof PasswordTransformationMethod) {

                        etpassword.setTransformationMethod(
                                HideReturnsTransformationMethod.getInstance()
                        );

                        etpassword.setCompoundDrawablesWithIntrinsicBounds(
                                0,
                                0,
                                R.drawable.visibility_24dp_e3e3e3,
                                0
                        );

                    } else {

                        etpassword.setTransformationMethod(
                                PasswordTransformationMethod.getInstance()
                        );

                        etpassword.setCompoundDrawablesWithIntrinsicBounds(
                                0,
                                0,
                                R.drawable.visibility_off_24dp_e3e3e3,
                                0
                        );
                    }

                    etpassword.setSelection(
                            etpassword.getText().length()
                    );

                    return true;
                }
            }

            return false;
        });

        // -----------------------------
        // Confirm password visibility
        // -----------------------------

        etcpassword.setOnTouchListener((v, event) -> {

            if (event.getAction() == MotionEvent.ACTION_UP) {

                if (event.getRawX() >=
                        (etcpassword.getRight()
                                - etcpassword.getCompoundDrawables()[2]
                                .getBounds()
                                .width())) {

                    if (etcpassword.getTransformationMethod()
                            instanceof PasswordTransformationMethod) {

                        etcpassword.setTransformationMethod(
                                HideReturnsTransformationMethod.getInstance()
                        );

                        etcpassword.setCompoundDrawablesWithIntrinsicBounds(
                                0,
                                0,
                                R.drawable.visibility_24dp_e3e3e3,
                                0
                        );

                    } else {

                        etcpassword.setTransformationMethod(
                                PasswordTransformationMethod.getInstance()
                        );

                        etcpassword.setCompoundDrawablesWithIntrinsicBounds(
                                0,
                                0,
                                R.drawable.visibility_off_24dp_e3e3e3,
                                0
                        );
                    }

                    etcpassword.setSelection(
                            etcpassword.getText().length()
                    );

                    return true;
                }
            }

            return false;
        });
        login.setOnClickListener(v -> {

            Intent intent =
                    new Intent(
                            RegisterActivity.this,
                            LoginActivity.class
                    );

            startActivity(intent);
            finish();
        });

        button.setOnClickListener(v -> registerUser());
    }


    private void registerUser() {

        String email =
                etemail.getText().toString().trim();

        String username =
                etusername.getText().toString().trim();

        String password =
                etpassword.getText().toString();

        String cpassword =
                etcpassword.getText().toString();

        if (email.isEmpty()
                || username.isEmpty()
                || password.isEmpty()
                || cpassword.isEmpty()) {

            Toast.makeText(
                    RegisterActivity.this,
                    "Please fill all fields",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        if (password.length() < 6 || password.length() >8 || !password.matches(".*[^a-zA-Z0-9].*")) {

            Toast.makeText(
                    RegisterActivity.this,
                    "Password must be at least 6 characters and contain 1 special character",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        if (!password.equals(cpassword)) {

            Toast.makeText(
                    RegisterActivity.this,
                    "Passwords do not match",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }


        // -----------------------------
        // Normalize username
        // -----------------------------

        String normalizedUsername =
                username.toLowerCase();


        // -----------------------------
        // Create Firebase Auth account
        // -----------------------------

        mAuth.createUserWithEmailAndPassword(
                        email,
                        password
                )
                .addOnCompleteListener(task -> {

                    if (!task.isSuccessful()) {

                        String errorMessage =
                                "Registration failed";

                        if (task.getException() != null) {
                            errorMessage =
                                    "Registration failed: "
                                            + task.getException().getMessage();
                        }

                        Toast.makeText(
                                RegisterActivity.this,
                                errorMessage,
                                Toast.LENGTH_LONG
                        ).show();

                        return;
                    }


                    // -----------------------------
                    // Get newly created user
                    // -----------------------------

                    FirebaseUser user =
                            mAuth.getCurrentUser();

                    if (user == null) {

                        Toast.makeText(
                                RegisterActivity.this,
                                "User creation failed",
                                Toast.LENGTH_SHORT
                        ).show();

                        return;
                    }


                    String uid =
                            user.getUid();


                    // -----------------------------
                    // Check username availability
                    // -----------------------------

                    usernameReference
                            .child(normalizedUsername)
                            .get()
                            .addOnSuccessListener(snapshot -> {

                                if (snapshot.exists()) {

                                    // Username already exists.
                                    // Delete newly created Auth account.

                                    user.delete();

                                    Toast.makeText(
                                            RegisterActivity.this,
                                            "Username already exists",
                                            Toast.LENGTH_SHORT
                                    ).show();

                                    return;
                                }
                                Map<String, Object> userData =
                                        new HashMap<>();

                                userData.put(
                                        "username",
                                        username
                                );

                                userData.put(
                                        "email",
                                        email
                                );

                                userData.put(
                                        "bio",
                                        ""
                                );

                                userData.put(
                                        "profileImageUrl",
                                        ""
                                );


                                databaseReference
                                        .child(uid)
                                        .setValue(userData)
                                        .addOnSuccessListener(unused -> {

                                            Map<String, Object> usernameData =
                                                    new HashMap<>();

                                            usernameData.put(
                                                    "uid",
                                                    uid
                                            );

                                            usernameData.put(
                                                    "email",
                                                    email
                                            );


                                            usernameReference
                                                    .child(normalizedUsername)
                                                    .setValue(usernameData)
                                                    .addOnSuccessListener(v -> {

                                                        Toast.makeText(
                                                                RegisterActivity.this,
                                                                "Username saved. Sending verification email...",
                                                                Toast.LENGTH_LONG
                                                        ).show();

                                                        user.sendEmailVerification()
                                                                .addOnCompleteListener(emailTask -> {

                                                                    if (emailTask.isSuccessful()) {

                                                                        Toast.makeText(
                                                                                RegisterActivity.this,
                                                                                "Verification email sent!",
                                                                                Toast.LENGTH_LONG
                                                                        ).show();

                                                                        mAuth.signOut();

                                                                        Intent intent = new Intent(
                                                                                RegisterActivity.this,
                                                                                LoginActivity.class
                                                                        );

                                                                        startActivity(intent);
                                                                        finish();

                                                                    } else {

                                                                        String error = "Unknown error";

                                                                        if (emailTask.getException() != null) {
                                                                            error = emailTask.getException().getMessage();
                                                                        }

                                                                        Toast.makeText(
                                                                                RegisterActivity.this,
                                                                                "Verification email failed: " + error,
                                                                                Toast.LENGTH_LONG
                                                                        ).show();
                                                                    }
                                                                });
                                                    })
                                                    .addOnFailureListener(e -> Toast.makeText(
                                                            RegisterActivity.this,
                                                            "Username could not be saved: "
                                                                    + e.getMessage(),
                                                            Toast.LENGTH_LONG
                                                    ).show());

                                        })
                                        .addOnFailureListener(
                                                e -> Toast.makeText(
                                                        RegisterActivity.this,
                                                        "Failed to save user: "
                                                                + e.getMessage(),
                                                        Toast.LENGTH_LONG
                                                ).show()
                                        );

                            })
                            .addOnFailureListener(
                                    e -> Toast.makeText(
                                            RegisterActivity.this,
                                            "Database error: "
                                                    + e.getMessage(),
                                            Toast.LENGTH_LONG
                                    ).show()
                            );
                });
    }
}