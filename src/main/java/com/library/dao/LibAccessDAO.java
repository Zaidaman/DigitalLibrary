package com.library.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.library.models.LibAccess;
import com.library.utils.DbUtils;

public class LibAccessDAO {
    public void insert(LibAccess access) {
        String sql = "INSERT INTO LibAccess (IdUser, IdLibrary) VALUES (?, ?)";
        try (Connection conn = DbUtils.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, access.getIdUser());
            ps.setInt(2, access.getIdLibrary());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<LibAccess> findAll() {
        List<LibAccess> accesses = new ArrayList<>();
        String sql = "SELECT IdUser, IdLibrary FROM LibAccess";
        try (Connection conn = DbUtils.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                accesses.add(new LibAccess(
                    rs.getInt("IdUser"),
                    rs.getInt("IdLibrary")
                ));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return accesses;
    }

    public List<LibAccess> findByUserId(int idUser) {
        List<LibAccess> accesses = new ArrayList<>();
        String sql = "SELECT IdUser, IdLibrary FROM LibAccess WHERE IdUser = ?";
        try (Connection conn = DbUtils.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idUser);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    accesses.add(new LibAccess(
                        rs.getInt("IdUser"),
                        rs.getInt("IdLibrary")
                    ));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return accesses;
    }

    public List<LibAccess> findByLibraryId(int idLibrary) {
        List<LibAccess> accesses = new ArrayList<>();
        String sql = "SELECT IdUser, IdLibrary FROM LibAccess WHERE IdLibrary = ?";
        try (Connection conn = DbUtils.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idLibrary);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    accesses.add(new LibAccess(
                        rs.getInt("IdUser"),
                        rs.getInt("IdLibrary")
                    ));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return accesses;
    }

    public void deleteByUserAndLibrary(int idUser, int idLibrary) {
        String sql = "DELETE FROM LibAccess WHERE IdUser = ? AND IdLibrary = ?";
        try (Connection conn = DbUtils.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idUser);
            ps.setInt(2, idLibrary);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
