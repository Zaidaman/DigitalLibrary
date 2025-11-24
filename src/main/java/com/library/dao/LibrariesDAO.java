package com.library.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.library.models.Libraries;
import com.library.utils.DbUtils;

public class LibrariesDAO {
    public void insert(com.library.models.Libraries library) {
        String sql = "INSERT INTO Libraries (LibName) VALUES (?)";
        try (java.sql.Connection conn = com.library.utils.DbUtils.getConnection();
             java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, library.getLibName());
            ps.executeUpdate();
        } catch (java.sql.SQLException e) {
            throw new RuntimeException(e);
        }
    }
    public List<Libraries> findAll() {
        List<Libraries> libraries = new ArrayList<>();
        String sql = "SELECT IdLibrary, LibName FROM Libraries";
        try (Connection conn = DbUtils.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Libraries lib = new Libraries(
                    rs.getInt("IdLibrary"),
                    rs.getString("LibName")
                );
                libraries.add(lib);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return libraries;
    }

    public Libraries findById(int idLibrary) {
        String sql = "SELECT IdLibrary, LibName FROM Libraries WHERE IdLibrary = ?";
        try (Connection conn = DbUtils.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idLibrary);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Libraries(
                        rs.getInt("IdLibrary"),
                        rs.getString("LibName")
                    );
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    public Libraries findByName(String name) {
        String sql = "SELECT IdLibrary, LibName FROM Libraries WHERE LibName = ?";
        try (Connection conn = DbUtils.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new Libraries(
                    rs.getInt("IdLibrary"),
                    rs.getString("LibName")
                );
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

}
