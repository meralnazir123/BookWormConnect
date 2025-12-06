package com.example.bookwormconnect;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.navigation.NavigationView;
public class HomeActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);
        DrawerLayout drawer=findViewById(R.id.drawerlayout);
        Toolbar toolbar=findViewById(R.id.toolbar);
        NavigationView navigationView=findViewById(R.id.navD);
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
                    loadFragment(new chat());
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