package com.example.bookwormconnect;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;

public class HomeActivity extends AppCompatActivity {
    RecyclerView recyclerView;
    ArrayList<postmodel> postList;
    postAdapter adapter;
    FirebaseFirestore firestore;
    SearchView searchView;
    ActivityResultLauncher<Intent> imagePickerLauncher;
    ActivityResultLauncher<String> cameraPermissionLauncher;

    ImageButton requestButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);

        searchView = findViewById(R.id.searchView);

        FirebaseMessaging.getInstance().getToken()
                .addOnSuccessListener(token -> {

                    String uid = FirebaseAuth.getInstance().getUid();

                    if (uid != null) {
                        FirebaseFirestore.getInstance()
                                .collection("users")
                                .document(uid)
                                .update("fcmToken", token);
                    }
                });
        recyclerView = findViewById(R.id.RecyclerV);
        requestButton = findViewById(R.id.requestButton);
        postList = new ArrayList<>();
        adapter = new postAdapter(postList, "HOME");
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
        firestore = FirebaseFirestore.getInstance();
        loadImages();
        cameraPermissionLauncher =
                registerForActivityResult(
                        new ActivityResultContracts.RequestPermission(),
                        isGranted -> {
                            if (isGranted) {
                                openCameraChooser();
                            } else {
                                Toast.makeText(this,
                                        "Camera permission required",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                );
        imagePickerLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Bitmap bitmap = null;
                        if (result.getData().getData() != null) {
                            try {
                                bitmap = MediaStore.Images.Media.getBitmap(
                                        this.getContentResolver(),
                                        result.getData().getData()
                                );
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } else if (result.getData().getExtras() != null) {
                            bitmap = (Bitmap) result.getData().getExtras().get("data");
                        }
                        if (bitmap != null)
                            fetchUsernameAndUpload(bitmap);
                    }
                });
        DrawerLayout drawer = findViewById(R.id.drawerlayout);
        Toolbar toolbar = findViewById(R.id.toolbar);
        NavigationView navigationView = findViewById(R.id.navD);
        String currentUserId = FirebaseAuth.getInstance().getUid();

        MenuItem adminItem = navigationView.getMenu().findItem(R.id.admin);

        if (currentUserId != null &&
                currentUserId.equals("21fLX4JQh5Rvic4kNEdZgHM38ih2")) {

            adminItem.setVisible(true);

        } else {
            adminItem.setVisible(false);
        }
        searchView.clearFocus();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.isEmpty()) {
                    adapter.setFilteredList(postList);
                } else {
                    filterList(newText);
                }
                return true;
            }
        });
        setSupportActionBar(toolbar);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.open_nav, R.string.close_nav);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            Fragment fragment = null;

            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                if (id == R.id.profile) {
                    Intent intent = new Intent(HomeActivity.this, profilescreen.class);
                    startActivity(intent);
                } else if (id == R.id.MyBooks) {
                    loadFragment(new MybooksFragment());
                }
                else if (item.getItemId() == R.id.cat) {
                    return false;
                }else if (id == R.id.novels) {
                    fragment = CategoriesFragment.newInstance("Novel");
                } else if (id == R.id.textbooks) {
                    fragment = CategoriesFragment.newInstance("Text Book");
                } else if (id == R.id.home) {
                    recyclerView.setVisibility(View.VISIBLE);
                    searchView.setVisibility(View.VISIBLE);

                    getSupportFragmentManager()
                            .popBackStack(null,
                                    FragmentManager.POP_BACK_STACK_INCLUSIVE);
                    drawer.closeDrawer(GravityCompat.START);
                    return true;
                } else if (id == R.id.chat) {
                    recyclerView.setVisibility(View.GONE);
                    searchView.setVisibility(View.GONE);
                    loadFragment(new ChatlistFragment());
                } else if (id == R.id.request) {
                    Intent intent = new Intent(HomeActivity.this, requestActivity.class);
                    startActivity(intent);
                }
                else if (id == R.id.about) {
                    Intent intent = new Intent(HomeActivity.this, AboutActivity.class);
                    startActivity(intent);
                }else if (id == R.id.post) {
                    if (checkSelfPermission(android.Manifest.permission.CAMERA)
                            == PackageManager.PERMISSION_GRANTED) {
                        openCameraChooser();
                    } else {
                        cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA);
                    }
                } else if (id == R.id.setting) {
                    loadFragment(new settingFragment());
                }else if (id == R.id.admin) {
                    Intent intent = new Intent(HomeActivity.this, AdminActivity.class);
                    startActivity(intent);
                } else if (id == R.id.logout) {

                new AlertDialog.Builder(HomeActivity.this)
                        .setTitle("Logout")
                        .setMessage("Are you sure you want to logout?")
                        .setPositiveButton("Yes", (dialog, which) -> {

                            FirebaseAuth.getInstance().signOut();

                            Intent intent = new Intent(
                                    HomeActivity.this,
                                    LoginActivity.class
                            );

                            intent.setFlags(
                                    Intent.FLAG_ACTIVITY_NEW_TASK |
                                            Intent.FLAG_ACTIVITY_CLEAR_TASK
                            );

                            startActivity(intent);
                            finish();
                        })
                        .setNegativeButton("No", (dialog, which) -> {
                            dialog.dismiss();
                        })
                        .show();
            }
                if (fragment != null) {
                    FrameLayout frame = findViewById(R.id.frame);

                    getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.frame, fragment)
                            .addToBackStack(null)
                            .commit();
                }
                drawer.closeDrawer(GravityCompat.START);
                return true;
            }
        });

    }
    private void filterList(String text) {
        ArrayList<postmodel> filteredList = new ArrayList<>();

        for (postmodel post : postList) {

            if (
                    (post.description != null &&
                            post.description.toLowerCase().contains(text.toLowerCase()))
                            ||
                            (post.username != null &&
                                    post.username.toLowerCase().contains(text.toLowerCase()))
                            ||
                            (post.bookType != null &&
                                    post.bookType.toLowerCase().contains(text.toLowerCase()))
            ) {
                filteredList.add(post);
            }
        }

        if (filteredList.isEmpty()) {
            Toast.makeText(this, "No posts found", Toast.LENGTH_SHORT).show();
            adapter.setFilteredList(new ArrayList<>());
        } else {
            adapter.setFilteredList(filteredList);
        }
    }

    private void openCameraChooser() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        Intent galleryIntent = new Intent(
                Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        );

        Intent chooser = Intent.createChooser(galleryIntent, "Select Image");
        chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS,
                new Intent[]{cameraIntent});

        imagePickerLauncher.launch(chooser);

    }

    private void fetchUsernameAndUpload(Bitmap bitmap) {

        String uid = FirebaseAuth.getInstance().getUid();

        if (uid == null) {
            Toast.makeText(
                    this,
                    "User not logged in",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        FirebaseDatabase.getInstance()
                .getReference("users")
                .child(uid)
                .get()
                .addOnSuccessListener(snapshot -> {

                    if (snapshot.exists()) {

                        String username =
                                snapshot.child("username")
                                        .getValue(String.class);

                        if (username != null &&
                                !username.isEmpty()) {

                            TempPostHolder.username = username;
                            TempPostHolder.bitmap = bitmap;

                            startActivity(
                                    new Intent(
                                            HomeActivity.this,
                                            PostDetailActivity.class
                                    )
                            );

                        } else {

                            Toast.makeText(
                                    this,
                                    "Username not found",
                                    Toast.LENGTH_SHORT
                            ).show();
                        }

                    } else {

                        Toast.makeText(
                                this,
                                "User record not found",
                                Toast.LENGTH_SHORT
                        ).show();
                    }

                })
                .addOnFailureListener(e ->
                        Toast.makeText(
                                this,
                                "Failed to load user: " + e.getMessage(),
                                Toast.LENGTH_SHORT
                        ).show()
                );
    }

    private void loadImages() {
        FirebaseFirestore.getInstance()
                .collection("posts")
                .orderBy("time", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {

                    postList.clear();

                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        postmodel post = doc.toObject(postmodel.class);
                        post.docId = doc.getId();

                        postList.add(post);
                    }

                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(HomeActivity.this,
                                "Failed to load posts",
                                Toast.LENGTH_SHORT).show());
    }
    @Override
    public void onBackPressed() {

        DrawerLayout drawer = findViewById(R.id.drawerlayout);

        if (drawer.isDrawerOpen(GravityCompat.START)) {

            drawer.closeDrawer(GravityCompat.START);

        } else if (getSupportFragmentManager().getBackStackEntryCount() > 0) {

            getSupportFragmentManager().popBackStack();

            recyclerView.setVisibility(View.VISIBLE);
            searchView.setVisibility(View.VISIBLE);

        } else {

            super.onBackPressed();
        }
    }
    private void loadFragment(Fragment fragment) {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.frame, fragment);
        ft.addToBackStack(null);
        ft.commit();
    }
    public void hideSearchBar() {
        searchView.setVisibility(View.GONE);
    }
    protected void onResume() {
        super.onResume();
        loadImages();
    }
}