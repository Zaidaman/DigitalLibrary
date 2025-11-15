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
}
