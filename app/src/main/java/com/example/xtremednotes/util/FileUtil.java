package com.example.xtremednotes.util;

import android.net.Uri;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class FileUtil {

    private static byte[] transferBuf = new byte[1024];

    public static String getRealPath(Uri uri) {
        try {
            String s = URLDecoder.decode(uri.toString(), StandardCharsets.UTF_8.name());
            final String u = "content://com.android.externalstorage.documents/tree/";
            String filePath = null;
            if (s.equals("content://com.android.providers.downloads.documents/tree/downloads")) {
                filePath = "/storage/emulated/0/Download";
            } else if (s.contains("raw:")) {
                filePath = s.split(":")[2];
            } else if (s.startsWith(u)) {
                int i = u.length()+1;
                filePath = "/storage/" + s.substring(i, s.indexOf(":", i)) + "/" + s.split(":")[2];
            }
            return filePath;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void transfer(InputStream is, OutputStream os) throws IOException {
        int n;
        while ((n = is.read(transferBuf)) != -1) {
            os.write(transferBuf, 0, n);
        }
    }

    public static String toNoteName(String fileName) {
        String[] tokens = fileName.split("\\.(?=[^\\.]+$)");
        if (tokens.length < 2) {
            return fileName;
        }
        return new String(Base64.getDecoder().decode(tokens[0].replace("_", "/")), StandardCharsets.UTF_8) + "." + tokens[1];
    }

    public static String fromNoteName(String noteName) {
        String[] tokens = noteName.split("\\.(?=[^\\.]+$)");
        if (tokens.length < 2) {
            return noteName;
        }
        return Base64.getEncoder().encodeToString(tokens[0].getBytes(StandardCharsets.UTF_8)).replace("/", "_") + "." + tokens[1];
    }

}
