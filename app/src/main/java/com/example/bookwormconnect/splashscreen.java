package com.example.bookwormconnect;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.VideoView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class splashscreen extends AppCompatActivity {

    private VideoView valVideo;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_splashscreen);
        valVideo=findViewById(R.id.Splash);
      Uri videopath=Uri.parse("android.resource://"+getPackageName()+"/"+R.raw.splash_screen);
        valVideo.setVideoURI(videopath);

        valVideo.setOnCompletionListener(mp -> {
            Intent intent=new Intent(splashscreen.this,MainActivity.class);
            startActivity(intent);
            finish();
        });
        valVideo.start();

    }
}