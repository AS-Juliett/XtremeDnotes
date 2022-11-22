package com.example.xtremednotes.activity;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NavUtils;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.xtremednotes.adapter.FoldersAdapter;
import com.example.xtremednotes.model.Folder;
import com.example.xtremednotes.util.ConfigUtil;
import com.example.xtremednotes.R;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

public class ManageFoldersActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private FoldersAdapter foldersAdapter;
    private ArrayList<Folder> folderList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_folders_activity);
        initializeViews();
    }

    private void initializeViews() {
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Manage folders");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        RecyclerView recyclerView = findViewById(R.id.foldersView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        folderList = new ArrayList<>();
        loadFolders();

        foldersAdapter = new FoldersAdapter(folderList,
                folder -> editName(),
                (adapter, folder) -> {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);

                    builder.setTitle("Confirm");
                    builder.setMessage("Delete " + ConfigUtil.decodeBase64(folder.getName()) + "?");
                    builder.setPositiveButton("Yes", (dialog, which) -> {
                        File file = new File(this.getFilesDir(), folder.getName());
                        if(file.exists()){
                            file.delete();
                            Toast.makeText(this,
                                    ConfigUtil.decodeBase64(folder.getName())+" deleted",
                                    Toast.LENGTH_SHORT).show();
                            folderList.remove(folder);
                            adapter.setData(folderList);
                            adapter.notifyDataSetChanged();
                        }
                    });
                    builder.setNegativeButton("No", null);
                    AlertDialog alert = builder.create();
                    alert.show();
                });
        recyclerView.setAdapter(foldersAdapter);

        Button createButton = findViewById(R.id.createFolderButton);
        createButton.setOnClickListener(view -> onClickCreate());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void editName(){
        Toast.makeText(ManageFoldersActivity.this, "To do edit", Toast.LENGTH_SHORT).show();
    }

    private void onClickCreate(){
        EditText et = new EditText(this);
        et.setHint(R.string.folder_hint);
        new android.app.AlertDialog.Builder(this)
                .setTitle("Create new folder")
                .setView(et)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String folderName = ConfigUtil.encodeBase64(et.getText().toString());
                        if(validateName(folderName)){
                            Toast.makeText(ManageFoldersActivity.this,
                                    "Folder with given name already exists",
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            createNewFolder(folderName);
                            loadFolders();
                        }
                    }
                })
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }

    private void createNewFolder(String folderName){
        File dir = new File(this.getFilesDir(), folderName);
        try{
            if(dir.mkdir()) {
                Toast.makeText(ManageFoldersActivity.this, "Created folder " + ConfigUtil.decodeBase64(folderName), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(ManageFoldersActivity.this, "Error", Toast.LENGTH_SHORT).show();
            }
        } catch(Exception e){
            e.printStackTrace();
        }
    }

    private boolean validateName(String folderName){
        File f = new File(this.getFilesDir(),folderName);
        return f.isDirectory();
    }

    private void loadFolders(){
        folderList.clear();
        folderList.addAll(ConfigUtil.convertObjectToList(ConfigUtil.getFolders(this)));
    }

    @Override
    public void onResume() {
        super.onResume();
        loadFolders();
        foldersAdapter.notifyDataSetChanged();
    }
}