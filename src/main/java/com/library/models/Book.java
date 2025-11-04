package com.library.models;

public class Book {
    private int id;
    private String title;
    private String author;
    private String genre;
    private String subgenre;
    private String description;
    private String fileDir;

    public Book(String title, String author, String genre, String subgenre, String description, String fileDir) {
        this.title = title;
        this.author = author;
        this.genre = genre;
        this.subgenre = subgenre;
        this.description = description;
        this.fileDir = fileDir;
    }

    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public String getGenre() { return genre; }
    public String getSubgenre() { return subgenre; }
    public String getDescription() { return description; }
    public String getFileDir() { return fileDir; }
}
