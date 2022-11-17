package com.example.xtremednotes.model;

public class Note {

    private String title;
    private int img;

    public Note(String title, int img){
        this.title = title;
        this.img = img;
    }

    public void setTitle(String title){
        this.title = title;
    }

    public void setImg(int img){
        this.img = img;
    }

    public String getTitle(){
        return this.title;
    }

    public int getImg(){
        return this.img;
    }
}

