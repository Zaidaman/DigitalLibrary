package com.library.controllers;

import com.library.dao.GenreDAO;
import com.library.models.Genre;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class AddGenreDialogController {
    @FXML private TextField genreNameField;
    @FXML private Button saveGenreBtn;
    @FXML private Button cancelBtn;

    private Genre createdGenre;

    @FXML
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
            closeDialog();
        } else {
            // Opzionale: mostra errore
        }
    }

    private void closeDialog() {
        Stage stage = (Stage) saveGenreBtn.getScene().getWindow();
        stage.close();
    }

    public Genre getCreatedGenre() {
        return createdGenre;
    }
}
