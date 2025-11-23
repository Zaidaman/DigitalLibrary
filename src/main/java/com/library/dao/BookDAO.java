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
        public void insert(Book book, int idAuthor, int annoPub) {
            String sql = "INSERT INTO Book (Title, IdAuthor, AnnoPub, BookFile) VALUES (?, ?, ?, ?)";
            try (Connection conn = DbUtils.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, book.getTitle());
                ps.setInt(2, idAuthor);
                ps.setInt(3, annoPub);
                ps.setBytes(4, book.getFile());
                ps.executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    public List<Book> findAll() {
        List<Book> books = new ArrayList<>();
        String sql = "SELECT IdBook, Title, IdAuthor, AnnoPub, BookFile FROM Book";
        try (Connection conn = DbUtils.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Book book = new Book(
                    rs.getString("Title"),
                    null, // Author name, to be joined if needed
                    null, // Genre, to be joined if needed
                    rs.getBytes("BookFile"),
                    -1 // IdLibrary, not present in Book table
                );
                books.add(book);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return books;
    }

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
                null,     // author name not loaded here
                null,     // genre not loaded
                rs.getBytes("BookFile"),
                idLibrary // now book knows from which library came
            );
            books.add(book);
        }

    } catch (SQLException e) {
        throw new RuntimeException(e);
    }

    return books;
}

}
