package com.library.models;

public class Libraries {
    private int IdLibrary;
    private String LibName;
    private int IdUser;

    public Libraries(int IdLibrary, String LibName, int IdUser) {
        this.IdLibrary = IdLibrary;
        this.LibName = LibName;
        this.IdUser = IdUser;
    }

    public int getIdLibrary() { return IdLibrary; }
    public String getLibName() { return LibName; }
    public int getIdUser() { return IdUser; }
}
