package com.library.models;

public class Library {
    private int IdLibrary;
    private String Name;

    public Library(int IdLibrary, String Name) {
        this.IdLibrary = IdLibrary;
        this.Name = Name;
    }

    public int getIdLibrary() { return IdLibrary; }
    public String getName() { return Name; }
}
