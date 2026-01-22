package com.library.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.library.models.BookAccess;
import com.library.utils.DbUtils;

public class BookAccessDAO {
    
    public void insert(BookAccess access) {
        String sql = "INSERT INTO BookAccess (IdBook, IdUser) VALUES (?, ?)";
        try (Connection conn = DbUtils.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, access.getIdBook());
            ps.setInt(2, access.getIdUser());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<BookAccess> findAll() {
        List<BookAccess> accesses = new ArrayList<>();
        String sql = "SELECT IdBook, IdUser FROM BookAccess";
        try (Connection conn = DbUtils.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                accesses.add(new BookAccess(
                    rs.getInt("IdBook"),
                    rs.getInt("IdUser")
                ));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return accesses;
    }

    public List<BookAccess> findByUserId(int idUser) {
        List<BookAccess> accesses = new ArrayList<>();
        String sql = "SELECT IdBook, IdUser FROM BookAccess WHERE IdUser = ?";
        try (Connection conn = DbUtils.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idUser);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    accesses.add(new BookAccess(
                        rs.getInt("IdBook"),
                        rs.getInt("IdUser")
                    ));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return accesses;
    }

    public List<BookAccess> findByBookId(int idBook) {
        List<BookAccess> accesses = new ArrayList<>();
        String sql = "SELECT IdBook, IdUser FROM BookAccess WHERE IdBook = ?";
        try (Connection conn = DbUtils.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idBook);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    accesses.add(new BookAccess(
                        rs.getInt("IdBook"),
                        rs.getInt("IdUser")
                    ));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return accesses;
    }

    public void deleteByBookAndUser(int idBook, int idUser) {
        String sql = "DELETE FROM BookAccess WHERE IdBook = ? AND IdUser = ?";
        try (Connection conn = DbUtils.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idBook);
            ps.setInt(2, idUser);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void deleteByUserId(int idUser) {
        String sql = "DELETE FROM BookAccess WHERE IdUser = ?";
        try (Connection conn = DbUtils.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idUser);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void deleteByBookId(int idBook) {
        String sql = "DELETE FROM BookAccess WHERE IdBook = ?";
        try (Connection conn = DbUtils.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idBook);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
