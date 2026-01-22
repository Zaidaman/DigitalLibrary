package com.library.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Properties;

/**
 * Gestisce la configurazione del database.
 * Permette di sovrascrivere le impostazioni di default con configurazioni personalizzate.
 */
public class ConfigurationManager {
    private static final String USER_CONFIG_DIR = System.getProperty("user.home") + File.separator + ".digitallibrary";
    private static final String USER_CONFIG_FILE = USER_CONFIG_DIR + File.separator + "db.properties";
    private static final String DEFAULT_CONFIG_FILE = "mysql.properties";
    
    private static Properties cachedProperties = null;
    
    /**
     * Carica le proprietà del database.
     * Prima prova a caricare il file di configurazione utente, se non esiste usa quello di default.
     */
    public static Properties loadDatabaseProperties() {
        if (cachedProperties != null) {
            return cachedProperties;
        }
        
        Properties props = new Properties();
        
        // Prova prima a caricare il file personalizzato dell'utente
        File userConfigFile = new File(USER_CONFIG_FILE);
        if (userConfigFile.exists()) {
            try (FileInputStream input = new FileInputStream(userConfigFile)) {
                props.load(input);
                cachedProperties = props;
                System.out.println("[INFO] Caricata configurazione DB personalizzata da: " + USER_CONFIG_FILE);
                return props;
            } catch (IOException e) {
                System.err.println("[WARNING] Errore nel caricamento della configurazione personalizzata: " + e.getMessage());
                // Continua a caricare quella di default
            }
        }
        
        // Carica il file di configurazione di default dalle risorse
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
    
    /**
     * Salva le nuove impostazioni del database nel file di configurazione utente.
     */
    public static void saveDatabaseConfiguration(String url, String username, String password) throws IOException {
        // Crea la directory se non esiste
        File configDir = new File(USER_CONFIG_DIR);
        if (!configDir.exists()) {
            configDir.mkdirs();
        }
        
        Properties props = new Properties();
        props.setProperty("db.url", url);
        props.setProperty("db.username", username);
        props.setProperty("db.password", password);
        
        // Salva nel file utente
        try (FileOutputStream output = new FileOutputStream(USER_CONFIG_FILE)) {
            props.store(output, "Digital Library - Database Configuration");
        }
        
        // Invalida la cache per forzare il ricaricamento
        cachedProperties = null;
        
        System.out.println("[INFO] Configurazione DB salvata in: " + USER_CONFIG_FILE);
    }
    
    /**
     * Verifica se esiste una configurazione personalizzata.
     */
    public static boolean hasCustomConfiguration() {
        return new File(USER_CONFIG_FILE).exists();
    }
    
    /**
     * Resetta la configurazione personalizzata e torna a quella di default.
     */
    public static void resetToDefault() throws IOException {
        File userConfigFile = new File(USER_CONFIG_FILE);
        if (userConfigFile.exists()) {
            Files.delete(userConfigFile.toPath());
            cachedProperties = null;
            System.out.println("[INFO] Configurazione personalizzata eliminata");
        }
    }
    
    /**
     * Ottiene il percorso del file di configurazione utente.
     */
    public static String getUserConfigFilePath() {
        return USER_CONFIG_FILE;
    }
}
