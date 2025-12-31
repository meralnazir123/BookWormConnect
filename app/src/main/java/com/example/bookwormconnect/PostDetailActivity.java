package com.example.bookwormconnect;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_detail);

        spinner = findViewById(R.id.bookTypeSpinner);
        etDescription = findViewById(R.id.etDescription);
        postButton = findViewById(R.id.postButton);

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

        Map<String, Object> post = new HashMap<>();
        post.put("url", imageUrl);
        post.put("username", TempPostHolder.username);
        post.put("bookType", bookType);
        post.put("description", description);
        post.put("time", System.currentTimeMillis());

        FirebaseFirestore.getInstance()
                .collection("posts")
                .add(post)
                .addOnSuccessListener(doc -> {

                    postEntity entity = new postEntity();
                    entity.imageUrl = imageUrl;
                    entity.username = TempPostHolder.username;
                    entity.bookType = bookType;
                    entity.description = description;
                    entity.time = System.currentTimeMillis();

                    AppDatabase.getInstance(this)
                            .postDao()
                            .insert(entity);

                    TempPostHolder.bitmap = null;
                    TempPostHolder.username = null;

                    Toast.makeText(this, "Post added", Toast.LENGTH_SHORT).show();
                    finish();
                });
    }
}
