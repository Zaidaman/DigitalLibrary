package com.library.models;

public class BookGenre {
    private int IdBook;
    private int IdGenre;

    public BookGenre(int IdBook, int IdGenre) {
        this.IdBook = IdBook;
        this.IdGenre = IdGenre;
    }

    public int getIdBook() { return IdBook; }
    public int getIdGenre() { return IdGenre; }
}
