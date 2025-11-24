package com.library.models;

public class LibAccess {
    private int IdUser;
    private int IdLibrary;

    public LibAccess(int IdUser, int IdLibrary) {
        this.IdUser = IdUser;
        this.IdLibrary = IdLibrary;
    }

    public int getIdUser() { return IdUser; }
    public int getIdLibrary() { return IdLibrary; }
}
