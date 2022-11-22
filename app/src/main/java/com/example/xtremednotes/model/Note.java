package com.example.xtremednotes.model;

import com.example.xtremednotes.R;
import com.example.xtremednotes.util.FileUtil;

public class Note {

    private String title;
    private String folder;
    private static int img = R.mipmap.ic_note_foreground;

    public Note(String title, String folder){
        this.title = title;
        this.folder = folder;
    }

    public void setTitle(String title){
        this.title = title;
    }

    public void setImg(int img){
        this.img = img;
    }

    public void setFolder(String folder){
        this.folder = folder;
    }

    public String getTitle(){
        return this.title;
    }

    public String getFolder(){
        return this.folder;
    }

    public String getFullName() {
        String cat = getFolder();
        String noteFileName = FileUtil.fromNoteName(getTitle());
        if (!cat.equals("files")) {
            noteFileName = cat + "/" + noteFileName;
        }
        return noteFileName;
    }

    public int getImg(){
        return this.img;
    }
}

