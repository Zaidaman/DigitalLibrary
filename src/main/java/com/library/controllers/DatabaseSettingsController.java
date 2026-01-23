package com.library.controllers;

import java.io.IOException;
import java.util.Properties;

import com.library.utils.ConfigurationManager;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class DatabaseSettingsController {

    @FXML
    private TextField urlField;

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Button saveButton;

    @FXML
    private Button resetButton;

    @FXML
    private Button cancelButton;

    @FXML
    public void initialize() {
        // Carica le impostazioni correnti
        loadCurrentSettings();
        
        // Setup dei pulsanti
        saveButton.setOnAction(event -> handleSave());
        resetButton.setOnAction(event -> handleReset());
        cancelButton.setOnAction(event -> handleCancel());
    }

    private void loadCurrentSettings() {
        Properties props = ConfigurationManager.loadDatabaseProperties();
        urlField.setText(props.getProperty("db.url", ""));
        usernameField.setText(props.getProperty("db.username", ""));
        passwordField.setText(props.getProperty("db.password", ""));
    }

    @FXML
    private void handleSave() {
        String url = urlField.getText().trim();
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        // Validazione base
        if (url.isEmpty() || username.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, 
                     "Campi obbligatori", 
                     "URL e Username sono obbligatori!");
            return;
        }

        // Validazione formato URL
        if (!url.startsWith("jdbc:")) {
            showAlert(Alert.AlertType.WARNING, 
                     "URL non valido", 
                     "L'URL deve iniziare con 'jdbc:'");
            return;
        }

        try {
            ConfigurationManager.saveDatabaseConfiguration(url, username, password);
            
            showAlert(Alert.AlertType.INFORMATION, 
                     "Successo", 
                     """
                     Configurazione salvata con successo!
                     
                     IMPORTANTE: Riavvia l'applicazione per applicare le modifiche.""");
            
            closeWindow();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, 
                     "Errore", 
                     "Impossibile salvare la configurazione:\n" + e.getMessage());
        }
    }

    @FXML
    private void handleReset() {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Conferma Reset");
        confirmAlert.setHeaderText("Ripristinare configurazione di default?");
        confirmAlert.setContentText("""
                Questa operazione eliminerà la configurazione personalizzata.
                Riavvia l'applicazione dopo il reset.""");

        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == javafx.scene.control.ButtonType.OK) {
                try {
                    ConfigurationManager.resetToDefault();
                    showAlert(Alert.AlertType.INFORMATION, 
                             "Reset completato", 
                             """
                             Configurazione ripristinata ai valori di default.
                             Riavvia l'applicazione per applicare le modifiche.""");
                    closeWindow();
                } catch (IOException e) {
                    showAlert(Alert.AlertType.ERROR, 
                             "Errore", 
                             "Impossibile ripristinare la configurazione:\n" + e.getMessage());
                }
            }
        });
    }

    @FXML
    private void handleCancel() {
        closeWindow();
    }

    private void closeWindow() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
