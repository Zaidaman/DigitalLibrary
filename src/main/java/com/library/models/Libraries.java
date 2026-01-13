package com.library.models;

public class Libraries {
    private final int IdLibrary;
    private final String LibName;

    public Libraries(int IdLibrary, String LibName) {
        this.IdLibrary = IdLibrary;
        this.LibName = LibName;
    }

    public int getIdLibrary() { return IdLibrary; }
    public String getLibName() { return LibName; }
}
