package com.example.xtremednotes.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import com.example.xtremednotes.EncryptedFileManager;
import com.example.xtremednotes.FileUtil;
import com.example.xtremednotes.R;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class EditNoteActivity extends AppCompatActivity {

    public static String EDIT_NAME_KEY = "editName";
    private Toolbar toolbar;
    private String editName;
    private EditText noteText;

    private void saveNote() {
        String noteContent = noteText.getText().toString();
        try {
            EncryptedFileManager.getInstance().saveFile(this, FileUtil.fromNoteName(editName), noteContent.getBytes(StandardCharsets.UTF_8));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private boolean validateTitle(String noteTitle){
        File file = new File(getFilesDir()+"/"+noteTitle+".txt");
        return file.exists();
    }

    public void onClickSaveNote() {
        if (editName == null) {
            EditText et = new EditText(this);
            new AlertDialog.Builder(this)
                    .setTitle("Save as")
                    .setView(et)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            String noteTitle = et.getText().toString();
                            if(validateTitle(noteTitle)){
                                Toast.makeText(EditNoteActivity.this,
                                        "Note with given title already exists",
                                        Toast.LENGTH_SHORT).show();
                            } else {
                                editName = noteTitle + ".txt";
                                Intent data = new Intent();
                                data.putExtra("NOTE_TITLE", editName);
                                setResult(RESULT_OK, data);
                                saveNote();
                                EditNoteActivity.this.finish();
                            }
                        }
                    })
                    .setNegativeButton("Cancel", null)
                    .create()
                    .show();
        } else {
            saveNote();
            Toast.makeText(EditNoteActivity.this,
                    "Saved changes",
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_note);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        noteText = findViewById(R.id.editTextTextMultiLine);
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            editName = extras.getString(EDIT_NAME_KEY);
        }
        if (editName != null) {
            String fileName = FileUtil.fromNoteName(editName);
            getSupportActionBar().setTitle(editName.substring(0, editName.lastIndexOf(".")));
            try {
                byte[] byts = EncryptedFileManager.getInstance().readFile(new File(getFilesDir(), fileName));
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
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_save) {
            onClickSaveNote();
            return (true);
        }
        return(super.onOptionsItemSelected(item));
    }
}