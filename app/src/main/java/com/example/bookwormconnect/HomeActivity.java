package com.example.bookwormconnect;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
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
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.Firebase;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class HomeActivity extends AppCompatActivity {

    ImageView Camera;
    RecyclerView recyclerView;
    ArrayList<String> imageList;
    ImageAdapter adapter;
    FirebaseFirestore firestore;



    ActivityResultLauncher<Intent> cameraLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        loadImages();
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);
        Camera=findViewById(R.id.camera);
        recyclerView=findViewById(R.id.RecyclerV);
        imageList=new ArrayList<>();

        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        adapter=new ImageAdapter(imageList);
        recyclerView.setAdapter(adapter);
       firestore =FirebaseFirestore.getInstance();


        cameraLauncher=registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                result-> {
            if(result.getResultCode()==RESULT_OK){
                Bitmap bitmap=(Bitmap) result.getData().getExtras().get("data");
                uploadImage(bitmap);
            }
                });

        Camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                cameraLauncher.launch(intent);
            }
        });


        DrawerLayout drawer=findViewById(R.id.drawerlayout);
        Toolbar toolbar=findViewById(R.id.toolbar);
        NavigationView navigationView=findViewById(R.id.navD);
        SearchView searchView=findViewById(R.id.searchView);
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
    }

    private void loadImages() {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();

        firestore.collection("images")
                .orderBy("time")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {

                    imageList.clear();

                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        String url = doc.getString("url");
                        imageList.add(url);
                    }

                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this,
                                "Failed to load images",
                                Toast.LENGTH_SHORT).show());
    }

    private void uploadImage(Bitmap bitmap) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();

        FirebaseFirestore firestore = FirebaseFirestore.getInstance();

        ByteArrayOutputStream baos=new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data= baos.toByteArray();

        StorageReference imageRef =
                storageRef.child("images/" + System.currentTimeMillis() + ".jpg");

        imageRef.putBytes(data).addOnSuccessListener(taskSnapshot ->
                imageRef.getDownloadUrl().addOnSuccessListener(uri -> {

                    Map<String, Object> imageData = new HashMap<>();
                    imageData.put("url", uri.toString());
                    imageData.put("time", System.currentTimeMillis());

                    firestore.collection("images")
                                    .add(imageData).addOnSuccessListener(doc ->{
                                imageList.add(uri.toString());
                                adapter.notifyItemInserted(imageList.size()-1);
                            });
                    Toast.makeText(this,
                            "Image uploaded",
                            Toast.LENGTH_SHORT).show();

                }))
                .addOnFailureListener(e -> {
                    Toast.makeText(this,"Firestore Failed",Toast.LENGTH_SHORT).show();
    });
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
        ft.add(R.id.frame,fragment);
        ft.commit();
    }
}