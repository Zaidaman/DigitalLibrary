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
        public int insert(com.library.models.LibUser user) {
            String sql = "INSERT INTO LibUser (Username, UserPass) VALUES (?, ?)";
            try (java.sql.Connection conn = com.library.utils.DbUtils.getConnection();
                 java.sql.PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, user.getUsername());
                ps.setString(2, user.getUserPass());
                ps.executeUpdate();
                
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        return rs.getInt(1);
                    }
                }
            } catch (java.sql.SQLException e) {
                throw new RuntimeException(e);
            }
            return -1;
        }
    public List<LibUser> findAll() {
        List<LibUser> users = new ArrayList<>();
        String sql = "SELECT IdUser, Username, UserPass, FirstLogin, IsAdmin, ChosenPath FROM LibUser";
        try (Connection conn = DbUtils.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                LibUser user = new LibUser(
                    rs.getInt("IdUser"),
                    rs.getString("Username"),
                    rs.getString("UserPass"),
                    rs.getBoolean("FirstLogin"),
                    rs.getBoolean("IsAdmin"),
                    rs.getString("ChosenPath")
                );
                users.add(user);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return users;
    }

    public LibUser findById(int idUser) {
        String sql = "SELECT IdUser, Username, UserPass, FirstLogin, IsAdmin, ChosenPath FROM LibUser WHERE IdUser = ?";
        try (Connection conn = DbUtils.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idUser);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new LibUser(
                        rs.getInt("IdUser"),
                        rs.getString("Username"),
                        rs.getString("UserPass"),
                        rs.getBoolean("FirstLogin"),
                        rs.getBoolean("IsAdmin"),
                        rs.getString("ChosenPath")
                    );
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    public void updateChosenPathAndFirstLogin(int idUser, String chosenPath) {
        String sql = "UPDATE LibUser SET ChosenPath = ?, FirstLogin = 0 WHERE IdUser = ?";
        try (Connection conn = DbUtils.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, chosenPath);
            ps.setInt(2, idUser);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean update(int idUser, String newUsername, String newPassword) {
        // Costruisce dinamicamente la query SQL in base ai parametri forniti
        StringBuilder sql = new StringBuilder("UPDATE LibUser SET ");
        List<String> updates = new ArrayList<>();
        
        if (newUsername != null && !newUsername.trim().isEmpty()) {
            updates.add("Username = ?");
        }
        if (newPassword != null && !newPassword.trim().isEmpty()) {
            updates.add("UserPass = ?");
        }
        
        if (updates.isEmpty()) {
            return false; // Nessun campo da aggiornare
        }
        
        sql.append(String.join(", ", updates));
        sql.append(" WHERE IdUser = ?");
        
        try (Connection conn = DbUtils.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            
            int paramIndex = 1;
            if (newUsername != null && !newUsername.trim().isEmpty()) {
                ps.setString(paramIndex++, newUsername);
            }
            if (newPassword != null && !newPassword.trim().isEmpty()) {
                ps.setString(paramIndex++, newPassword);
            }
            ps.setInt(paramIndex, idUser);
            
            int rowsAffected = ps.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void delete(int idUser) {
        String sql = "DELETE FROM LibUser WHERE IdUser = ?";
        try (Connection conn = DbUtils.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idUser);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
