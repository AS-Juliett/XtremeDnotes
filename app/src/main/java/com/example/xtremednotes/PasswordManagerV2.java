package com.example.xtremednotes;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class PasswordManagerV2 implements IPasswordManager{
    private Context ctx;
    private static final String PASSWORD_KEY = "password_encrypted";
    private byte[] enc;
    public PasswordManagerV2(Context ctx) {
        this.ctx = ctx;
    }

    private Cipher getCipher(byte[] bytes, int mode) {
        SecretKey sk = new SecretKeySpec(bytes, "AES");
        Cipher cipher;
        try {
            cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            e.printStackTrace();
            return null;
        }
        try {
            cipher.init(mode, sk, new IvParameterSpec(new byte[16]));
        } catch (InvalidAlgorithmParameterException | InvalidKeyException e) {
            e.printStackTrace();
        }
        return cipher;
    }

    @Override
    public boolean read() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(ctx);
        String b64 = sharedPref.getString(PASSWORD_KEY, null);
        if (b64 == null) {
            return false;
        }
        enc = Base64.getDecoder().decode(b64);
        return true;
    }

    @Override
    public void write(byte[] password) {
        Cipher c = getCipher(password, Cipher.ENCRYPT_MODE);
        try {
            byte[] enc2 = c.doFinal(password);
            enc = enc2;
            String b64 = Base64.getEncoder().encodeToString(enc2);
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(ctx);
            sharedPref.edit().putString(PASSWORD_KEY, b64).commit();
        } catch (BadPaddingException | IllegalBlockSizeException e) {
            e.printStackTrace();
        }

    }

    @Override
    public byte[] verify(String key) {
        try {
            byte[] tmp = MessageDigest.getInstance("SHA-256").digest(key.getBytes(StandardCharsets.UTF_8));
            Cipher c = getCipher(tmp, Cipher.DECRYPT_MODE);
            byte[] dec = c.doFinal(enc);
            if (Arrays.equals(tmp, dec)) {
                return dec;
            }
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (BadPaddingException | IllegalBlockSizeException e) {
            e.printStackTrace();
        }
        return null;
    }
}
