package com.library.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.library.models.Book;
import com.library.utils.DbUtils;

public class BookDAO {

    // Inserisce un nuovo libro nel DB con percorso file
    public void insert(Book book, int idAuthor, int annoPub) {
        String sql = "INSERT INTO Book (Title, IdAuthor, AnnoPub, BookFile) VALUES (?, ?, ?, ?)";
        try (Connection conn = DbUtils.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, book.getTitle());
            ps.setInt(2, idAuthor);
            ps.setInt(3, annoPub);
            ps.setString(4, book.getFilePath()); // qui usiamo il path come VARCHAR
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // Restituisce tutti i libri
    public List<Book> findAll() {
        List<Book> books = new ArrayList<>();
        String sql = "SELECT IdBook, Title, IdAuthor, AnnoPub, BookFile FROM Book";
        try (Connection conn = DbUtils.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Book book = new Book(
                    rs.getString("Title"),
                    null, // author
                    null, // genre
                    rs.getString("BookFile"), // path del file
                    -1    // idLibrary non presente
                );
                books.add(book);
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return books;
    }

    // Restituisce i libri di una libreria specifica
    public List<Book> findByLibraryId(int idLibrary) {
        List<Book> books = new ArrayList<>();
        String sql =
            "SELECT b.IdBook, b.Title, b.IdAuthor, b.AnnoPub, b.BookFile " +
            "FROM Book b " +
            "JOIN BookLib bl ON b.IdBook = bl.IdBook " +
            "WHERE bl.IdLibrary = ?";

        try (Connection conn = DbUtils.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idLibrary);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Book book = new Book(
                    rs.getString("Title"),
                    null,
                    null,
                    rs.getString("BookFile"), // path del PDF
                    idLibrary
                );
                books.add(book);
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return books;
    }

    // Trova un libro tramite il titolo
    public Book findByTitle(String title) {
        String sql = "SELECT IdBook, Title, IdAuthor, AnnoPub, BookFile FROM Book WHERE Title = ?";
        try (Connection conn = DbUtils.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, title);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new Book(
                    rs.getString("Title"),
                    null,
                    null,
                    rs.getString("BookFile"), // path del PDF
                    -1
                );
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }
}
