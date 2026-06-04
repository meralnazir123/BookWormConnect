package com.example.bookwormconnect;
import android.content.Intent;
import android.os.Bundle;
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

public class RegisterActivity extends AppCompatActivity {

    EditText etemail, etusername, etpassword, etcpassword;
    TextView login;
    Button button;
    FirebaseAuth mAuth;
    DatabaseReference databaseReference;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.register_activity);
        etemail = findViewById(R.id.mail);
        etusername = findViewById(R.id.usern);
        etpassword = findViewById(R.id.pwd);
        etcpassword = findViewById(R.id.cpwd);
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
            }
            else {

                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                FirebaseUser user = mAuth.getCurrentUser();
                                if (user != null) {
                                    databaseReference.child(user.getUid()).child("username").setValue(username);
                                    databaseReference.child(user.getUid()).child("email").setValue(email);
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
                            }
                            else {
                                Toast.makeText(RegisterActivity.this,  "Registration failed: " +
                                        (task.getException()!=null?task.getException().getMessage():"Unknown error"), Toast.LENGTH_LONG).show();
                            }
                        });
            }
                    });
                }
}
