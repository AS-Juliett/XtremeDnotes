package com.example.xtremednotes.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.xtremednotes.EncryptedFileManager;
import com.example.xtremednotes.R;

import androidx.appcompat.app.AppCompatActivity;

import java.util.function.Function;

public class PasswordActivity extends AppCompatActivity {
    private static Function<String, Boolean> verifier;
    private final String defaultMessage = "Password:";
    private static String message = "";

    public static void setVerifier(Function<String, Boolean> verifier) {
        PasswordActivity.verifier = verifier;
    }

    public static void setNextMessage(String message) {
        PasswordActivity.message = message;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password);
        EditText et = findViewById(R.id.passwordEntry);
        ((TextView)findViewById(R.id.passwordMessageText)).setText(message);
        message = defaultMessage;
        et.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if (i == EditorInfo.IME_ACTION_DONE) {
                    Intent data = new Intent();
                    String password = et.getText().toString();
                    if (verifier.apply(password)) {
                        data.putExtra("password", EncryptedFileManager.getInstance().hashKey(password));
                        setResult(RESULT_OK, data);
                        finish();
                    } else {
                        et.setText("");
                        Toast.makeText(PasswordActivity.this, "Wrong password", Toast.LENGTH_SHORT).show();
                    }
                }
                return false;
            }
        });
    }
}
