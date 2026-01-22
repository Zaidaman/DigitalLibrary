package com.library.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DbUtils {
    public static Connection getConnection() throws SQLException {
        // Usa il ConfigurationManager per caricare le proprietà
        Properties props = ConfigurationManager.loadDatabaseProperties();

        String url = props.getProperty("db.url");
        String username = props.getProperty("db.username");
        String password = props.getProperty("db.password");

        return DriverManager.getConnection(url, username, password);
    }
}