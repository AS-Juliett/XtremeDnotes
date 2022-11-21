package com.example.xtremednotes.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import de.hdodenhof.circleimageview.CircleImageView;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;

import com.example.xtremednotes.ConfigUtil;
import com.example.xtremednotes.R;
import com.example.xtremednotes.fragment.ListAllNotesFragment;
import com.example.xtremednotes.fragment.SettingsFragment;
import com.google.android.material.navigation.NavigationView;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class NotesNavigationActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private Toolbar toolbar;
    private DrawerLayout drawerLayout;
    private FrameLayout frameLayout;
    private NavigationView navigationView;
    private CircleImageView imageNav;
    private String exportName = "export";
    private static String manage = "Manage categories";
    private static String defaultCategory = "Uncategorized";
    private Object[] categoriesList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notes_navigation);

        initializeViews();
        toggleDrawer();
        initializeDefaultFragment(savedInstanceState,1);
    }

    private void initializeViews() {
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        drawerLayout = findViewById(R.id.drawerLayout);
        frameLayout = findViewById(R.id.frameLayout);
        navigationView = findViewById(R.id.navigationView);
        navigationView.setNavigationItemSelectedListener(this);
        getCategories();
        View nView = navigationView.getHeaderView(0);
        imageNav = nView.findViewById(R.id.imageNav);
        ConfigUtil.setAvatar(this, imageNav);

        Button manageBtn = findViewById(R.id.manageButton);
        manageBtn.setOnClickListener(view -> onClickManage());
    }

    private void initializeDefaultFragment(Bundle savedInstanceState, int itemIndex){
        if (savedInstanceState == null){
            MenuItem menuItem = navigationView.getMenu().getItem(itemIndex).setChecked(true);
            onNavigationItemSelected(menuItem);
        }
    }

    private void toggleDrawer() {
        ActionBarDrawerToggle drawerToggle = new ActionBarDrawerToggle(this, drawerLayout,
                toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(drawerToggle);
        drawerToggle.syncState();
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        }
        else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        switch (menuItem.getItemId()){
            case R.id.nav_settings:
                getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout, new SettingsFragment())
                        .commit();
                deSelectCheckedState();
                closeDrawer();
                break;
            case R.id.nav_all_notes:
                getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout, new ListAllNotesFragment(null))
                        .commit();
                deSelectCheckedState();
                closeDrawer();
                break;
            default:
                getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout,
                            new ListAllNotesFragment((String) menuItem.getTitle())).commit();
                deSelectCheckedState();
                closeDrawer();
        }
        return false;
    }

    private void getCategories(){
        Supplier<Stream<String>> dirSupplier = () ->
                Arrays.stream(Objects.requireNonNull(this.getFilesDir().listFiles(File::isDirectory))).map(File::getName);
        categoriesList = dirSupplier.get().toArray();
        //Log.d("dir no", String.valueOf(categoriesList.length));
        //for (Object dir: categoriesList) { Log.d("dir", dir.toString()); }
        //dirSupplier.get().forEach((dir) -> Log.d("dir", dir));
        Menu subMenu = navigationView.getMenu().addSubMenu("Categories");

        MenuItem defItem = subMenu.add(defaultCategory);
        defItem.setIcon(R.mipmap.ic_note_foreground);

        for (Object dir: categoriesList) {
            MenuItem mi = subMenu.add(dir.toString());
            mi.setIcon(R.mipmap.ic_note_foreground);
        }
    }

    private void closeDrawer(){
        if (drawerLayout.isDrawerOpen(GravityCompat.START)){
            drawerLayout.closeDrawer(GravityCompat.START);
        }
    }

    private void deSelectCheckedState(){
        int noOfItems = navigationView.getMenu().size();
        for (int i=0; i<noOfItems;i++){
            navigationView.getMenu().getItem(i).setChecked(false);
        }
    }

    private void onClickManage(){
        Intent i = new Intent(this, ManageCategoriesActivity.class);
        NotesNavigationActivity.this.startActivity(i);
    }

    @Override
    protected void onResume() {
        super.onResume();
        ConfigUtil.setAvatar(this, imageNav);
    }
}