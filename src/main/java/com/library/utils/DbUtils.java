package com.library.utils;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DbUtils {
    public static Connection getConnection() throws SQLException {
        Properties props = new Properties();
        try (InputStream input = DbUtils.class.getClassLoader().getResourceAsStream("mysql.properties")) {
            if (input == null) {
                throw new RuntimeException("Unable to find mysql.properties");
            }
            props.load(input);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load database properties", e);
        }

        String url = props.getProperty("db.url");
        String username = props.getProperty("db.username");
        String password = props.getProperty("db.password");

        return DriverManager.getConnection(url, username, password);
    }
}