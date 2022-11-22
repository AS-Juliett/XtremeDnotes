package com.example.xtremednotes;

public interface IPasswordManager {
    boolean read();
    void write(byte[] password);
    byte[] verify(String key);
}
