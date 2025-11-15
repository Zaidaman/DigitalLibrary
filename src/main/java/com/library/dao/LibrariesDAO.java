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
    public List<Libraries> findAll() {
        List<Libraries> libraries = new ArrayList<>();
        String sql = "SELECT IdLibrary, LibName, IdUser FROM Libraries";
        try (Connection conn = DbUtils.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Libraries lib = new Libraries(
                    rs.getInt("IdLibrary"),
                    rs.getString("LibName"),
                    rs.getInt("IdUser")
                );
                libraries.add(lib);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return libraries;
    }

    public Libraries findById(int idLibrary) {
        String sql = "SELECT IdLibrary, LibName, IdUser FROM Libraries WHERE IdLibrary = ?";
        try (Connection conn = DbUtils.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idLibrary);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Libraries(
                        rs.getInt("IdLibrary"),
                        rs.getString("LibName"),
                        rs.getInt("IdUser")
                    );
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }
}
