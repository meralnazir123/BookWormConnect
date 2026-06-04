package com.example.bookwormconnect;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class EditProfileActivity extends AppCompatActivity {
    private static final String TAG = "EditProfileActivity";
    private ImageView editProfileImage;
    private EditText editUserName, editUserBio;
    private Button btnChangeImage, btnSaveProfile;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;
    private Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        editProfileImage = findViewById(R.id.editProfileImage);
        editUserName = findViewById(R.id.editUserName);
        editUserBio = findViewById(R.id.editUserBio);
        btnChangeImage = findViewById(R.id.btnChangeImage);
        btnSaveProfile = findViewById(R.id.btnSaveProfile);

        db = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        // Load current profile data
        db.collection("users").document(currentUser.getUid()).get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        String profileImageUrl = document.getString("profileImageUrl");
                        String userName = document.getString("username");
                        String userBio = document.getString("bio");

                        if (profileImageUrl != null) {
                            Glide.with(this).load(profileImageUrl).into(editProfileImage);
                        }
                        editUserName.setText(userName);
                        editUserBio.setText(userBio);
                    }
                });

        // Change picture (open gallery)
        btnChangeImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            startActivityForResult(intent, 100);
        });

        // Save changes
        btnSaveProfile.setOnClickListener(v -> {
            String newName = editUserName.getText().toString();
            String newBio = editUserBio.getText().toString();

            db.collection("users").document(currentUser.getUid())
                    .update("username", newName, "bio", newBio)
                    .addOnSuccessListener(aVoid -> Log.d(TAG, "Profile updated"))
                    .addOnFailureListener(e -> Log.w(TAG, "Error updating profile", e));

            // TODO: Upload imageUri to Firebase Storage and update profileImageUrl
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == RESULT_OK && data != null) {
            imageUri = data.getData();
            editProfileImage.setImageURI(imageUri);
        }
    }
}
