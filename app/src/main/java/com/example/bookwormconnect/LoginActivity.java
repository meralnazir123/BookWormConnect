package com.example.bookwormconnect;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MotionEvent;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


public class LoginActivity extends AppCompatActivity {

    TextView registerTV, forgetPW;
    EditText UN, PW;
    Button button;

    FirebaseAuth mAuth;
    DatabaseReference usernameReference;

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

        sharedPreferences = getSharedPreferences(
                PREFS_NAME,
                Context.MODE_PRIVATE
        );

        mAuth = FirebaseAuth.getInstance();



        usernameReference = FirebaseDatabase.getInstance()
                .getReference("usernames");

        checkBox = findViewById(R.id.checkBox);
        UN = findViewById(R.id.Un);
        PW = findViewById(R.id.pwd);

        button = findViewById(R.id.button);
        registerTV = findViewById(R.id.RegisterTV);
        forgetPW = findViewById(R.id.forgetPW);
        FirebaseUser currentUser =
                mAuth.getCurrentUser();

        boolean savedRememberMe =
                sharedPreferences.getBoolean(
                        PREF_REMEMBER_ME,
                        false
                );

        if (currentUser != null &&
                currentUser.isEmailVerified() &&
                savedRememberMe) {

            startActivity(new Intent(
                    LoginActivity.this,
                    HomeActivity.class
            ));

            finish();
            return;
        }

        PW.setOnTouchListener((v, event) -> {

            if (event.getAction() ==
                    MotionEvent.ACTION_UP) {

                if (event.getRawX() >=
                        (PW.getRight()
                                - PW.getCompoundDrawables()[2]
                                .getBounds()
                                .width())) {

                    if (PW.getTransformationMethod()
                            instanceof PasswordTransformationMethod) {

                        // Show password

                        PW.setTransformationMethod(
                                HideReturnsTransformationMethod
                                        .getInstance()
                        );

                        PW.setCompoundDrawablesWithIntrinsicBounds(
                                0,
                                0,
                                R.drawable.visibility_24dp_e3e3e3,
                                0
                        );

                    } else {

                        // Hide password

                        PW.setTransformationMethod(
                                PasswordTransformationMethod
                                        .getInstance()
                        );

                        PW.setCompoundDrawablesWithIntrinsicBounds(
                                0,
                                0,
                                R.drawable.visibility_off_24dp_e3e3e3,
                                0
                        );
                    }

                    PW.setSelection(
                            PW.getText().length()
                    );

                    return true;
                }
            }

            return false;
        });


        loadLoginPreferences();


        forgetPW.setOnClickListener(v ->
                startActivity(new Intent(
                        LoginActivity.this,
                        forgetPW.class
                ))
        );
        registerTV.setOnClickListener(v -> {

            Intent intent = new Intent(
                    LoginActivity.this,
                    RegisterActivity.class
            );

            startActivity(intent);
            finish();
        });

        button.setOnClickListener(v -> {

            String usernameOrEmail =
                    UN.getText()
                            .toString()
                            .trim();

            String password =
                    PW.getText()
                            .toString()
                            .trim();

            if (usernameOrEmail.isEmpty() ||
                    password.isEmpty()) {

                Toast.makeText(
                        LoginActivity.this,
                        "All fields required!",
                        Toast.LENGTH_SHORT
                ).show();

                return;
            }


            boolean rememberMe =
                    checkBox.isChecked();


            if (android.util.Patterns.EMAIL_ADDRESS
                    .matcher(usernameOrEmail)
                    .matches()) {

                loginWithEmail(
                        usernameOrEmail,
                        password,
                        rememberMe
                );

            }


            else {

                loginWithUsername(
                        usernameOrEmail,
                        password,
                        rememberMe
                );
            }
        });
    }


    private void loginWithEmail(
            String email,
            String password,
            boolean rememberMe
    ) {

        mAuth.signInWithEmailAndPassword(
                        email,
                        password
                )
                .addOnCompleteListener(task -> {

                    if (task.isSuccessful()) {

                        checkEmailVerification(
                                rememberMe
                        );

                    } else {

                        Toast.makeText(
                                LoginActivity.this,
                                "Invalid email or password",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                });
    }

    private void loginWithUsername(
            String username,
            String password,
            boolean rememberMe
    ) {
        String normalizedUsername =
                username
                        .trim()
                        .toLowerCase();
        usernameReference
                .child(normalizedUsername)
                .get()
                .addOnSuccessListener(snapshot -> {

                    if (!snapshot.exists()) {

                        Toast.makeText(
                                LoginActivity.this,
                                "Username not found",
                                Toast.LENGTH_SHORT
                        ).show();

                        return;
                    }

                    String email =
                            snapshot.child("email")
                                    .getValue(String.class);

                    if (email == null ||
                            email.isEmpty()) {

                        Toast.makeText(
                                LoginActivity.this,
                                "Email not found for this username",
                                Toast.LENGTH_SHORT
                        ).show();

                        return;
                    }


                    loginWithEmail(
                            email,
                            password,
                            rememberMe
                    );
                })
                .addOnFailureListener(e -> {

                    Toast.makeText(
                            LoginActivity.this,
                            "Username lookup failed: "
                                    + e.getMessage(),
                            Toast.LENGTH_LONG
                    ).show();
                });
    }


    private void loadLoginPreferences() {

        String savedUsername =
                sharedPreferences.getString(
                        PREF_USERNAME,
                        ""
                );


        boolean rememberMeChecked =
                sharedPreferences.getBoolean(
                        PREF_REMEMBER_ME,
                        false
                );

              UN.setText(savedUsername);
        checkBox.setChecked(
                rememberMeChecked
        );
    }
    private void checkEmailVerification(
            boolean rememberMe
    ) {
        FirebaseUser user =
                mAuth.getCurrentUser();
        if (user == null) {
            return;
        }
        user.reload()
                .addOnCompleteListener(reloadTask -> {

                    FirebaseUser updatedUser =
                            mAuth.getCurrentUser();
                    if (updatedUser != null &&
                            updatedUser.isEmailVerified()) {
                        SharedPreferences.Editor editor =
                                sharedPreferences.edit();
                        if (rememberMe) {
                            editor.putString(
                                    PREF_USERNAME,
                                    UN.getText()
                                            .toString()
                                            .trim()
                            );

                            editor.putBoolean(
                                    PREF_REMEMBER_ME,
                                    true
                            );

                        } else {
                            editor.remove(
                                    PREF_USERNAME
                            );

                            editor.putBoolean(
                                    PREF_REMEMBER_ME,
                                    false
                            );
                        }
                        editor.apply();
                        Toast.makeText(
                                LoginActivity.this,
                                "Login Successful",
                                Toast.LENGTH_SHORT
                        ).show();
                        startActivity(new Intent(
                                LoginActivity.this,
                                HomeActivity.class
                        ));
                        finish();
                    }
                    else {
                        Toast.makeText(
                                LoginActivity.this,
                                "Please verify your email first",
                                Toast.LENGTH_LONG
                        ).show();
                        mAuth.signOut();
                    }
                });
    }
}