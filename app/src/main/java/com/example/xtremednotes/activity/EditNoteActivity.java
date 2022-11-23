package com.example.xtremednotes.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.MenuItemCompat;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.xtremednotes.Config;
import com.example.xtremednotes.EncryptedFileManager;
import com.example.xtremednotes.util.FileUtil;
import com.example.xtremednotes.R;
import com.example.xtremednotes.util.ConfigUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Base64;

public class EditNoteActivity extends AppCompatActivity {

    public static String EDIT_NAME_KEY = "editName";
    public static String FOLDER_KEY = "folder";
    private Toolbar toolbar;
    private String editName;
    private String folderName;
    private EditText noteText;
    private String selectedFolder;
    private LinearLayout background;
    private int selectedColor;
    private String[] colorList;
    private Menu menu;
    private MenuItem colorSpinner;
    private boolean isEditing = false;

    private String makeFullName(String folder, String name) {
        String ret = "";
        if (folder != null && !folder.equals("files")) {
            ret += folder + "/";
        }
        return ret + name;
    }

    private void setColor(int color) {
        background.setBackgroundColor(ConfigUtil.convertColor(color));
    }

    private void saveNote(String newName, String oldName) {
        String noteContent = noteText.getText().toString();
        try {
            String encName = FileUtil.fromNoteName(newName);
            if (isEditing && (!selectedFolder.equals(folderName) || !oldName.equals(newName))) {
                File f = new File(getFilesDir(), makeFullName(folderName, FileUtil.fromNoteName(oldName)));
                f.delete();
            }
            EncryptedFileManager.getInstance().saveFile(this, encName, selectedFolder, noteContent.getBytes(StandardCharsets.UTF_8));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private boolean validateTitle(String noteTitle){
        if(selectedFolder != null){
            return validate(noteTitle);
        } else {
            File file = new File(getFilesDir(),noteTitle);
            return file.exists();
        }
    }

    private boolean validate(String noteTile){
        if(noteTile != null){
            File fileParent = new File(getFilesDir(), selectedFolder);
            File file = new File(fileParent, noteTile);
            return file.exists();
        }
        return false;
    }

    public void onClickSaveNote() {
        EditText et = new EditText(this);
        final String suggestedName = editName != null ? editName.substring(0, editName.lastIndexOf(".")) : "";
        et.setText(suggestedName);
        new AlertDialog.Builder(this)
                .setTitle("Save as")
                .setView(et)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String noteTitle = et.getText().toString();
                        if(!(suggestedName.equals(noteTitle) && selectedFolder.equals(folderName)) && validateTitle(FileUtil.fromNoteName(noteTitle+".txt"))){
                            Toast.makeText(EditNoteActivity.this,
                                    "Note with given title already exists",
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            String oldName = editName;
                            editName = noteTitle + ".txt";
                            Intent data = new Intent();
                            data.putExtra("NOTE_TITLE", editName);
                            data.putExtra("NOTE_FOLDER", selectedFolder);
                            setResult(RESULT_OK, data);
                            saveNote(editName, oldName);
                            EditNoteActivity.this.finish();
                        }
                    }
                })
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_note);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        background = findViewById(R.id.background);
        colorList = ConfigUtil.getColors();

        noteText = findViewById(R.id.editTextTextMultiLine);
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            editName = extras.getString(EDIT_NAME_KEY);
            folderName = extras.getString(FOLDER_KEY);
            selectedFolder = folderName;
            isEditing = true;
        }
        if (editName != null) {
            String fileName = FileUtil.fromNoteName(editName);
            getSupportActionBar().setTitle(editName.substring(0, editName.lastIndexOf(".")));
            try {
                File file;
                if(folderName == null || folderName.equals("files")){
                    file = new File(getFilesDir(), fileName);
                } else{
                    File parent = new File(getFilesDir(), folderName);
                    file = new File(parent, fileName);
                }
                byte[] byts = EncryptedFileManager.getInstance().readFile(file);
                Log.d("WKD", ""+byts.length);
                noteText.setText(new String(byts));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
        else{
            getSupportActionBar().setTitle("");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_note, menu);
        colorSpinner = menu.findItem(R.id.colorSpinner);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, colorList);
        Spinner spinner = (Spinner) MenuItemCompat.getActionView(colorSpinner);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                setColor(i);
            }

            public void onNothingSelected(AdapterView<?> adapterView) {
                return;
            }
        });

        this.menu = menu;
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_save:
                onClickSaveNote();
                return (true);
            case R.id.action_select_folder:
                selectFolder();
        }
        return(super.onOptionsItemSelected(item));
    }

    public void selectFolder(){
        Spinner folderSelector = new Spinner(this);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, ConfigUtil.convertObject(ConfigUtil.getFolders(this)));
        folderSelector.setAdapter(adapter);

        new AlertDialog.Builder(this)
                .setTitle("Choose a folder")
                .setView(folderSelector)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        selectedFolder = ConfigUtil.encodeBase64(folderSelector.getSelectedItem().toString());
                        if(validate(editName)){
                            Toast.makeText(EditNoteActivity.this,
                                    "Note with the same title already exists in the selected folder",
                                    Toast.LENGTH_SHORT).show();
                            selectedFolder = folderName;
                        }
                    }
                })
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }
}