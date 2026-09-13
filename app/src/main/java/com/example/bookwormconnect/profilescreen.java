package com.example.bookwormconnect;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RatingBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;

import com.bumptech.glide.Glide;
import com.example.bookwormconnect.databinding.ActivityProfilescreenBinding;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class profilescreen extends AppCompatActivity {

    private static final String TAG = "ProfileActivity";

    private ActivityProfilescreenBinding binding;
    private PostAdapter2 postAdapter;
    private List<Post> posts;

    private FirebaseFirestore db;
    private DatabaseReference usersReference;

    private String profileUserId;
    private FirebaseUser currentUser;

    private boolean isOwnProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityProfilescreenBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        FirebaseApp.initializeApp(this);

        db = FirebaseFirestore.getInstance();

        usersReference = FirebaseDatabase.getInstance()
                .getReference("users");

        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser == null) {
            finish();
            return;
        }

        profileUserId = getIntent().getStringExtra("USER_ID");

        if (profileUserId == null) {
            profileUserId = currentUser.getUid();
        }

        isOwnProfile =
                profileUserId.equals(currentUser.getUid());

        if (isOwnProfile) {
            binding.btnEditProfile.setVisibility(View.VISIBLE);
        } else {
            binding.btnEditProfile.setVisibility(View.GONE);
        }

        posts = new ArrayList<>();

        postAdapter = new PostAdapter2(
                posts,
                isOwnProfile,
                new PostAdapter2.OnPostDeletedListener() {
                    @Override
                    public void onPostDeleted() {
                        binding.postsCount.setText(
                                posts.size() + "\nPosts"
                        );
                    }
                }
        );

        binding.PostsRecycler.setLayoutManager(
                new GridLayoutManager(this, 3)
        );

        binding.PostsRecycler.setAdapter(postAdapter);

        loadProfileData();
        loadPosts();
        loadAverageRating();

        RatingBar ratingBar = binding.RatingBar;

        if (isOwnProfile) {

            ratingBar.setIsIndicator(true);

        } else {

            ratingBar.setIsIndicator(false);

            loadUserRating();

            ratingBar.setOnRatingBarChangeListener(
                    (bar, rating, fromUser) -> {

                        if (fromUser) {

                            db.collection("users")
                                    .document(profileUserId)
                                    .collection("ratings")
                                    .document(currentUser.getUid())
                                    .set(new Rating(rating))
                                    .addOnSuccessListener(unused -> {

                                        loadAverageRating();

                                        Toast.makeText(
                                                profilescreen.this,
                                                "Rating updated",
                                                Toast.LENGTH_SHORT
                                        ).show();
                                    })
                                    .addOnFailureListener(e -> Toast.makeText(
                                            profilescreen.this,
                                            "Failed to update rating",
                                            Toast.LENGTH_SHORT
                                    ).show());
                        }
                    }
            );
        }

        binding.btnEditProfile.setOnClickListener(v -> {

            Intent intent = new Intent(
                    profilescreen.this,
                    EditProfileActivity.class
            );

            startActivity(intent);
        });
    }

    private void loadProfileData() {

        usersReference
                .child(profileUserId)
                .get()
                .addOnSuccessListener(snapshot -> {

                    if (snapshot.exists()) {

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

                            Glide.with(profilescreen.this)
                                    .load(profileImageUrl)
                                    .into(binding.UserProfile);

                        } else {

                            binding.UserProfile.setImageResource(
                                    R.drawable.user
                            );
                        }

                        if (userName != null &&
                                !userName.isEmpty()) {

                            binding.UserName.setText(userName);

                        } else {

                            binding.UserName.setText("Username");
                        }

                        if (userBio != null &&
                                !userBio.isEmpty()) {

                            binding.UserBio.setText(userBio);

                        } else {

                            binding.UserBio.setText("Bio");
                        }

                    } else {

                        binding.UserName.setText("Username");
                        binding.UserBio.setText("Bio");

                        binding.UserProfile.setImageResource(
                                R.drawable.user
                        );
                    }
                })
                .addOnFailureListener(e -> {

                    Log.e(
                            TAG,
                            "Failed to load profile data",
                            e
                    );

                    Toast.makeText(
                            profilescreen.this,
                            "Failed to load profile",
                            Toast.LENGTH_SHORT
                    ).show();
                });
    }

    private void loadUserRating() {

        if (isOwnProfile) {
            return;
        }

        db.collection("users")
                .document(profileUserId)
                .collection("ratings")
                .document(currentUser.getUid())
                .get()
                .addOnSuccessListener(documentSnapshot -> {

                    if (documentSnapshot.exists()) {

                        Double rating =
                                documentSnapshot.getDouble("rating");

                        if (rating != null) {

                            binding.RatingBar.setRating(
                                    rating.floatValue()
                            );
                        }

                    } else {

                        binding.RatingBar.setRating(0);
                    }
                })
                .addOnFailureListener(e -> Log.e(
                        TAG,
                        "Failed to load user rating",
                        e
                ));
    }

    private void loadAverageRating() {

        db.collection("users")
                .document(profileUserId)
                .collection("ratings")
                .get()
                .addOnSuccessListener(querySnapshot -> {

                    double total = 0;
                    int count = 0;

                    for (DocumentSnapshot document :
                            querySnapshot.getDocuments()) {

                        Double rating =
                                document.getDouble("rating");

                        if (rating != null) {

                            total += rating;
                            count++;
                        }
                    }

                    if (count > 0) {

                        double average =
                                total / count;

                        binding.averageRatingText.setText(
                                String.format(
                                        "%.1f",
                                        average
                                )
                        );

                    } else {

                        binding.averageRatingText.setText(
                                "No ratings"
                        );
                    }
                })
                .addOnFailureListener(e -> {

                    Log.e(
                            TAG,
                            "Failed to load average rating",
                            e
                    );

                    binding.averageRatingText.setText(
                            "No ratings"
                    );
                });
    }

    private void loadPosts() {

        db.collection("posts")
                .whereEqualTo("userId", profileUserId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {

                    posts.clear();

                    for (DocumentSnapshot document :
                            queryDocumentSnapshots) {

                        String imageUrl =
                                document.getString("url");

                        String description =
                                document.getString("description");

                        Post post = new Post(
                                imageUrl,
                                description
                        );

                        post.setDocId(
                                document.getId()
                        );

                        posts.add(post);
                    }

                    postAdapter.notifyDataSetChanged();

                    binding.postsCount.setText(
                            posts.size() + "\nPosts"
                    );

                    if (posts.isEmpty()) {

                        binding.noPostsText.setVisibility(
                                View.VISIBLE
                        );

                        binding.PostsRecycler.setVisibility(
                                View.GONE
                        );

                    } else {

                        binding.noPostsText.setVisibility(
                                View.GONE
                        );

                        binding.PostsRecycler.setVisibility(
                                View.VISIBLE
                        );
                    }
                })
                .addOnFailureListener(e -> {

                    Log.e(
                            TAG,
                            "Error loading posts",
                            e
                    );

                    binding.noPostsText.setVisibility(
                            View.VISIBLE
                    );

                    binding.noPostsText.setText(
                            "No posts uploaded"
                    );

                    binding.PostsRecycler.setVisibility(
                            View.GONE
                    );
                });
    }
}
