package com.library.dao;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.library.models.BookGenre;
import com.library.utils.DbUtils;

public class BookGenreDAO {
        public void insert(com.library.models.BookGenre bookGenre) {
            String sql = "INSERT INTO BookGenre (IdBook, IdGenre) VALUES (?, ?)";
            try (java.sql.Connection conn = com.library.utils.DbUtils.getConnection();
                 java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, bookGenre.getIdBook());
                ps.setInt(2, bookGenre.getIdGenre());
                ps.executeUpdate();
            } catch (java.sql.SQLException e) {
                throw new RuntimeException(e);
            }
        }
    public List<BookGenre> findAll() {
        List<BookGenre> list = new ArrayList<>();
        String sql = "SELECT IdBook, IdGenre FROM BookGenre";
        try (Connection conn = DbUtils.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                BookGenre bg = new BookGenre(
                    rs.getInt("IdBook"),
                    rs.getInt("IdGenre")
                );
                list.add(bg);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return list;
    }
    
    public List<Integer> findGenresByBookId(int idBook) {
        List<Integer> genreIds = new ArrayList<>();
        String sql = "SELECT IdGenre FROM BookGenre WHERE IdBook = ?";
        try (Connection conn = DbUtils.getConnection();
             java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idBook);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                genreIds.add(rs.getInt("IdGenre"));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return genreIds;
    }
    
    public void updateGenreForBook(int idBook, int newIdGenre) {
        // Elimina le vecchie associazioni
        String deleteSql = "DELETE FROM BookGenre WHERE IdBook = ?";
        try (Connection conn = DbUtils.getConnection();
             java.sql.PreparedStatement ps = conn.prepareStatement(deleteSql)) {
            ps.setInt(1, idBook);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        
        // Inserisci la nuova associazione
        String insertSql = "INSERT INTO BookGenre (IdBook, IdGenre) VALUES (?, ?)";
        try (Connection conn = DbUtils.getConnection();
             java.sql.PreparedStatement ps = conn.prepareStatement(insertSql)) {
            ps.setInt(1, idBook);
            ps.setInt(2, newIdGenre);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void deleteByBookId(int idBook) {
        String sql = "DELETE FROM BookGenre WHERE IdBook = ?";
        try (Connection conn = DbUtils.getConnection();
             java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idBook);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
