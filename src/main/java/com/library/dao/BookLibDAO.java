package com.library.dao;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.library.models.BookLib;
import com.library.utils.DbUtils;

public class BookLibDAO {
        public void insert(com.library.models.BookLib bookLib) {
            String sql = "INSERT INTO BookLib (IdBook, IdLibrary) VALUES (?, ?)";
            try (java.sql.Connection conn = com.library.utils.DbUtils.getConnection();
                 java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, bookLib.getIdBook());
                ps.setInt(2, bookLib.getIdLibrary());
                ps.executeUpdate();
            } catch (java.sql.SQLException e) {
                throw new RuntimeException(e);
            }
        }
    public List<BookLib> findAll() {
        List<BookLib> list = new ArrayList<>();
        String sql = "SELECT IdBook, IdLibrary FROM BookLib";
        try (Connection conn = DbUtils.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                BookLib bl = new BookLib(
                    rs.getInt("IdBook"),
                    rs.getInt("IdLibrary")
                );
                list.add(bl);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return list;
    }

    public void deleteByLibraryId(int idLibrary) {
        String sql = "DELETE FROM BookLib WHERE IdLibrary = ?";
        try (Connection conn = DbUtils.getConnection();
             java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idLibrary);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void deleteByBookAndLibrary(int idBook, int idLibrary) {
        String sql = "DELETE FROM BookLib WHERE IdBook = ? AND IdLibrary = ?";
        try (Connection conn = DbUtils.getConnection();
             java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idBook);
            ps.setInt(2, idLibrary);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
