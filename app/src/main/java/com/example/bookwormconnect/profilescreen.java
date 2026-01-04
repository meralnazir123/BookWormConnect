package com.example.bookwormconnect;

import android.os.Binder;
import android.os.Bundle;
import android.renderscript.ScriptGroup;
import com.bumptech.glide.Glide;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.bookwormconnect.databinding.ActivityMainBinding;
import com.example.bookwormconnect.databinding.ActivityProfilescreenBinding;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import javax.net.ssl.SSLSessionBindingEvent;

public class profilescreen extends AppCompatActivity {
    ActivityProfilescreenBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityProfilescreenBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        loadUserData();
    }
    private void loadUserData() {
        binding.UserName.setText(FirebaseAuth.getInstance().getCurrentUser().getDisplayName());

        FirebaseFirestore.getInstance().collection("Users")
                .document(FirebaseAuth.getInstance().getUid())
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        UserModel userModel=documentSnapshot.toObject(UserModel.class);
                        if (userModel.getUserBio()!=null){
                            binding.UserBio.setText(userModel.getUserBio());
                        }else {
                            binding.UserBio.setText(R.string.bio);
                        }

                        if (userModel.getUserProfile()!=null){
                            Glide.with(profilescreen.this).load(userModel.getUserProfile())
                                    .into(binding.UserProfile);
                        }
                        if (userModel.getCoverPhoto()!=null){
                            Glide.with(profilescreen.this).load(userModel.getUserProfile())
                                    .into(binding.coverPhoto);
                        }
                    }
                });
    }
}
