package com.library.models;

public class Book {
    private int IdBook;
    private String Title;
    private String Author;
    private String Genre;
    private byte[] File;
    private int IdLibrary;


    public Book(String title, String author, String genre, byte[] File, int idLibrary) {
        this.Title = title;
        this.Author = author;
        this.Genre = genre;
        this.File = File;
        this.IdLibrary = idLibrary;
    }

    public String getTitle() { return Title; }
    public String getAuthor() { return Author; }
    public String getGenre() { return Genre; }
    public byte[] getFile() { return File; }
    public int getIdLibrary() { return IdLibrary; }
}
