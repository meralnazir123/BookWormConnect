package com.example.bookwormconnect;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.InputType;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

public class PostDetailActivity extends AppCompatActivity {

    Spinner spinner;
    EditText etDescription;
    Button postButton;
    EditText depositET;
    EditText durationET;

    private boolean isPosting = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_post_detail);

        spinner = findViewById(R.id.bookTypeSpinner);
        etDescription = findViewById(R.id.etDescription);
        postButton = findViewById(R.id.postButton);
        depositET = findViewById(R.id.lendingdepositET);
        durationET = findViewById(R.id.durationET);

        depositET.setInputType(
                InputType.TYPE_CLASS_NUMBER |
                        InputType.TYPE_NUMBER_FLAG_DECIMAL
        );

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                new String[]{
                        "Select book type",
                        "Novel",
                        "Text Book",
                        "Autobiography",
                        "Other"
                }
        );

        spinner.setAdapter(adapter);

        postButton.setOnClickListener(
                v -> uploadImageThenPost()
        );
    }

    private void uploadImageThenPost() {

        if (isPosting) {
            return;
        }

        Bitmap bitmap = TempPostHolder.bitmap;

        if (bitmap == null) {

            Toast.makeText(
                    this,
                    "Image missing",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        String bookType =
                spinner.getSelectedItem().toString();

        String description =
                etDescription.getText()
                        .toString()
                        .trim();

        String duration =
                durationET.getText()
                        .toString()
                        .trim();

        String deposit =
                depositET.getText()
                        .toString()
                        .trim();

        if (bookType.equals("Select book type")) {

            Toast.makeText(
                    this,
                    "Select book type",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        if (duration.isEmpty()) {

            Toast.makeText(
                    this,
                    "Enter duration",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        if (!duration.matches(
                "(?i)^\\d+\\s+(day|days|week|weeks|month|months|year|years)$"
        )) {

            Toast.makeText(
                    this,
                    "Use duration like 7 days, 1 week, or 1 month",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        String[] durationParts =
                duration.trim().split("\\s+");

        int durationNumber;

        try {

            durationNumber =
                    Integer.parseInt(durationParts[0]);

        } catch (NumberFormatException e) {

            Toast.makeText(
                    this,
                    "Enter a valid duration",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        if (durationNumber <= 0) {

            Toast.makeText(
                    this,
                    "Duration must be greater than 0",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        if (deposit.isEmpty()) {

            Toast.makeText(
                    this,
                    "Enter deposit amount",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        double depositValue;

        try {

            depositValue =
                    Double.parseDouble(deposit);

        } catch (NumberFormatException e) {

            Toast.makeText(
                    this,
                    "Enter a valid deposit amount",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        if (depositValue <= 0) {

            Toast.makeText(
                    this,
                    "Deposit must be greater than 0",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        if (depositValue > 2000) {

            Toast.makeText(
                    this,
                    "Deposit cannot be more than 2000",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        isPosting = true;

        postButton.setEnabled(false);
        postButton.setText("Posting...");

        FirebaseStorage storage =
                FirebaseStorage.getInstance();

        StorageReference ref =
                storage.getReference()
                        .child(
                                "images/" +
                                        System.currentTimeMillis() +
                                        ".jpg"
                        );

        ByteArrayOutputStream baos =
                new ByteArrayOutputStream();

        bitmap.compress(
                Bitmap.CompressFormat.JPEG,
                90,
                baos
        );

        byte[] data =
                baos.toByteArray();

        ref.putBytes(data)
                .addOnSuccessListener(task ->
                        ref.getDownloadUrl()
                                .addOnSuccessListener(uri ->
                                        savePost(uri.toString())
                                )
                                .addOnFailureListener(e ->
                                        postingFailed(
                                                "Could not get image URL"
                                        )
                                )
                )
                .addOnFailureListener(e ->
                        postingFailed(
                                "Image upload failed"
                        )
                );
    }

    private void savePost(String imageUrl) {

        String bookType =
                spinner.getSelectedItem().toString();

        String description =
                etDescription.getText()
                        .toString()
                        .trim();

        String duration =
                durationET.getText()
                        .toString()
                        .trim();

        String deposit =
                depositET.getText()
                        .toString()
                        .trim();

        Map<String, Object> post =
                new HashMap<>();

        post.put(
                "url",
                imageUrl
        );

        post.put(
                "username",
                TempPostHolder.username
        );

        post.put(
                "bookType",
                bookType
        );

        post.put(
                "description",
                description
        );

        post.put(
                "time",
                System.currentTimeMillis()
        );

        post.put(
                "userId",
                FirebaseAuth.getInstance().getUid()
        );

        post.put(
                "duration",
                duration
        );

        post.put(
                "deposit",
                deposit
        );

        post.put(
                "status",
                "Available"
        );

        FirebaseFirestore.getInstance()
                .collection("posts")
                .add(post)
                .addOnSuccessListener(doc -> {

                    TempPostHolder.bitmap = null;
                    TempPostHolder.username = null;

                    Toast.makeText(
                            this,
                            "Post added",
                            Toast.LENGTH_SHORT
                    ).show();

                    finish();
                })
                .addOnFailureListener(e ->
                        postingFailed(
                                "Failed to add post"
                        )
                );
    }

    private void postingFailed(String message) {

        isPosting = false;

        postButton.setEnabled(true);
        postButton.setText("Post");

        Toast.makeText(
                this,
                message,
                Toast.LENGTH_SHORT
        ).show();
    }
}