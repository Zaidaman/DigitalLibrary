package com.library.controllers;

import java.io.IOException;

import com.library.dao.DAOFactory;
import com.library.dao.LibUserDAO;
import com.library.models.LibUser;

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
    // @FXML
    // private Button registerButton;
    @FXML
    private Label messageLabel;

    private final LibUserDAO userDAO = DAOFactory.getInstance().getLibUserDAO();

    @FXML
    private void initialize() {
        // Imposta il bottone di login come default (si attiva premendo Enter)
        loginButton.setDefaultButton(true);
    }

    @FXML
    private void onLogin() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        boolean usernameEmpty = username.isEmpty();
        boolean passwordEmpty = password.isEmpty();

        // Verifica campi vuoti usando switch
        String emptyFieldsState = (usernameEmpty ? "U" : "") + (passwordEmpty ? "P" : "");
        switch (emptyFieldsState) {
            case "UP" -> {
                messageLabel.setText("Inserire username e password.");
                return;
            }
            case "U" -> {
                messageLabel.setText("Inserire username.");
                return;
            }
            case "P" -> {
                messageLabel.setText("Inserire password.");
                return;
            }
        }

        // Cerca l'utente per username
        LibUser user = userDAO.findAll().stream()
            .filter(u -> u.getUsername().equals(username))
            .findFirst().orElse(null);

        boolean usernameValid = user != null;
        boolean passwordValid = user != null && user.getUserPass().equals(password);

        // Verifica credenziali usando switch
        String credentialsState = (usernameValid ? "" : "U") + (passwordValid ? "" : "P");
        switch (credentialsState) {
            case "UP" -> messageLabel.setText("Username e password non validi.");
            case "U" -> messageLabel.setText("Username non valido.");
            case "P" -> messageLabel.setText("Password non valida.");
            default -> {
                messageLabel.setText("Login effettuato!");
                goToHome();
            }
        }
    }

    @FXML
    private void onRegister() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        boolean usernameEmpty = username.isEmpty();
        boolean passwordEmpty = password.isEmpty();

        // Verifica campi vuoti usando switch
        String emptyFieldsState = (usernameEmpty ? "U" : "") + (passwordEmpty ? "P" : "");
        switch (emptyFieldsState) {
            case "UP" -> {
                messageLabel.setText("Inserire username e password.");
                return;
            }
            case "U" -> {
                messageLabel.setText("Inserire username.");
                return;
            }
            case "P" -> {
                messageLabel.setText("Inserire password.");
                return;
            }
        }

        // Verifica se username esiste già
        boolean exists = userDAO.findAll().stream()
            .anyMatch(u -> u.getUsername().equals(username));

        if (exists) {
            messageLabel.setText("Username già esistente.");
        } else {
            userDAO.insert(new LibUser(0, username, password, true, false));
            messageLabel.setText("Registrazione avvenuta!");
        }
    }

    private void goToHome() {
        try {
            Stage stage = (Stage) loginButton.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/main-view.fxml"));
            Scene scene = new Scene(loader.load(), 900, 600);
            // Passa l'utente loggato al controller della home
            HomeController homeController = loader.getController();
            LibUser loggedUser = userDAO.findAll().stream()
                .filter(u -> u.getUsername().equals(usernameField.getText()))
                .findFirst().orElse(null);
            if (homeController != null && loggedUser != null) {
                homeController.setUser(loggedUser);
            }
            stage.setScene(scene);
            stage.setTitle("Digital Library - Home");
        } catch (IOException e) {
            // Log dell'errore, senza printStackTrace
            messageLabel.setText("Errore nel caricamento della Home: " + (e.getMessage() != null ? e.getMessage() : "Errore sconosciuto"));
        }
    }
}
