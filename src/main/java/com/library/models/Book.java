package com.library.models;

public class Book {
    // Rimossa variabile non usata IdBook
    private final String Title;
    private final String Author;
    private final String Genre;
    private final String filePath;
    private final int IdLibrary;


    public Book(String title, String author, String genre, String filePath, int idLibrary) {
        this.Title = title;
        this.Author = author;
        this.Genre = genre;
        this.filePath = filePath;
        this.IdLibrary = idLibrary;
    }

    public String getTitle() { return Title; }
    public String getAuthor() { return Author; }
    public String getGenre() { return Genre; }
    public String getFilePath() { return filePath; }
    public int getIdLibrary() { return IdLibrary; }
}
