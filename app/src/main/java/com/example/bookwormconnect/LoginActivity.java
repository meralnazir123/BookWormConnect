package com.example.bookwormconnect;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class LoginActivity extends AppCompatActivity {

    TextView registerTV;
    EditText UN, PW;
    Button button;
    FirebaseAuth mAuth;
    DatabaseReference databaseReference;
    private SharedPreferences sharedPreferences;
    CheckBox checkBox;
    private static final String PREFS_NAME = "LoginPrefs";
    private static final String PREF_USERNAME = "username";
    private static final String PREF_REMEMBER_ME = "rememberMe";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        boolean rememberMe = sharedPreferences.getBoolean(PREF_REMEMBER_ME, false);

        if (currentUser != null && rememberMe) {
            startActivity(new Intent(LoginActivity.this, HomeActivity.class));
            finish();
            return;
        }
        databaseReference = FirebaseDatabase.getInstance().getReference("users");
        checkBox=findViewById(R.id.checkBox);
        UN = findViewById(R.id.Un);
        PW = findViewById(R.id.pwd);
        button = findViewById(R.id.button);
        registerTV = findViewById(R.id.RegisterTV);

        loadLoginPreferences();
        registerTV.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
            finish();
        });



        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = UN.getText().toString().trim();
                String password = PW.getText().toString().trim();
                boolean rememberMe=checkBox.isChecked();

                if (username.isEmpty() || password.isEmpty()) {
                    Toast.makeText(LoginActivity.this, "All fields required!", Toast.LENGTH_SHORT).show();
                    return;
                }

                SharedPreferences.Editor editor=sharedPreferences.edit();
                if (rememberMe) {
                    editor.putString(PREF_USERNAME, username);
                    editor.putBoolean(PREF_REMEMBER_ME, true);
                } else {
                    editor.remove(PREF_USERNAME);
                    editor.putBoolean(PREF_REMEMBER_ME, false);
                }
                editor.apply();
                databaseReference.orderByChild("username").equalTo(username)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.exists()) {
                                    for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                                        String email = userSnapshot.child("email").getValue(String.class);
                                        if (email != null) {
                                            mAuth.signInWithEmailAndPassword(email, password)
                                                    .addOnCompleteListener(task -> {
                                                        if (task.isSuccessful()) {
                                                            Toast.makeText(LoginActivity.this, "Login Successful", Toast.LENGTH_SHORT).show();
                                                            startActivity(new Intent(LoginActivity.this, HomeActivity.class));
                                                            finish();
                                                        } else {
                                                            Toast.makeText(LoginActivity.this, "Invalid credentials", Toast.LENGTH_SHORT).show();
                                                        }
                                                    });
                                        }
                                    }
                                } else {
                                    Toast.makeText(LoginActivity.this, "Username not found", Toast.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Toast.makeText(LoginActivity.this, "Database error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });
    }

    private void loadLoginPreferences() {
        String savedUsername = sharedPreferences.getString(PREF_USERNAME, "");
        boolean rememberMeChecked = sharedPreferences.getBoolean(PREF_REMEMBER_ME, false);

        UN.setText(savedUsername);
        checkBox.setChecked(rememberMeChecked);
    }
}