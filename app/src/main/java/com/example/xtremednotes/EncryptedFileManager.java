package com.example.xtremednotes;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.example.xtremednotes.util.FileUtil;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class EncryptedFileManager {
    private static EncryptedFileManager instance;
    private byte[] rawKey;
    private SecretKey defaultKey;
    private IvParameterSpec iv;
    public static String ARCHIVE_EXT = ".zip";
    private static final String PASSWORD_FILENAME = ".password";
    public static EncryptedFileManager getInstance() {
        if (instance == null) {
            instance = new EncryptedFileManager();
        }
        return instance;
    }

    private EncryptedFileManager() {

    }

    private void setKey(byte[] key) {
        this.rawKey = key;
        defaultKey = new SecretKeySpec(key, "AES");
        if (iv == null) {
            byte[] ivBytes = new byte[16];
            iv = new IvParameterSpec(ivBytes);
        }
    }

    public byte[] hashKey(String key) {
        try {
            byte[] tmp = key.getBytes(StandardCharsets.UTF_8);
            return MessageDigest.getInstance("SHA-256").digest(tmp);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void traverse(File dir, Consumer<File> ff) {
        for (File f : dir.listFiles()) {
            if (f.isDirectory()) {
                traverse(f, ff);
            } else if (!f.getName().endsWith(".txt")) {
                continue;
            } else {
                ff.accept(f);
            }
        }
    }

    private void reencrypt(File f, SecretKey sk) {
        try {
            CipherInputStream cis = new CipherInputStream(new FileInputStream(f), this.getCipher(Cipher.DECRYPT_MODE));
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            FileUtil.transfer(cis, bos);
            cis.close();
            CipherOutputStream cos = new CipherOutputStream(new FileOutputStream(f), this.getCipher(sk, Cipher.ENCRYPT_MODE));
            cos.write(bos.toByteArray());
            cos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void updateDefaultKey(Context ctx, String key) {
        byte[] hash = this.hashKey(key);
        SecretKey sk = new SecretKeySpec(hash, "AES");
        traverse(ctx.getFilesDir(), (File f) -> reencrypt(f, sk));
        this.setKey(hash);
        outputMgr.write(hash);
    }

    public static final String STORE_VERSION_KEY = "store_version";
    public static final String FILE_VERSION_CURRENT = "1";
    public static final String STORE_VERSION_CURRENT = "2";

    private IPasswordManager getPasswordManager(Context ctx, String version) {
        switch (version) {
            case "1":
                return new PasswordManagerV1(ctx);
            case "2":
                return new PasswordManagerV2(ctx);
            default:
                return null;
        }
    }

    private IPasswordManager inputMgr;
    private IPasswordManager outputMgr;

    private class AppVersion {
        String passwordVersion;
        String fileVersion;
        public AppVersion(String pv, String fv) {
            this.passwordVersion = pv;
            this.fileVersion = fv;
        }
    }

    private AppVersion getVersions(Context ctx) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(ctx);
        String appVersion = sharedPref.getString(STORE_VERSION_KEY, "1.0");
        String[] tokens = appVersion.split("\\.");
        if (tokens.length == 1) {
            return new AppVersion(tokens[0], "0");
        }
        return new AppVersion(tokens[0], tokens.length < 2 ? "0" : tokens[1]);
    }

    public boolean tryInitKey(Context ctx) {
        if (this.defaultKey != null) {
            return true;
        }

        AppVersion av = this.getVersions(ctx);

        inputMgr = getPasswordManager(ctx, av.passwordVersion);
        if (av.passwordVersion.equals(STORE_VERSION_CURRENT)) {
            outputMgr = inputMgr;
        } else {
            outputMgr = getPasswordManager(ctx, STORE_VERSION_CURRENT);
        }

        return inputMgr.read();
    }

    public void clearKey() {
        this.rawKey = null;
        this.defaultKey = null;
    }

    public boolean verify(String password) {
        byte[] res = inputMgr.verify(password);
        if (res == null) {
            return false;
        }
        setKey(res);
        if (inputMgr != outputMgr) {
            outputMgr.write(res);
            inputMgr = outputMgr;
        }
        return true;
    }

    public void updateVersion(Context ctx) {
        AppVersion av = this.getVersions(ctx);
        if (av.fileVersion.equals("0")) {
            traverse(ctx.getFilesDir(), (File f) -> {
                f.renameTo(new File(f.getParent(), FileUtil.fromNoteName(f.getName())));
            });
        }

        String newAppVersion = STORE_VERSION_CURRENT + "." + FILE_VERSION_CURRENT;
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(ctx);
        sharedPref.edit().putString(STORE_VERSION_KEY, newAppVersion).commit();
    }

    public File saveFile(Context ctx, String filename, String folder, byte[] content) throws FileNotFoundException {
        File file;
        if(folder == null){
            file = new File(ctx.getFilesDir(), filename);
        } else{
            File parent = new File(ctx.getFilesDir(), folder);
            file = new File(parent, filename);
        }
        CipherOutputStream cos = new CipherOutputStream(new FileOutputStream(file), this.getCipher(Cipher.ENCRYPT_MODE));
        try {
            cos.write(content);
            cos.flush();
            cos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return file;
    }

    public byte[] readFile(File file) throws FileNotFoundException {
        return readFileInternal(file, defaultKey);
    }

    public byte[] readFile(File file, SecretKey key) throws FileNotFoundException {
        return readFileInternal(file, key);
    }

    public byte[] readFileInternal(File file, SecretKey key) throws FileNotFoundException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        FileInputStream stream1 = new FileInputStream(file);
        CipherInputStream stream = new CipherInputStream(stream1, this.getCipher(Cipher.DECRYPT_MODE));
        try {
            FileUtil.transfer(stream, bos);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bos.toByteArray();
    }

    private Cipher getCipher(SecretKey sk, int mode) {
        Cipher cipher;
        try {
            cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            e.printStackTrace();
            return null;
        }
        try {
            cipher.init(mode, sk, iv);
        } catch (InvalidAlgorithmParameterException | InvalidKeyException e) {
            e.printStackTrace();
        }
        return cipher;
    }

    private Cipher getCipher(int mode) {
        return this.getCipher(defaultKey, mode);
    }

    public class InvalidArchiveException extends Exception {}

    public void importArchive(Context ctx, String filePath) throws IOException {
        File fo = new File(filePath);
        CipherInputStream cis = new CipherInputStream(new FileInputStream(fo), this.getCipher(Cipher.DECRYPT_MODE));
        ZipInputStream zis = new ZipInputStream(cis);
        ZipEntry ze;
        while ((ze = zis.getNextEntry()) != null) {
            FileOutputStream f;
            try {
                f = new FileOutputStream(new File(ctx.getFilesDir(), ze.getName()));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                continue;
            }
            CipherOutputStream cos = new CipherOutputStream(f, this.getCipher(Cipher.ENCRYPT_MODE));
            FileUtil.transfer(zis, cos);
            cos.flush();
            cos.close();
        }
        zis.close();
        cis.close();
    }

    public ArrayList<String> readArchive(String filePath, SecretKey sk) throws InvalidArchiveException {
        File fo = new File(filePath);
        CipherInputStream cis = null;
        try {
            cis = new CipherInputStream(new FileInputStream(fo), this.getCipher(sk, Cipher.DECRYPT_MODE));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        ZipInputStream zis = new ZipInputStream(cis);
        ZipEntry ze;
        ArrayList<String> al = new ArrayList<>();
        while (true) {
            try {
                ze = zis.getNextEntry();
            } catch (IOException e) {
                e.printStackTrace();
                throw new InvalidArchiveException();
            }
            if (ze == null) break;
            al.add(FileUtil.toNoteName(ze.getName()));
        }
        try {
            zis.close();
            cis.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (al.size() == 0) {
            throw new InvalidArchiveException();
        }
        return al;
    }

    public void export(Context ctx, String filePath) {
        File fo = new File(filePath);
        try {
            CipherOutputStream cos = new CipherOutputStream(new FileOutputStream(fo), this.getCipher(Cipher.ENCRYPT_MODE));
            ZipOutputStream zos = new ZipOutputStream(cos);
            traverse(ctx.getFilesDir(), (File f) -> {
                try {
                    zos.putNextEntry(new ZipEntry(f.getName()));
                    FileInputStream fis = new FileInputStream(f);
                    CipherInputStream cis = new CipherInputStream(fis, this.getCipher(Cipher.DECRYPT_MODE));
                    FileUtil.transfer(cis, zos);
                    cis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            zos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
