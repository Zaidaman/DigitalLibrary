package com.library.models;

import java.io.File;

public class Book {
    private int id;
    private String title;
    private String author;
    private String genre;
    private String subgenre;
    private String description;
    private File data;

    public Book(String title, String author, String genre, String subgenre, String description, File data) {
        this.title = title;
        this.author = author;
        this.genre = genre;
        this.subgenre = subgenre;
        this.description = description;
        this.data = data;
    }

    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public String getGenre() { return genre; }
    public String getSubgenre() { return subgenre; }
    public String getDescription() { return description; }
    public File getData() { return data; }
}
