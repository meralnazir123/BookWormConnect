package com.example.bookwormconnect;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;

public class EditProfileActivity extends AppCompatActivity {
    private static final String TAG = "EditProfileActivity";
    private ImageView editProfileImage;
    private EditText editUserName, editUserBio;
    private Button btnChangeImage, btnSaveProfile;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;
    private FirebaseStorage storage;
    private StorageReference storageReference;
    private Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        editProfileImage = findViewById(R.id.editProfileImage);
        editUserName = findViewById(R.id.editUserName);
        editUserBio = findViewById(R.id.editUserBio);
        btnChangeImage = findViewById(R.id.btnChangeImage);
        btnSaveProfile = findViewById(R.id.btnSaveProfile);
        db = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if(currentUser == null){
            finish();
            return;
        }
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
        btnChangeImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            startActivityForResult(intent, 100);
        });
        btnSaveProfile.setOnClickListener(v -> {
            Log.d("EditProfile", "Save button clicked");

            String newName = editUserName.getText().toString().trim();
            String newBio = editUserBio.getText().toString().trim();

            if(newName.isEmpty()){
                editUserName.setError("Username required");
                return;
            }

            if(imageUri != null){
                uploadImageAndSave(newName,newBio);
            }else{
                updateProfile(newName,newBio,null);
            }

        });

    }
    private void uploadImageAndSave(String name,String bio){

        StorageReference imageRef = storageReference
                .child("profile_images")
                .child(currentUser.getUid() + ".jpg");

        imageRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> {

                    imageRef.getDownloadUrl()
                            .addOnSuccessListener(uri -> {

                                updateProfile(
                                        name,
                                        bio,
                                        uri.toString()
                                );

                            });

                })
                .addOnFailureListener(e -> {
                    Toast.makeText(
                            this,
                            "Image upload failed",
                            Toast.LENGTH_SHORT
                    ).show();
                });
    }
    private void updateProfile(
            String name,
            String bio,
            String imageUrl
    ){
        Map<String,Object> updates = new HashMap<>();
        updates.put("username",name);
        updates.put("bio",bio);
        if(imageUrl != null){
            updates.put("profileImageUrl",imageUrl);
        }
        db.collection("users")
                .document(currentUser.getUid())
                .set(updates, SetOptions.merge())
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(
                            this,
                            "Profile updated successfully", Toast.LENGTH_SHORT
                    ).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Log.e("EditProfile", "Update failed", e);

                    Toast.makeText(
                            this,
                            e.getMessage(),
                            Toast.LENGTH_LONG
                    ).show();
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
