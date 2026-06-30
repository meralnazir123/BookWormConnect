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
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    EditText etemail, etusername, etpassword, etcpassword;
    TextView login;
    Button button;
    FirebaseAuth mAuth;
    DatabaseReference databaseReference;
    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.register_activity);
        etemail = findViewById(R.id.mail);
        etusername = findViewById(R.id.usern);
        etpassword = findViewById(R.id.pwd);
        etpassword.setOnTouchListener((v, event) -> {

            if (event.getAction() == android.view.MotionEvent.ACTION_UP) {
                if (event.getRawX() >=
                        (etpassword.getRight()- etpassword.getCompoundDrawables()[2].getBounds().width())) {
                    if (etpassword.getTransformationMethod()
                            instanceof android.text.method.PasswordTransformationMethod) {

                        etpassword.setTransformationMethod(
                                android.text.method.HideReturnsTransformationMethod.getInstance());

                        etpassword.setCompoundDrawablesWithIntrinsicBounds(
                                0, 0, R.drawable.visibility_24dp_e3e3e3, 0);

                    } else {

                        etpassword.setTransformationMethod(
                                android.text.method.PasswordTransformationMethod.getInstance());

                        etpassword.setCompoundDrawablesWithIntrinsicBounds(
                                0, 0, R.drawable.visibility_off_24dp_e3e3e3, 0);
                    }

                    etpassword.setSelection(etpassword.getText().length());
                    return true;
                }
            }
            return false;
        });
        etcpassword = findViewById(R.id.cpwd);
        etcpassword.setOnTouchListener((v, event) -> {

            if (event.getAction() == MotionEvent.ACTION_UP) {

                if (event.getRawX() >=
                        (etcpassword.getRight()
                                - etcpassword.getCompoundDrawables()[2].getBounds().width())) {

                    if (etcpassword.getTransformationMethod()
                            instanceof PasswordTransformationMethod) {

                        etcpassword.setTransformationMethod(
                                HideReturnsTransformationMethod.getInstance());

                        etcpassword.setCompoundDrawablesWithIntrinsicBounds(
                                0, 0, R.drawable.visibility_24dp_e3e3e3, 0);

                    } else {

                        etcpassword.setTransformationMethod(
                                PasswordTransformationMethod.getInstance());

                        etcpassword.setCompoundDrawablesWithIntrinsicBounds(
                                0, 0, R.drawable.visibility_off_24dp_e3e3e3, 0);
                    }

                    etcpassword.setSelection(etcpassword.getText().length());
                    return true;
                }
            }

            return false;
        });
        mAuth = FirebaseAuth.getInstance();
        databaseReference=FirebaseDatabase.getInstance().getReference("users");
        button = findViewById(R.id.button);
        login = findViewById(R.id.loginTv);
        login.setOnClickListener(v -> {
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });
        button.setOnClickListener(v -> {

            String email, username, password, cpassword;

            email = String.valueOf(etemail.getText());
            username = String.valueOf(etusername.getText());
            password = String.valueOf(etpassword.getText());
            cpassword = String.valueOf(etcpassword.getText());
            if (email.isEmpty() || username.isEmpty() || password.isEmpty() || cpassword.isEmpty()) {
                Toast.makeText(RegisterActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }
            if(password.length() < 6){
                Toast.makeText(this,
                        "Password must be at least 6 characters",
                        Toast.LENGTH_SHORT).show();
                return;
            }
            if (!password.equals(cpassword)) {
                Toast.makeText(RegisterActivity.this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            } else {
                    FirebaseFirestore.getInstance()
                            .collection("users")
                            .whereEqualTo("username", username)
                            .get()
                            .addOnSuccessListener(queryDocumentSnapshots -> {

                                if (!queryDocumentSnapshots.isEmpty()) {
                                    Toast.makeText(RegisterActivity.this,
                                            "Username already exists",
                                            Toast.LENGTH_SHORT).show();
                                    return;
                                }

                                mAuth.createUserWithEmailAndPassword(email, password)
                                        .addOnCompleteListener(task -> {
                                            if (task.isSuccessful()) {
                                                FirebaseUser user = mAuth.getCurrentUser();
                                                if (user != null) {
                                                    FirebaseFirestore db = FirebaseFirestore.getInstance();

                                                    Map<String, Object> userData = new HashMap<>();
                                                    userData.put("username", username);
                                                    userData.put("email", email);
                                                    userData.put("bio", "");
                                                    userData.put("profileImageUrl", "");

                                                    db.collection("users")
                                                            .document(user.getUid())
                                                            .set(userData);
                                                    DatabaseReference databaseReference =
                                                            FirebaseDatabase.getInstance().getReference("users");

                                                    databaseReference.child(user.getUid()).setValue(userData);
                                                    user.sendEmailVerification();
                                                    Toast.makeText(RegisterActivity.this,
                                                            "Verification email sent",
                                                            Toast.LENGTH_SHORT).show();
                                                }
                                                Toast.makeText(RegisterActivity.this,
                                                        "Registration successful. Please verify your email before login.Check for spam folder.",
                                                        Toast.LENGTH_LONG).show();

                                                mAuth.signOut();

                                                startActivity(new Intent(RegisterActivity.this,
                                                        LoginActivity.class));

                                                finish();
                                            } else {
                                                Toast.makeText(RegisterActivity.this, "Registration failed: " +
                                                        (task.getException() != null ? task.getException().getMessage() : "Unknown error"), Toast.LENGTH_LONG).show();
                                            }
                                        });
                            });
                }
            });
        }
}
