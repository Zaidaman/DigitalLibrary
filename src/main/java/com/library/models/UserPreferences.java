package com.library.models;

import java.io.*;
import java.util.Properties;

public class UserPreferences {
    
    private static final String PREFERENCES_FILE = "user-preferences.properties";
    private Properties properties;
    
    // Chiavi delle preferenze
    public static final String THEME = "theme";
    public static final String CARD_SIZE = "card.size";
    
    // Valori predefiniti
    public static final String THEME_LIGHT = "light";
    public static final String THEME_DARK = "dark";
    public static final String SIZE_SMALL = "small";
    public static final String SIZE_MEDIUM = "medium";
    public static final String SIZE_LARGE = "large";
    
    public UserPreferences() {
        properties = new Properties();
        loadPreferences();
    }
    
    private void loadPreferences() {
        File file = new File(PREFERENCES_FILE);
        if (file.exists()) {
            try (FileInputStream fis = new FileInputStream(file)) {
                properties.load(fis);
            } catch (IOException e) {
                System.err.println("Errore nel caricamento delle preferenze: " + e.getMessage());
                setDefaults();
            }
        } else {
            setDefaults();
        }
    }
    
    private void setDefaults() {
        properties.setProperty(THEME, THEME_LIGHT);
        properties.setProperty(CARD_SIZE, SIZE_MEDIUM);
        savePreferences();
    }
    
    public void savePreferences() {
        try (FileOutputStream fos = new FileOutputStream(PREFERENCES_FILE)) {
            properties.store(fos, "User Preferences for Digital Library");
        } catch (IOException e) {
            System.err.println("Errore nel salvataggio delle preferenze: " + e.getMessage());
        }
    }
    
    public String getTheme() {
        return properties.getProperty(THEME, THEME_LIGHT);
    }
    
    public void setTheme(String theme) {
        properties.setProperty(THEME, theme);
        savePreferences();
    }
    
    public String getCardSize() {
        return properties.getProperty(CARD_SIZE, SIZE_MEDIUM);
    }
    
    public void setCardSize(String size) {
        properties.setProperty(CARD_SIZE, size);
        savePreferences();
    }
    
    // Metodo per ottenere le dimensioni delle card in base alla preferenza
    public CardDimensions getCardDimensions() {
        String size = getCardSize();
        switch (size) {
            case SIZE_SMALL:
                return new CardDimensions(120, 160);
            case SIZE_LARGE:
                return new CardDimensions(180, 240);
            case SIZE_MEDIUM:
            default:
                return new CardDimensions(150, 200);
        }
    }
    
    public static class CardDimensions {
        private final int width;
        private final int height;
        
        public CardDimensions(int width, int height) {
            this.width = width;
            this.height = height;
        }
        
        public int getWidth() {
            return width;
        }
        
        public int getHeight() {
            return height;
        }
    }
}