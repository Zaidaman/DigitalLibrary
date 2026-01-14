package com.library.controllers;

import com.library.dao.AuthorDAO;
import com.library.models.Author;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class AddAuthorDialogController {
    @FXML private TextField nameField;
    @FXML private TextField midNameField;
    @FXML private TextField surnameField;
    @FXML private Button saveAuthorBtn;
    @FXML private Button cancelBtn;

    private Author createdAuthor;

    @FXML
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
            closeDialog();
        } else {
            // Opzionale: mostra errore
        }
    }

    private void closeDialog() {
        Stage stage = (Stage) saveAuthorBtn.getScene().getWindow();
        stage.close();
    }

    public Author getCreatedAuthor() {
        return createdAuthor;
    }
}
