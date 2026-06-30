package com.example.bookwormconnect;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.RatingBar;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import com.example.bookwormconnect.databinding.ActivityProfilescreenBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import androidx.annotation.NonNull;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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
    private String profileUserId;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfilescreenBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        FirebaseApp.initializeApp(this);
        db = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser == null) {
            finish();
            return;
        }
        profileUserId = getIntent().getStringExtra("USER_ID");

        if (profileUserId == null) {
            profileUserId = currentUser.getUid();
        }
        if (profileUserId.equals(currentUser.getUid())) {
            binding.btnEditProfile.setVisibility(android.view.View.VISIBLE);
        } else {
            binding.btnEditProfile.setVisibility(android.view.View.GONE);
        }

        posts= new ArrayList<>();
        postAdapter = new PostAdapter2(posts);
        binding.PostsRecycler.setLayoutManager(new GridLayoutManager(this, 3));
        binding.PostsRecycler.setAdapter(postAdapter);
        loadProfileData();
        loadPosts();
        binding.btnEditProfile.setOnClickListener(v -> {
            Intent intent = new Intent(profilescreen.this, EditProfileActivity.class);
            startActivity(intent);
        });
        RatingBar ratingBar = binding.RatingBar;
        TextView averageRatingText = binding.averageRatingText;

        loadAverageRating(averageRatingText);

        if(profileUserId.equals(currentUser.getUid())){

            ratingBar.setIsIndicator(true);

        }else{

            ratingBar.setIsIndicator(false);

            loadUserRating();

            ratingBar.setOnRatingBarChangeListener((bar, rating, fromUser) -> {

                if(fromUser){

                    db.collection("users")
                            .document(profileUserId)
                            .collection("ratings")
                            .document(currentUser.getUid())
                            .set(new Rating(rating))
                            .addOnSuccessListener(unused -> {

                                loadAverageRating(binding.averageRatingText);

                            });
                }
            });
        }
    }

    private void loadProfileData() {
        db.collection("users").document(profileUserId).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                String profileImageUrl = document.getString("profileImageUrl");
                                String userName = document.getString("username");
                                String userBio = document.getString("bio");
                                if (profileImageUrl != null && !profileImageUrl.isEmpty()) {

                                    Glide.with(profilescreen.this)
                                            .load(profileImageUrl)
                                            .into(binding.UserProfile);

                                } else {

                                    binding.UserProfile.setImageResource(R.drawable.user);
                                }
                                binding.UserName.setText(userName != null ? userName : "Username");
                                binding.UserBio.setText(userBio != null ? userBio : "Bio");


                            } else {
                                Log.d(TAG, "No such document");
                            }
                        } else {
                            Log.d(TAG, "get failed with ", task.getException());
                        }
                    }
                });
    }
    private void loadUserRating() {

        db.collection("users")
                .document(profileUserId)
                .collection("ratings")
                .document(currentUser.getUid())
                .get()
                .addOnSuccessListener(documentSnapshot -> {

                    if(documentSnapshot.exists()){

                        Double rating =
                                documentSnapshot.getDouble("rating");

                        if(rating != null){

                            binding.RatingBar.setRating(
                                    rating.floatValue()
                            );
                        }
                    }
                });
    }
    private void loadPosts() {

        db.collection("posts")
                .whereEqualTo("userId", profileUserId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {

                    posts.clear();

                    for (DocumentSnapshot document : queryDocumentSnapshots) {

                        String imageUrl = document.getString("url");
                        String description = document.getString("description");

                        posts.add(new Post(imageUrl, description));
                    }

                    postAdapter.notifyDataSetChanged();

                    binding.postsCount.setText(posts.size() + "\nPosts");

                })
                .addOnFailureListener(e ->
                        Log.e(TAG, "Error loading posts", e));
    }
    private void loadAverageRating(TextView averageRatingText) {
        db.collection("users")
                .document(profileUserId)
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

                        binding.RatingBar.setRating((float) average);

                        averageRatingText.setText(
                                "Average Rating: " + String.format("%.1f", average)
                        );

                    }
                });
    }
    @Override
    protected void onResume() {
        super.onResume();

        loadProfileData();
        loadPosts();
        loadAverageRating(binding.averageRatingText);
    }

}