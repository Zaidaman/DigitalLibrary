package com.library.controllers;

import com.library.models.UserPreferences;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.RadioButton;
import javafx.stage.Stage;

public class PreferencesController {

    @FXML
    private RadioButton lightThemeRadio;
    
    @FXML
    private RadioButton darkThemeRadio;
    
    @FXML
    private RadioButton smallCardRadio;
    
    @FXML
    private RadioButton mediumCardRadio;
    
    @FXML
    private RadioButton largeCardRadio;
    
    @FXML
    private Button saveButton;
    
    @FXML
    private Button cancelButton;
    
    private UserPreferences preferences;
    private Runnable onSaveCallback;
    
    @FXML
    public void initialize() {
        preferences = new UserPreferences();
        
        // Carica le preferenze correnti
        loadCurrentPreferences();
        
        // Setup dei pulsanti
        saveButton.setOnAction(e -> savePreferences());
        cancelButton.setOnAction(e -> closeWindow());
    }
    
    private void loadCurrentPreferences() {
        // Tema
        String theme = preferences.getTheme();
        if (UserPreferences.THEME_DARK.equals(theme)) {
            darkThemeRadio.setSelected(true);
        } else {
            lightThemeRadio.setSelected(true);
        }
        
        // Dimensione card
        String cardSize = preferences.getCardSize();
        switch (cardSize) {
            case UserPreferences.SIZE_SMALL:
                smallCardRadio.setSelected(true);
                break;
            case UserPreferences.SIZE_LARGE:
                largeCardRadio.setSelected(true);
                break;
            case UserPreferences.SIZE_MEDIUM:
            default:
                mediumCardRadio.setSelected(true);
                break;
        }
    }
    
    private void savePreferences() {
        // Salva tema
        if (darkThemeRadio.isSelected()) {
            preferences.setTheme(UserPreferences.THEME_DARK);
        } else {
            preferences.setTheme(UserPreferences.THEME_LIGHT);
        }
        
        // Salva dimensione card
        if (smallCardRadio.isSelected()) {
            preferences.setCardSize(UserPreferences.SIZE_SMALL);
        } else if (largeCardRadio.isSelected()) {
            preferences.setCardSize(UserPreferences.SIZE_LARGE);
        } else {
            preferences.setCardSize(UserPreferences.SIZE_MEDIUM);
        }
        
        // Mostra conferma
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Preferenze salvate");
        alert.setHeaderText(null);
        alert.setContentText("Le preferenze sono state salvate. Riavvia l'applicazione per applicare tutte le modifiche.");
        alert.showAndWait();
        
        // Chiama il callback se impostato
        if (onSaveCallback != null) {
            onSaveCallback.run();
        }
        
        closeWindow();
    }
    
    private void closeWindow() {
        Stage stage = (Stage) saveButton.getScene().getWindow();
        stage.close();
    }
    
    public void setOnSaveCallback(Runnable callback) {
        this.onSaveCallback = callback;
    }
}