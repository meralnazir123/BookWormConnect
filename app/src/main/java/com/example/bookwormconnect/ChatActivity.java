package com.example.bookwormconnect;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.auth.User;

import java.util.ArrayList;

public class ChatActivity extends AppCompatActivity {

    FirebaseAuth auth;
    RecyclerView chatUserRecyclerView;
    UserAdapter adapter;
    FirebaseDatabase database;
    ArrayList<Users> userArrayList;

    ImageView cambut,settingbut,chatbut;


    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        if (getSupportActionBar() != null){
            getSupportActionBar().hide();
        }

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        // Find Views
        chatUserRecyclerView = findViewById(R.id.chatUserRecyclerView);
        cambut = findViewById(R.id.cambut);
        settingbut = findViewById(R.id.settingbut);
        chatbut = findViewById(R.id.chatbut);


        // Create Arraylist
        userArrayList = new ArrayList<>();

        //RecyclerView Setup
        chatUserRecyclerView.setLayoutManager(new LinearLayoutManager(this)
        );
        adapter = new UserAdapter(ChatActivity.this,userArrayList);
        chatUserRecyclerView.setAdapter(adapter);

        // Firebase Reference
        DatabaseReference reference = database.getReference().child("user");

        reference.addValueEventListener(new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userArrayList.clear();

                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Users users = dataSnapshot.getValue(Users.class);
                    if (users != null) {
                        userArrayList.add(users);
                    }
                }
                adapter.notifyDataSetChanged();
            }


            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });



        settingbut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ChatActivity.this, settingFragment.class);
                startActivity(intent);
            }
        });
        chatbut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ChatActivity.this,chatwin.class);
                startActivity(intent);
            }
        });
        cambut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent,100);
            }
        });



        if (auth.getCurrentUser() == null) {
            Intent intent = new Intent(ChatActivity.this,EditProfileActivity.class);
            startActivity(intent);
            finish();
        }
    }



}