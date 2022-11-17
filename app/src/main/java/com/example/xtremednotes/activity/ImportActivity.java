package com.example.xtremednotes.activity;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.example.xtremednotes.EncryptedFileManager;
import com.example.xtremednotes.R;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class ImportActivity extends AppCompatActivity {
    private SecretKey key;
    public static String FILENAME_KEY = "filename";
    public static final String PASSWORD_FILENAME = ".password";
    public static final String EXT = ".txt";
    private File passwordFile;
    private String filename;

    private File tempDir;

    public void onClickImport(View v) {
        try {
            EncryptedFileManager.getInstance().importArchive(this, filename);
            finish();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean verifyPassword(String pw) {
        EncryptedFileManager efm = EncryptedFileManager.getInstance();
        byte[] key = efm.hashKey(pw);
        SecretKey secretKey = new SecretKeySpec(key, "AES");
        try {
            EncryptedFileManager.getInstance().readArchive(filename, secretKey);
        } catch (EncryptedFileManager.InvalidArchiveException e) {
            return false;
        }
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        tempDir = getDir("temp", Context.MODE_PRIVATE);
        setContentView(R.layout.activity_import);

        Bundle extras = getIntent().getExtras();
        filename = extras.getString(FILENAME_KEY);

        PasswordActivity.setNextMessage("Enter archive password:");
        PasswordActivity.setVerifier(this::verifyPassword);
        startActivityForResult(new Intent(this, PasswordActivity.class), 124);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 124 && resultCode == RESULT_OK) {
            byte[] hash = data.getByteArrayExtra("password");
            SecretKey sk = new SecretKeySpec(hash, "AES");
            try {
                ArrayList<String> files = EncryptedFileManager.getInstance().readArchive(filename, sk);
                ListView lv = findViewById(R.id.listView);
                lv.setAdapter(new ArrayAdapter(this, android.R.layout.simple_list_item_1, android.R.id.text1, files));
            } catch (EncryptedFileManager.InvalidArchiveException e) {
                e.printStackTrace();
            }
        }
    }


}