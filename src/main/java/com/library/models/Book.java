package com.library.models;

public class Book {
    private int IdBook;
    private String Title;
    private String Author;
    private String Genre;
    private String Subgenre;
    private String Description;
    private String FileDir;
    private int IdLibrary;

    public Book(String title, String author, String genre, String subgenre, String description, String fileDir, int idLibrary) {
        this.Title = title;
        this.Author = author;
        this.Genre = genre;
        this.Subgenre = subgenre;
        this.Description = description;
        this.FileDir = fileDir;
        this.IdLibrary = idLibrary;
    }

    public String getTitle() { return Title; }
    public String getAuthor() { return Author; }
    public String getGenre() { return Genre; }
    public String getSubgenre() { return Subgenre; }
    public String getDescription() { return Description; }
    public String getFileDir() { return FileDir; }
    public int getIdLibrary() { return IdLibrary; }
}
