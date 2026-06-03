package com.example.bookwormconnect;

import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.renderscript.ScriptGroup;
import android.util.Log;
import android.widget.RatingBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;


import com.example.bookwormconnect.databinding.ActivityProfilescreenBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import androidx.annotation.NonNull;

import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.SSLSessionBindingEvent;


public class profilescreen extends AppCompatActivity {
    private static final String TAG = "ProfileActivity";
    private ActivityProfilescreenBinding binding;
    private PostAdapter2 postAdapter;
    private List<Post> posts;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfilescreenBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        FirebaseApp.initializeApp(this);
        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser == null) {
            // Handle not logged in (e.g., redirect to login)
            finish();
            return;
        }

        // Initialize posts list and adapter
        posts= new ArrayList<>();
        postAdapter = new PostAdapter2(posts);
        binding.PostsRecycler.setLayoutManager(new GridLayoutManager(this, 3));
        binding.PostsRecycler.setAdapter(postAdapter);

        // Load profile data and posts
        loadProfileData();
        loadPosts();
        binding.btnEditProfile.setOnClickListener(v -> {
            Intent intent = new Intent(profilescreen.this, EditProfileActivity.class);
            startActivity(intent);
        });

        // RatingBar setup (INSIDE onCreate)
        RatingBar ratingBar = binding.RatingBar;
        TextView averageRatingText = binding.averageRatingText;

        // Load average rating when profile opens
        loadAverageRating(averageRatingText);

        // Handle user rating input
        ratingBar.setOnRatingBarChangeListener((bar, rating, fromUser) -> {
            if (fromUser) {
                String uid = currentUser.getUid();
                String profileId = currentUser.getUid(); // or another user's profile ID

                db.collection("users").document(profileId)
                        .collection("ratings").document(uid)
                        .set(new Rating(rating))
                        .addOnSuccessListener(aVoid -> {
                            Log.d(TAG, "Rating submitted: " + rating);
                            loadAverageRating(averageRatingText); // refresh average
                        })
                        .addOnFailureListener(e -> Log.w(TAG, "Error submitting rating", e));
            }
        });
    }

    private void loadProfileData() {
        db.collection("users").document(currentUser.getUid()).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                String profileImageUrl = document.getString("profileImageUrl");
                                String userName = document.getString("username");
                                String userBio = document.getString("bio");
                                Long postsCnt = document.getLong("postsCount");

                                // Set data using binding
                                if (profileImageUrl != null) {
                                    Glide.with(profilescreen.this).load(profileImageUrl).into(binding.UserProfile);
                                }
                                binding.UserName.setText(userName != null ? userName : "Username");
                                binding.UserBio.setText(userBio != null ? userBio : "Bio");
                                binding.postsCount.setText((postsCnt != null ? postsCnt : 0) + "\nPosts");

                            } else {
                                Log.d(TAG, "No such document");
                            }
                        } else {
                            Log.d(TAG, "get failed with ", task.getException());
                        }
                    }
                });
    }

    private void loadPosts() {
        db.collection("users").document(currentUser.getUid()).collection("posts")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            posts.clear();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                String imageUrl = document.getString("imageUrl");
                                String caption = document.getString("caption");
                                posts.add(new Post(imageUrl, caption));
                            }
                            postAdapter.notifyDataSetChanged();
                        } else {
                            Log.d(TAG, "Error getting posts: ", task.getException());
                        }
                    }
                });
    }
    // ⭐ Helper method for average rating
    private void loadAverageRating(TextView averageRatingText) {
        db.collection("users").document(currentUser.getUid())
                .collection("ratings")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        double total = 0;
                        int count = 0;
                        for (DocumentSnapshot doc : task.getResult()) {
                            Double rating = doc.getDouble("rating");
                            if (rating != null) {
                                total += rating;
                                count++;
                            }
                        }
                        double average = count > 0 ? total / count : 0;
                        averageRatingText.setText("Average Rating: " + String.format("%.1f", average));
                        binding.RatingBar.setRating((float) average);
                    } else {
                        Log.w(TAG, "Error getting ratings", task.getException());
                    }
                });
}}