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
}
