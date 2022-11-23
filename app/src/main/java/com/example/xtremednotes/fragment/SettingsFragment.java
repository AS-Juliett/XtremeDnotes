package com.example.xtremednotes.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.xtremednotes.util.ConfigUtil;
import com.example.xtremednotes.EncryptedFileManager;
import com.example.xtremednotes.util.FileUtil;
import com.example.xtremednotes.R;
import com.example.xtremednotes.util.VerifyUtil;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsFragment extends Fragment {

    private CircleImageView imageView;
    private View view;
    private boolean needsKey = false;
    int SELECT_PICTURE = 200;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_settings, container, false);
        imageView = view.findViewById(R.id.imageAvatar);
        ConfigUtil.setAvatar(view.getContext(), imageView);
        imageView.setOnClickListener(v -> imageChooser());

        needsKey = !EncryptedFileManager.getInstance().tryInitKey(view.getContext());
        Button resetButton = view.findViewById(R.id.resetPassword);
        resetButton.setOnClickListener(v -> onClickReset());

        return view;
    }

    private void imageChooser() {
        Intent i = new Intent();
        i.setType("image/*");
        i.setAction(Intent.ACTION_GET_CONTENT);

        startActivityForResult(Intent.createChooser(i, "Select Picture"), SELECT_PICTURE);
    }

    public void saveFile(Uri sourceUri, Uri destinationUri) throws IOException {
        InputStream inputStream = view.getContext().getContentResolver().openInputStream(sourceUri);
        OutputStream outputStream = view.getContext().getContentResolver().openOutputStream(destinationUri);
        FileUtil.transfer(inputStream, outputStream);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == SELECT_PICTURE) {
                Uri selectedImageUri = data.getData();
                if (null != selectedImageUri) {
                    Uri destUri = Uri.fromFile(new File(view.getContext().getFilesDir().toString(),"avatar.png"));
                    try {
                        saveFile(selectedImageUri, destUri);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(view.getContext());
                    SharedPreferences.Editor edit = sharedPref.edit();
                    edit.putString("imagePath", "avatar.png");
                    edit.commit();

                    ConfigUtil.setAvatar(view.getContext(), imageView);
                }
            }
        }
    }

    private void onClickReset(){
        EditText et = new EditText(view.getContext());
        et.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
        new AlertDialog.Builder(view.getContext())
                .setTitle("Type new password")
                .setView(et)
                .setPositiveButton("OK", (dialogInterface, i) -> {
                    String newPassword = et.getText().toString();
                    String res = VerifyUtil.verifyPassword(newPassword);
                    if (res != null) {
                        Toast.makeText(getContext(), res, Toast.LENGTH_SHORT).show();
                        return;
                    }
                    EncryptedFileManager.getInstance().updateDefaultKey(view.getContext(), newPassword);
                    Toast.makeText(view.getContext(),
                                "Updated password",
                                Toast.LENGTH_SHORT).show();
                    }
                )
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }
}
