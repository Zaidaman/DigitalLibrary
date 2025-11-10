package com.library.models;

public class Author {
    private int IdAuthor;
    private String AuthorName;
    private String MidName;
    private String Surname;

    public Author(int IdAuthor, String AuthorName, String MidName, String Surname) {
        this.IdAuthor = IdAuthor;
        this.AuthorName = AuthorName;
        this.MidName = MidName;
        this.Surname = Surname;
    }

    public int getIdAuthor() { return IdAuthor; }
    public String getAuthorName() { return AuthorName; }
    public String getMidName() { return MidName; }
    public String getSurname() { return Surname; }
}
