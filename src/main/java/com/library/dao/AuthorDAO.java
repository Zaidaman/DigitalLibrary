package com.library.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.library.models.Author;
import com.library.utils.DbUtils;

public class AuthorDAO {
        public void insert(com.library.models.Author author) {
            String sql = "INSERT INTO Author (AuthorName, MidName, Surname) VALUES (?, ?, ?)";
            try (java.sql.Connection conn = com.library.utils.DbUtils.getConnection();
                 java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, author.getAuthorName());
                ps.setString(2, author.getMidName());
                ps.setString(3, author.getSurname());
                ps.executeUpdate();
            } catch (java.sql.SQLException e) {
                throw new RuntimeException(e);
            }
        }
    public List<Author> findAll() {
        List<Author> authors = new ArrayList<>();
        String sql = "SELECT IdAuthor, AuthorName, MidName, Surname FROM Author";
        try (Connection conn = DbUtils.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Author author = new Author(
                    rs.getInt("IdAuthor"),
                    rs.getString("AuthorName"),
                    rs.getString("MidName"),
                    rs.getString("Surname")
                );
                authors.add(author);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return authors;
    }

    public Author findById(int idAuthor) {
        String sql = "SELECT IdAuthor, AuthorName, MidName, Surname FROM Author WHERE IdAuthor = ?";
        try (Connection conn = DbUtils.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idAuthor);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Author(
                        rs.getInt("IdAuthor"),
                        rs.getString("AuthorName"),
                        rs.getString("MidName"),
                        rs.getString("Surname")
                    );
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }
}
