package com.example.bookwormconnect;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;

public class EditProfileActivity extends AppCompatActivity {

    private ImageView editProfileImage;
    private EditText editUserName, editUserBio;
    private Button btnChangeImage, btnSaveProfile;

    private FirebaseUser currentUser;
    private DatabaseReference usersReference;

    private FirebaseStorage storage;
    private StorageReference storageReference;

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

        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser == null) {
            finish();
            return;
        }

        usersReference = FirebaseDatabase.getInstance()
                .getReference("users");

        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        loadProfileData();

        btnChangeImage.setOnClickListener(v -> {

            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            startActivityForResult(intent, 100);

        });

        btnSaveProfile.setOnClickListener(v -> {

            String newName =
                    editUserName.getText().toString().trim();

            String newBio =
                    editUserBio.getText().toString().trim();

            if (newName.isEmpty()) {

                editUserName.setError("Username required");
                return;
            }

            if (imageUri != null) {

                uploadImageAndSave(newName, newBio);

            } else {

                updateProfile(
                        newName,
                        newBio,
                        null
                );
            }
        });
    }

    private void loadProfileData() {

        usersReference
                .child(currentUser.getUid())
                .get()
                .addOnSuccessListener(snapshot -> {

                    if (!snapshot.exists()) {
                        return;
                    }

                    String profileImageUrl =
                            snapshot.child("profileImageUrl")
                                    .getValue(String.class);

                    String userName =
                            snapshot.child("username")
                                    .getValue(String.class);

                    String userBio =
                            snapshot.child("bio")
                                    .getValue(String.class);

                    if (profileImageUrl != null &&
                            !profileImageUrl.isEmpty()) {

                        Glide.with(this)
                                .load(profileImageUrl)
                                .into(editProfileImage);

                    } else {

                        editProfileImage.setImageResource(
                                R.drawable.user
                        );
                    }

                    if (userName != null) {
                        editUserName.setText(userName);
                    }

                    if (userBio != null) {
                        editUserBio.setText(userBio);
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(
                        this,
                        "Failed to load profile: "
                                + e.getMessage(),
                        Toast.LENGTH_LONG
                ).show());
    }

    private void uploadImageAndSave(
            String name,
            String bio
    ) {

        StorageReference imageRef =
                storageReference
                        .child("profile_images")
                        .child(currentUser.getUid() + ".jpg");

        imageRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> imageRef.getDownloadUrl()
                        .addOnSuccessListener(uri -> updateProfile(
                                name,
                                bio,
                                uri.toString()
                        )))
                .addOnFailureListener(e -> Toast.makeText(
                        this,
                        "Image upload failed: "
                                + e.getMessage(),
                        Toast.LENGTH_LONG
                ).show());
    }

    private void updateProfile(
            String name,
            String bio,
            String imageUrl
    ) {

        Map<String, Object> updates =
                new HashMap<>();

        updates.put("username", name);
        updates.put("bio", bio);

        if (imageUrl != null) {
            updates.put(
                    "profileImageUrl",
                    imageUrl
            );
        }

        usersReference
                .child(currentUser.getUid())
                .updateChildren(updates)
                .addOnSuccessListener(unused -> updateUsernameMapping(
                        name,
                        () -> {

                            Toast.makeText(
                                    this,
                                    "Profile updated successfully",
                                    Toast.LENGTH_SHORT
                            ).show();

                            finish();
                        }
                ))
                .addOnFailureListener(e -> Toast.makeText(
                        this,
                        "Profile update failed: "
                                + e.getMessage(),
                        Toast.LENGTH_LONG
                ).show());
    }

    private void updateUsernameMapping(
            String newUsername,
            Runnable onComplete
    ) {

        String newNormalizedUsername =
                newUsername.toLowerCase().trim();

        DatabaseReference usernamesReference =
                FirebaseDatabase.getInstance()
                        .getReference("usernames");

        usersReference
                .child(currentUser.getUid())
                .child("username")
                .get()
                .addOnSuccessListener(oldUsernameSnapshot -> {

                    String oldUsername =
                            oldUsernameSnapshot.getValue(String.class);

                    String oldNormalizedUsername =
                            oldUsername != null
                                    ? oldUsername.toLowerCase().trim()
                                    : "";

                    usernamesReference
                            .child(newNormalizedUsername)
                            .get()
                            .addOnSuccessListener(newUsernameSnapshot -> {

                                if (newUsernameSnapshot.exists()) {

                                    String existingUid =
                                            newUsernameSnapshot
                                                    .child("uid")
                                                    .getValue(String.class);

                                    if (existingUid != null &&
                                            !existingUid.equals(
                                                    currentUser.getUid())) {

                                        Toast.makeText(
                                                this,
                                                "Username already exists",
                                                Toast.LENGTH_LONG
                                        ).show();

                                        return;
                                    }
                                }

                                Map<String, Object> newUsernameData =
                                        new HashMap<>();

                                newUsernameData.put(
                                        "uid",
                                        currentUser.getUid()
                                );

                                newUsernameData.put(
                                        "email",
                                        currentUser.getEmail()
                                );

                                if (!oldNormalizedUsername.isEmpty()
                                        && !oldNormalizedUsername.equals(
                                        newNormalizedUsername)) {

                                    usernamesReference
                                            .child(oldNormalizedUsername)
                                            .removeValue()
                                            .addOnSuccessListener(unused -> usernamesReference
                                                    .child(newNormalizedUsername)
                                                    .setValue(newUsernameData)
                                                    .addOnSuccessListener(unused2 ->
                                                            onComplete.run()
                                                    )
                                                    .addOnFailureListener(e ->
                                                            Toast.makeText(
                                                                    this,
                                                                    "New username could not be saved: "
                                                                            + e.getMessage(),
                                                                    Toast.LENGTH_LONG
                                                            ).show()
                                                    ))
                                            .addOnFailureListener(e ->
                                                    Toast.makeText(
                                                            this,
                                                            "Old username could not be deleted: "
                                                                    + e.getMessage(),
                                                            Toast.LENGTH_LONG
                                                    ).show()
                                            );

                                } else {

                                    usernamesReference
                                            .child(newNormalizedUsername)
                                            .setValue(newUsernameData)
                                            .addOnSuccessListener(unused ->
                                                    onComplete.run()
                                            )
                                            .addOnFailureListener(e ->
                                                    Toast.makeText(
                                                            this,
                                                            "Username could not be saved: "
                                                                    + e.getMessage(),
                                                            Toast.LENGTH_LONG
                                                    ).show()
                                            );
                                }
                            })
                            .addOnFailureListener(e ->
                                    Toast.makeText(
                                            this,
                                            "Could not check username: "
                                                    + e.getMessage(),
                                            Toast.LENGTH_LONG
                                    ).show()
                            );
                })
                .addOnFailureListener(e ->
                        Toast.makeText(
                                this,
                                "Could not get current username: "
                                        + e.getMessage(),
                                Toast.LENGTH_LONG
                        ).show()
                );
    }

    @Override
    protected void onActivityResult(
            int requestCode,
            int resultCode,
            @Nullable Intent data
    ) {

        super.onActivityResult(
                requestCode,
                resultCode,
                data
        );

        if (requestCode == 100 &&
                resultCode == RESULT_OK &&
                data != null) {

            imageUri = data.getData();

            editProfileImage.setImageURI(imageUri);
        }
    }
}