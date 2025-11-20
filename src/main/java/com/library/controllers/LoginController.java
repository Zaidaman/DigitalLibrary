package com.library.controllers;

import java.io.IOException;

import com.library.dao.LibUserDAO;
import com.library.models.LibUser;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class LoginController {
    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Button loginButton;
    @FXML
    private Button registerButton;
    @FXML
    private Label messageLabel;

    private final LibUserDAO userDAO = new LibUserDAO();

    @FXML
    private void onLogin(ActionEvent event) {
        String username = usernameField.getText();
        String password = passwordField.getText();

        boolean found = userDAO.findAll().stream()
            .anyMatch(u -> u.getUsername().equals(username) && u.getUserPass().equals(password));

        if (found) {
            messageLabel.setText("Login effettuato!");
            goToHome();
        } else {
            messageLabel.setText("Credenziali non valide.");
        }
    }

    @FXML
    private void onRegister(ActionEvent event) {
        String username = usernameField.getText();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            messageLabel.setText("Inserisci username e password.");
            return;
        }

        boolean exists = userDAO.findAll().stream()
            .anyMatch(u -> u.getUsername().equals(username));

        if (exists) {
            messageLabel.setText("Username già esistente.");
        } else {
            userDAO.insert(new LibUser(0, username, password));
            messageLabel.setText("Registrazione avvenuta!");
        }
    }

    private void goToHome() {
        try {
            Stage stage = (Stage) loginButton.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/main-view.fxml"));
            Scene scene = new Scene(loader.load(), 900, 600);
            stage.setScene(scene);
            stage.setTitle("Digital Library - Home");
        } catch (IOException e) {
            messageLabel.setText("Errore nel caricamento della Home.");
        }
    }
}
