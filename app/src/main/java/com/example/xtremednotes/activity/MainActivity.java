package com.example.xtremednotes.activity;

import androidx.appcompat.app.AppCompatActivity;
import de.hdodenhof.circleimageview.CircleImageView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.xtremednotes.Config;
import com.example.xtremednotes.util.ConfigUtil;
import com.example.xtremednotes.EncryptedFileManager;
import com.example.xtremednotes.R;

public class MainActivity extends AppCompatActivity {

    private CircleImageView imageView = null;
    private boolean needsKey = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        needsKey = !EncryptedFileManager.getInstance().tryInitKey(this);

        EditText mainPasswordInput = findViewById(R.id.mainPasswordInput);
        mainPasswordInput.setHint(R.string.password_hint);
        ImageButton buttonVerifyPassword = findViewById(R.id.imageButton);

        imageView = findViewById(R.id.imageview);
        ConfigUtil.setAvatar(this, imageView);

        buttonVerifyPassword.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String password = mainPasswordInput.getText().toString();
                if (password.isEmpty() || !needsKey && !EncryptedFileManager.getInstance().verify(password)) {
                    Toast.makeText(MainActivity.this, "Invalid password", Toast.LENGTH_SHORT).show();
                    mainPasswordInput.setText("");
                    return;
                } else if (needsKey) {
                    EncryptedFileManager.getInstance().updateDefaultKey(MainActivity.this, password);
                    Toast.makeText(MainActivity.this, "New password set", Toast.LENGTH_SHORT).show();
                    MainActivity.this.needsKey = false;
                }
                Intent intent = new Intent(v.getContext(), NotesNavigationActivity.class);
                startActivity(intent);
                mainPasswordInput.setText("");
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        Config.getInstance().load();
        ConfigUtil.setAvatar(this, imageView);
    }
}