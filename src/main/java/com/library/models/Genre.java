package com.library.models;

public class Genre {
    private final int IdGenre;
    private final String GenreName;

    public Genre(int IdGenre, String GenreName) {
        this.IdGenre = IdGenre;
        this.GenreName = GenreName;
    }

    public int getIdGenre() { return IdGenre; }
    public String getGenreName() { return GenreName; }
}
