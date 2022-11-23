package com.example.xtremednotes;

import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;

public class Config {
    private static Config instance;
    private static final String CONFIG_FILENAME = "badapp.conf";
    private Properties props;
    public static final String AVATAR_URL = "avatar.url";
    private File configFile;
    public static Config getInstance() {
        if (instance == null) {
            instance = new Config();
        }
        return instance;
    }

    private Config() {
        configFile = new File(Environment.getExternalStorageDirectory(), CONFIG_FILENAME);
        props = new Properties();
        if (configFile.exists()) {
            this.load();
        } else {
            this.initDefault();
            this.save();
        }
    }

    public void load() {
        try {
            FileInputStream stream = new FileInputStream(configFile);
            props.load(stream);
            stream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String get(String name) {
        return props.getProperty(name);
    }

    public void initDefault() {
        props.setProperty(AVATAR_URL, "//file:///sdcard/puffin.png\n");
    }

    public void save() {
        try {
            props.store(new FileWriter(configFile), "");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
