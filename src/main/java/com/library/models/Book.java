package com.library.models;

import java.io.File;

public class Book {
    private String title;
    private File file;

    public Book(String title, File file) {
        this.title = title;
        this.file = file;
    }

    public String getTitle() { return title; }
    public File getFile() { return file; }
}
