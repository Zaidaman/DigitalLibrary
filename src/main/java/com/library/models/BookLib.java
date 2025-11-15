package com.library.models;

public class BookLib {
	private int idBook;
	private int idLibrary;

	public BookLib(int idBook, int idLibrary) {
		this.idBook = idBook;
		this.idLibrary = idLibrary;
	}

	public int getIdBook() { return idBook; }

	public int getIdLibrary() { return idLibrary; }
}
