package com.example.xtremednotes;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.widget.ImageView;

import java.io.File;

public class ConfigUtil {

    public static void setAvatar(Context context, ImageView imageView){
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        String avatarPath = sharedPref.getString("imagePath", null);
        imageView.setImageResource(R.mipmap.ic_default_avatar_foreground);
        if(avatarPath != null && new File(context.getFilesDir(),avatarPath).exists()){
            imageView.setImageURI(Uri.fromFile(new File(context.getFilesDir(),avatarPath)));
        }
    }
}
