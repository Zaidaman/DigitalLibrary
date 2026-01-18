package com.library.models;

/**
 * Classe che rappresenta un libro nel sistema.
 * Utilizza il Builder Pattern per una costruzione flessibile.
 */
public class Book {
    // Rimossa variabile non usata IdBook
    private final String Title;
    private final String Author;
    private final String Genre;
    private final String filePath;
    private final int IdLibrary;

    // Costruttore pubblico mantenuto per retrocompatibilità
    public Book(String title, String author, String genre, String filePath, int idLibrary) {
        this.Title = title;
        this.Author = author;
        this.Genre = genre;
        this.filePath = filePath;
        this.IdLibrary = idLibrary;
    }

    // Costruttore privato usato dal Builder
    private Book(Builder builder) {
        this.Title = builder.title;
        this.Author = builder.author;
        this.Genre = builder.genre;
        this.filePath = builder.filePath;
        this.IdLibrary = builder.idLibrary;
    }

    public String getTitle() { return Title; }
    public String getAuthor() { return Author; }
    public String getGenre() { return Genre; }
    public String getFilePath() { return filePath; }
    public int getIdLibrary() { return IdLibrary; }

    /**
     * Builder class per costruire oggetti Book in modo flessibile.
     * Design Pattern: Builder
     * 
     * Esempio d'uso:
     * Book book = new Book.Builder()
     *     .title("Il Signore degli Anelli")
     *     .author("J.R.R. Tolkien")
     *     .genre("Fantasy")
     *     .filePath("/path/to/book.pdf")
     *     .idLibrary(1)
     *     .build();
     */
    public static class Builder {
        private String title;
        private String author;
        private String genre;
        private String filePath;
        private int idLibrary;

        public Builder title(String title) {
            this.title = title;
            return this;
        }

        public Builder author(String author) {
            this.author = author;
            return this;
        }

        public Builder genre(String genre) {
            this.genre = genre;
            return this;
        }

        public Builder filePath(String filePath) {
            this.filePath = filePath;
            return this;
        }

        public Builder idLibrary(int idLibrary) {
            this.idLibrary = idLibrary;
            return this;
        }

        public Book build() {
            return new Book(this);
        }
    }
}
