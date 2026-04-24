package com.example.bookwormconnect;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_detail);

        spinner = findViewById(R.id.bookTypeSpinner);
        etDescription = findViewById(R.id.etDescription);
        postButton = findViewById(R.id.postButton);
        depositET=findViewById(R.id.lendingdepositET);
        durationET=findViewById(R.id.durationET);


        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                new String[]{
                        "Select book type",
                        "Novel", "Text Book", "Autobiography", "Other"
                }
        );
        spinner.setAdapter(adapter);

        postButton.setOnClickListener(v -> uploadImageThenPost());
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 100 && resultCode == RESULT_OK) {
            String address = data.getStringExtra("address");
        }
    }

    private void uploadImageThenPost() {

        Bitmap bitmap = TempPostHolder.bitmap;
        if (bitmap == null) {
            Toast.makeText(this, "Image missing", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference ref = storage.getReference()
                .child("images/" + System.currentTimeMillis() + ".jpg");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, baos);
        byte[] data = baos.toByteArray();

        ref.putBytes(data)
                .addOnSuccessListener(task ->
                        ref.getDownloadUrl().addOnSuccessListener(uri ->
                                savePost(uri.toString())
                        )
                )
                .addOnFailureListener(e ->
                        Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }

    private void savePost(String imageUrl) {

        String bookType = spinner.getSelectedItem().toString();
        String description = etDescription.getText().toString();
        String duration = durationET.getText().toString().trim();
        String deposit = depositET.getText().toString().trim();

        if (bookType.equals("Select book type")) {
            Toast.makeText(this, "Select book type", Toast.LENGTH_SHORT).show();
            return;
        }

        if (duration.isEmpty() || deposit.isEmpty()) {
            Toast.makeText(this, "Fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> post = new HashMap<>();
        post.put("url", imageUrl);
        post.put("username", TempPostHolder.username);
        post.put("bookType", bookType);
        post.put("description", description);
        post.put("time", System.currentTimeMillis());
        post.put("userId", FirebaseAuth.getInstance().getUid());
        post.put("duration", duration);
        post.put("deposit", deposit);

        FirebaseFirestore.getInstance()
                .collection("posts")
                .add(post)
                .addOnSuccessListener(doc -> {

                    TempPostHolder.bitmap = null;
                    TempPostHolder.username = null;

                    Toast.makeText(this, "Post added", Toast.LENGTH_SHORT).show();
                    finish();
                });
    }
}
