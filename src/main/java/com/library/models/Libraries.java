package com.library.models;

public class Libraries {
    private int IdLibrary;
    private String LibName;

    public Libraries(int IdLibrary, String LibName) {
        this.IdLibrary = IdLibrary;
        this.LibName = LibName;
    }

    public int getIdLibrary() { return IdLibrary; }
    public String getLibName() { return LibName; }
}
