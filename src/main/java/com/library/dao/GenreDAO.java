package com.library.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.library.models.Genre;
import com.library.utils.DbUtils;

/**
 * DAO per la gestione dei generi nel database.
 * Implementa BaseDAO per le operazioni CRUD standard.
 */
public class GenreDAO implements BaseDAO<Genre> {
    @Override
    public int insert(Genre genre) {
            String sql = "INSERT INTO Genre (GenreName) VALUES (?)";
            try (java.sql.Connection conn = com.library.utils.DbUtils.getConnection();
                 java.sql.PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, genre.getGenreName());
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
    
    @Override
    public List<Genre> findAll() {
        List<Genre> genres = new ArrayList<>();
        String sql = "SELECT IdGenre, GenreName FROM Genre";
        try (Connection conn = DbUtils.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Genre genre = new Genre(
                    rs.getInt("IdGenre"),
                    rs.getString("GenreName")
                );
                genres.add(genre);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return genres;
    }

    @Override
    public Genre findById(int idGenre) {
        String sql = "SELECT IdGenre, GenreName FROM Genre WHERE IdGenre = ?";
        try (Connection conn = DbUtils.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idGenre);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Genre(
                        rs.getInt("IdGenre"),
                        rs.getString("GenreName")
                    );
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }
    
    @Override
    public void delete(int id) {
        String sql = "DELETE FROM Genre WHERE IdGenre = ?";
        try (Connection conn = DbUtils.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
