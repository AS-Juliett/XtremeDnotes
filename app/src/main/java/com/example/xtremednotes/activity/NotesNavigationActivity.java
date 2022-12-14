package com.example.xtremednotes.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import de.hdodenhof.circleimageview.CircleImageView;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;

import com.example.xtremednotes.util.ConfigUtil;
import com.example.xtremednotes.R;
import com.example.xtremednotes.fragment.ListAllNotesFragment;
import com.example.xtremednotes.fragment.SettingsFragment;
import com.google.android.material.navigation.NavigationView;

public class NotesNavigationActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private Toolbar toolbar;
    private DrawerLayout drawerLayout;
    private FrameLayout frameLayout;
    private NavigationView navigationView;
    private CircleImageView imageNav;
    private Menu subMenu;
    private String exportName = "export";
    private static String defaultFolder = "Uncategorized";

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
        subMenu = navigationView.getMenu().addSubMenu("Folders");
        ConfigUtil.setFolders(this, subMenu, defaultFolder);
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
        Intent i = new Intent(this, ManageFoldersActivity.class);
        NotesNavigationActivity.this.startActivity(i);
    }

    @Override
    protected void onResume() {
        super.onResume();
        ConfigUtil.setAvatar(this, imageNav);
        ConfigUtil.setFolders(this, subMenu, defaultFolder);
    }
}