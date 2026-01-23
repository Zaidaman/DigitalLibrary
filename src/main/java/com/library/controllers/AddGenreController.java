package com.library.controllers;

import com.library.dao.GenreDAO;
import com.library.models.Genre;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class AddGenreController {
    @FXML private TextField genreNameField;
    @FXML private Button saveGenreBtn;
    @FXML private Button cancelBtn;

    private Genre createdGenre;

    @FXML
    @SuppressWarnings("unused") // Called by JavaFX FXML loader
    private void initialize() {
        saveGenreBtn.setOnAction(e -> saveGenre());
        cancelBtn.setOnAction(e -> closeDialog());
    }

    private void saveGenre() {
        String name = genreNameField.getText().trim();
        if (!name.isEmpty()) {
            Genre genre = new Genre(0, name);
            new GenreDAO().insert(genre);
            this.createdGenre = genre;
            
            showNotification("Genere aggiunto con successo!");
            
            new Thread(() -> {
                try {
                    Thread.sleep(1500);
                    javafx.application.Platform.runLater(() -> closeDialog());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }).start();
        } else {
            genreNameField.setStyle("-fx-border-color: red;");
        }
    }
    
    private void showNotification(String message) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
        alert.setTitle("Notifica");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.show();
        
        // Chiudi automaticamente dopo 2 secondi
        new Thread(() -> {
            try {
                Thread.sleep(2000);
                javafx.application.Platform.runLater(() -> alert.close());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }

    private void closeDialog() {
        Stage stage = (Stage) saveGenreBtn.getScene().getWindow();
        stage.close();
    }

    public Genre getCreatedGenre() {
        return createdGenre;
    }
}
