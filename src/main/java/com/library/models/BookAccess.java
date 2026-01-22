package com.library.models;

public class BookAccess {
    private final int IdBook;
    private final int IdUser;

    public BookAccess(int IdBook, int IdUser) {
        this.IdBook = IdBook;
        this.IdUser = IdUser;
    }

    public int getIdBook() { return IdBook; }
    public int getIdUser() { return IdUser; }
}
    
