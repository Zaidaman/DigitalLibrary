package com.library.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Properties;

public class ConfigurationManager {
    private static final String USER_CONFIG_DIR = System.getProperty("user.home") + File.separator + ".digitallibrary";
    private static final String USER_CONFIG_FILE = USER_CONFIG_DIR + File.separator + "db.properties";
    private static final String DEFAULT_CONFIG_FILE = "mysql.properties";
    
    private static Properties cachedProperties = null;
    
    public static Properties loadDatabaseProperties() {
        if (cachedProperties != null) {
            return cachedProperties;
        }
        
        Properties props = new Properties();
        
        File userConfigFile = new File(USER_CONFIG_FILE);
        if (userConfigFile.exists()) {
            try (FileInputStream input = new FileInputStream(userConfigFile)) {
                props.load(input);
                cachedProperties = props;
                System.out.println("[INFO] Caricata configurazione DB personalizzata da: " + USER_CONFIG_FILE);
                return props;
            } catch (IOException e) {
                System.err.println("[WARNING] Errore nel caricamento della configurazione personalizzata: " + e.getMessage());
            }
        }
        
        try (InputStream input = ConfigurationManager.class.getClassLoader().getResourceAsStream(DEFAULT_CONFIG_FILE)) {
            if (input == null) {
                throw new RuntimeException("Unable to find " + DEFAULT_CONFIG_FILE);
            }
            props.load(input);
            cachedProperties = props;
            System.out.println("[INFO] Caricata configurazione DB di default");
            return props;
        } catch (IOException e) {
            throw new RuntimeException("Failed to load database properties", e);
        }
    }
    
    public static void saveDatabaseConfiguration(String url, String username, String password) throws IOException {
        File configDir = new File(USER_CONFIG_DIR);
        if (!configDir.exists()) {
            configDir.mkdirs();
        }
        
        Properties props = new Properties();
        props.setProperty("db.url", url);
        props.setProperty("db.username", username);
        props.setProperty("db.password", password);
        
        try (FileOutputStream output = new FileOutputStream(USER_CONFIG_FILE)) {
            props.store(output, "Digital Library - Database Configuration");
        }
        
        cachedProperties = null;
        
        System.out.println("[INFO] Configurazione DB salvata in: " + USER_CONFIG_FILE);
    }
    
    public static boolean hasCustomConfiguration() {
        return new File(USER_CONFIG_FILE).exists();
    }
    
    public static void resetToDefault() throws IOException {
        File userConfigFile = new File(USER_CONFIG_FILE);
        if (userConfigFile.exists()) {
            Files.delete(userConfigFile.toPath());
            cachedProperties = null;
            System.out.println("[INFO] Configurazione personalizzata eliminata");
        }
    }
    
    public static String getUserConfigFilePath() {
        return USER_CONFIG_FILE;
    }
}
