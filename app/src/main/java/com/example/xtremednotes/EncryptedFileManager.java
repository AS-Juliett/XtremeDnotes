package com.example.xtremednotes;

import android.content.Context;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
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
        byte[] ivBytes = new byte[16];
        iv = new IvParameterSpec(ivBytes);
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

    public void updateDefaultKey(Context ctx, String key) {
        byte[] hash = this.hashKey(key);
        this.setKey(hash);
        File pw = new File(ctx.getFilesDir(), PASSWORD_FILENAME);
        try {
            FileOutputStream fos = new FileOutputStream(pw);
            fos.write(hash);
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean tryInitKey(Context ctx) {
        if (this.defaultKey != null) {
            return true;
        }
        File pw = new File(ctx.getFilesDir(), PASSWORD_FILENAME);
        try {
            FileInputStream fos = new FileInputStream(pw);
            byte[] hash = new byte[32];
            fos.read(hash);
            this.setKey(hash);
            fos.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void clearKey() {
        this.rawKey = null;
        this.defaultKey = null;
    }

    public boolean verify(String password) {
        byte[] tmp = this.hashKey(password);
        return Arrays.equals(tmp, this.rawKey);
    }

    public File saveFile(Context ctx, String filename, byte[] content) throws FileNotFoundException {
        File file = new File(ctx.getFilesDir(), filename);
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

    public void cleanDir(File dir) {
        for (File f : dir.listFiles()) {
            if (f.isDirectory()) {
                cleanDir(f);
            }
            f.delete();
        }
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
        ZipEntry ze = null;
        ArrayList<String> al = new ArrayList<>();
        while (true) {
            try {
                ze = zis.getNextEntry();
            } catch (IOException e) {
                e.printStackTrace();
                throw new InvalidArchiveException();
            }
            if (ze == null) break;
            al.add(ze.getName());
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
            for (File f : ctx.getFilesDir().listFiles()) {
                if (!f.getName().endsWith(".txt")) {
                    continue;
                }
                zos.putNextEntry(new ZipEntry(f.getName()));
                FileInputStream fis = new FileInputStream(f);
                CipherInputStream cis = new CipherInputStream(fis, this.getCipher(Cipher.DECRYPT_MODE));
                FileUtil.transfer(cis, zos);
                cis.close();
            }
            zos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
