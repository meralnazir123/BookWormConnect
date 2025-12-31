package com.example.bookwormconnect;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity{
    ImageButton Camera;
    RecyclerView recyclerView;
    ArrayList<postEntity> postList;
    postAdapter adapter;
    FirebaseFirestore firestore;
    SearchView searchView;
    ActivityResultLauncher<Intent> imagePickerLauncher;
    ActivityResultLauncher<String> cameraPermissionLauncher;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);

        searchView=findViewById(R.id.searchView);
        Camera=findViewById(R.id.camera);
        recyclerView=findViewById(R.id.RecyclerV);

        postList=new ArrayList<>();
        adapter=new postAdapter(postList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
        firestore =FirebaseFirestore.getInstance();

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
        imagePickerLauncher=registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                result-> {
                    if(result.getResultCode()==RESULT_OK && result.getData()!=null){
                        Bitmap bitmap=null;
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
                             bitmap=(Bitmap) result.getData().getExtras().get("data");
                        }
                        if (bitmap!=null)
                            fetchUsernameAndUpload(bitmap);
                    }
                });

        Camera.setOnClickListener(v -> {
            if (checkSelfPermission(android.Manifest.permission.CAMERA)
                    == PackageManager.PERMISSION_GRANTED) {

                openCameraChooser();

            } else {
                cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA);
            }
        });

        DrawerLayout drawer=findViewById(R.id.drawerlayout);
        Toolbar toolbar=findViewById(R.id.toolbar);
        NavigationView navigationView=findViewById(R.id.navD);
        searchView.clearFocus();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }
            @Override
            public boolean onQueryTextChange(String newText) {
                filterText(newText);
                return true;
            }
        });
        setSupportActionBar(toolbar);
        ActionBarDrawerToggle toggle=new ActionBarDrawerToggle(
                this, drawer,toolbar,R.string.open_nav,R.string.close_nav);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id=item.getItemId();
                if(id==R.id.profile){
                    loadFragment(new profileFragment());
                }else if(id==R.id.MyBooks){
                    loadFragment(new MybooksFragment());
                }
                else if(id==R.id.home){
                    Intent intent = new Intent(HomeActivity.this, HomeActivity.class);
                    startActivity(intent);
                    finish();
                }
                else if(id==R.id.chat){
                    loadFragment(new ChatlistFragment());
                }
                else if(id==R.id.cat){
                    loadFragment(new CategoriesFragment());
                }
                else if(id==R.id.setting){
                    loadFragment(new settingFragment());
                }
                else{  //logout
                    startActivity(new Intent(HomeActivity.this,LoginActivity.class));
                }
                drawer.closeDrawer(GravityCompat.START);
                return true;
            }
        });

        loadFromRoom();

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
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        DatabaseReference userRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(uid);

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (snapshot.exists()) {
                    String username = snapshot.child("username").getValue(String.class);

                    if (username != null) {

                            TempPostHolder.username = username;
                            TempPostHolder.bitmap=bitmap;

                            startActivity(
                                    new Intent(HomeActivity.this, PostDetailActivity.class)
                            );
                    } else {
                        Toast.makeText(HomeActivity.this,
                                "Username field missing",
                                Toast.LENGTH_SHORT).show();
                    }

                } else {
                    Toast.makeText(HomeActivity.this,
                            "User record not found",
                            Toast.LENGTH_SHORT).show();
                }
            }
            
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(HomeActivity.this,
                        error.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
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
                        postEntity entity = new postEntity();
                        entity.imageUrl = post.getUrl();
                        entity.username = post.getUsername();
                        entity.bookType = post.getBookType();
                        entity.description = post.getDescription();
                        entity.time = post.getTime();

                        AppDatabase.getInstance(this).postDao().insert(entity);
                        postList.add(entity);
                    }
                        adapter.notifyDataSetChanged();
                  loadFromRoom();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this,
                                "Failed to load posts",
                                Toast.LENGTH_SHORT).show());
    }

    private void loadFromRoom() {

     List<postEntity> roomPosts =
             AppDatabase.getInstance(this)
                     .postDao()
                     .getAllPosts();

            postList.clear();

        postList.addAll(roomPosts);

        adapter.notifyDataSetChanged();
    }
    private void filterText(String newText) {
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer;
        drawer=findViewById(R.id.drawerlayout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
    private void loadFragment(Fragment fragment) {
        FragmentManager fm=getSupportFragmentManager();
        FragmentTransaction ft=fm.beginTransaction();
        ft.replace(R.id.frame,fragment);
        ft.addToBackStack(null);
        ft.commit();
    }
    public void hideSearchBar() {
        searchView.setVisibility(View.GONE);
    }
    public void hideCamera() {
        Camera.setVisibility(View.GONE);
    }
    @Override
    protected void onResume() {
        super.onResume();
        loadFromRoom();
    }
}