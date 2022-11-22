package com.example.xtremednotes.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;

import com.example.xtremednotes.R;
import com.example.xtremednotes.model.Folder;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class ConfigUtil {

    public static void setAvatar(Context context, ImageView imageView){
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        String avatarPath = sharedPref.getString("imagePath", null);
        imageView.setImageResource(R.mipmap.ic_default_avatar_foreground);
        if(avatarPath != null && new File(context.getFilesDir(),avatarPath).exists()){
            imageView.setImageURI(Uri.fromFile(new File(context.getFilesDir(),avatarPath)));
        }
    }

    public static Object[] getFolders(Context context){
        Supplier<Stream<String>> dirSupplier = () ->
                Arrays.stream(Objects.requireNonNull(context.getFilesDir().listFiles(File::isDirectory))).map(File::getName);
        return dirSupplier.get().toArray();
    }

    public static void setFolders(Context context, Menu subMenu, String defaultFolder) {
        Object[] foldersList = getFolders(context);
        subMenu.clear();
        MenuItem defItem = subMenu.add(defaultFolder);
        defItem.setIcon(R.mipmap.ic_note_foreground);

        for (Object dir : foldersList) {
            MenuItem mi = subMenu.add(decodeBase64(dir.toString()));
            mi.setIcon(R.mipmap.ic_note_foreground);
        }
    }

    public static ArrayList<Folder> convertObjectToList(Object[] obj) {
        ArrayList<Folder> folderList = new ArrayList<>();
        for (Object dir: obj) {
            Folder fld = (new Folder((String) dir));
            folderList.add(fld);
        }
        return folderList;
    }

    public static String[] convertObject(Object[] obj){
        ArrayList<String> folderList = new ArrayList<>();
        for (Object dir: obj) {
            folderList.add(decodeBase64((String) dir));
        }
        return folderList.toArray(new String[0]);
    }

    public static String[] convertObjectBase64(Object[] obj){
        ArrayList<String> folderList = new ArrayList<>();
        for (Object dir: obj) {
            folderList.add((String) dir);
        }
        return folderList.toArray(new String[0]);
    }

    public static String encodeBase64(String string){
        return Base64.getEncoder().encodeToString(string.getBytes()).replace("/", "_");
    }

    public static String decodeBase64(String string){
        byte[] decodedBytes = Base64.getDecoder().decode(string.replace("_", "/"));
        return new String(decodedBytes);
    }
}
