package com.library.models;

import java.util.ArrayList;
import java.util.List;

public class Library {
    private String name;
    private List<Book> books = new ArrayList<>();

    public Library(String name) {
        this.name = name;
    }

    public String getName() { return name; }
    public List<Book> getBooks() { return books; }

    public void addBook(Book book) {
        books.add(book);
    }
}
