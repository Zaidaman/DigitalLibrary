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

public class GenreDAO {
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
}
