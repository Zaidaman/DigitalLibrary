package com.library.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.library.models.LibUser;
import com.library.utils.DbUtils;

public class LibUserDAO {
        public void insert(com.library.models.LibUser user) {
            String sql = "INSERT INTO LibUser (Username, UserPass) VALUES (?, ?)";
            try (java.sql.Connection conn = com.library.utils.DbUtils.getConnection();
                 java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, user.getUsername());
                ps.setString(2, user.getUserPass());
                ps.executeUpdate();
            } catch (java.sql.SQLException e) {
                throw new RuntimeException(e);
            }
        }
    public List<LibUser> findAll() {
        List<LibUser> users = new ArrayList<>();
        String sql = "SELECT IdUser, Username, UserPass FROM LibUser";
        try (Connection conn = DbUtils.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                LibUser user = new LibUser(
                    rs.getInt("IdUser"),
                    rs.getString("Username"),
                    rs.getString("UserPass")
                );
                users.add(user);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return users;
    }

    public LibUser findById(int idUser) {
        String sql = "SELECT IdUser, Username, UserPass FROM LibUser WHERE IdUser = ?";
        try (Connection conn = DbUtils.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idUser);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new LibUser(
                        rs.getInt("IdUser"),
                        rs.getString("Username"),
                        rs.getString("UserPass")
                    );
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }
}
