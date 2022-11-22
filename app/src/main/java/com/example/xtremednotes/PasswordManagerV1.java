package com.example.xtremednotes;

import android.content.Context;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public class PasswordManagerV1 implements IPasswordManager{
    private Context ctx;
    private byte[] hash;
    private static final String PASSWORD_FILENAME = ".password";
    public PasswordManagerV1(Context ctx) {
        this.ctx = ctx;
    }
    @Override
    public boolean read() {
        File pw = new File(ctx.getFilesDir(), PASSWORD_FILENAME);
        try {
            FileInputStream fos = new FileInputStream(pw);
            hash = new byte[32];
            fos.read(hash);
            fos.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public void write(byte[] password) {
        hash = password;
        File pw = new File(ctx.getFilesDir(), PASSWORD_FILENAME);
        try {
            FileOutputStream fos = new FileOutputStream(pw);
            fos.write(password);
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public byte[] verify(String key) {
        try {
            byte[] tmp = MessageDigest.getInstance("SHA-256").digest(key.getBytes(StandardCharsets.UTF_8));
            if (Arrays.equals(tmp, hash)) {
                return hash;
            }
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }
}
