package com.library.models;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.library.utils.DbUtils;

public class LibraryDAO {
    public static List<Library> getAllLibraries() {
        List<Library> libraries = new ArrayList<>();
       try (Connection conn = DbUtils.getConnection();
           Statement stmt = conn.createStatement();
           ResultSet rs = stmt.executeQuery("SELECT IdLibrary, Name FROM `library`")) {
            while (rs.next()) {
                libraries.add(new Library(rs.getInt("IdLibrary"), rs.getString("Name")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return libraries;
    }
}
