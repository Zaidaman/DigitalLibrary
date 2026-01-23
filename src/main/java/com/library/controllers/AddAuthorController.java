package com.library.controllers;

import com.library.dao.AuthorDAO;
import com.library.models.Author;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class AddAuthorController {
    @FXML private TextField nameField;
    @FXML private TextField midNameField;
    @FXML private TextField surnameField;
    @FXML private Button saveAuthorBtn;
    @FXML private Button cancelBtn;

    private Author createdAuthor;

    @FXML
    @SuppressWarnings("unused")
    private void initialize() {
        saveAuthorBtn.setOnAction(e -> saveAuthor());
        cancelBtn.setOnAction(e -> closeDialog());
    }

    private void saveAuthor() {
        String name = nameField.getText().trim();
        String mid = midNameField.getText().trim();
        String surname = surnameField.getText().trim();
        if (!name.isEmpty() && !surname.isEmpty()) {
            Author author = new Author(0, name, mid, surname);
            new AuthorDAO().insert(author);
            this.createdAuthor = author;
            
            showNotification("Autore aggiunto con successo!");
            
            new Thread(() -> {
                try {
                    Thread.sleep(1500);
                    javafx.application.Platform.runLater(() -> closeDialog());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }).start();
        } else {
            // Mostra errore se nome o cognome sono vuoti
            nameField.setStyle("-fx-border-color: red;");
            surnameField.setStyle("-fx-border-color: red;");
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
        Stage stage = (Stage) saveAuthorBtn.getScene().getWindow();
        stage.close();
    }

    public Author getCreatedAuthor() {
        return createdAuthor;
    }
}
