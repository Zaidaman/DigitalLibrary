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
    @SuppressWarnings("unused")
    private void initialize() {
        // Imposta il bottone di login come default (si attiva premendo Enter)
        loginButton.setDefaultButton(true);
    }

    @FXML
    @SuppressWarnings("unused")
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
    @SuppressWarnings("unused")
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
            userDAO.insert(new LibUser(0, username, password, true, false, null));
            messageLabel.setText("Registrazione avvenuta!");
        }
    }

    private void goToHome() {
        try {
            // Recupera l'utente loggato
            LibUser loggedUser = userDAO.findAll().stream()
                .filter(u -> u.getUsername().equals(usernameField.getText()))
                .findFirst().orElse(null);
            
            if (loggedUser == null) {
                messageLabel.setText("Errore nel recupero dei dati utente.");
                return;
            }
            
            // Gestisci il primo login
            if (loggedUser.isFirstLogin()) {
                handleFirstLogin(loggedUser);
                // Ricarica l'utente aggiornato dal database
                loggedUser = userDAO.findById(loggedUser.getIdUser());
            }
            
            Stage stage = (Stage) loginButton.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/main-view.fxml"));
            Scene scene = new Scene(loader.load(), 900, 600);
            // Passa l'utente loggato al controller della home
            HomeController homeController = loader.getController();
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
    
    private void handleFirstLogin(LibUser user) {
        // Mostra messaggio di benvenuto
        javafx.scene.control.Alert welcomeAlert = new javafx.scene.control.Alert(
            javafx.scene.control.Alert.AlertType.INFORMATION);
        welcomeAlert.setTitle("Benvenuto!");
        welcomeAlert.setHeaderText("Benvenuto nella Digital Library, " + user.getUsername() + "!");
        welcomeAlert.setContentText("Prima di iniziare, seleziona la cartella dove desideri salvare i tuoi libri.");
        welcomeAlert.showAndWait();
        
        // Apri il dialog per selezionare la cartella
        javafx.stage.DirectoryChooser directoryChooser = new javafx.stage.DirectoryChooser();
        directoryChooser.setTitle("Seleziona cartella per i libri");
        directoryChooser.setInitialDirectory(new java.io.File(System.getProperty("user.home")));
        
        Stage stage = (Stage) loginButton.getScene().getWindow();
        java.io.File selectedDirectory = directoryChooser.showDialog(stage);
        
        if (selectedDirectory != null) {
            // Salva il percorso selezionato nel database
            userDAO.updateChosenPathAndFirstLogin(user.getIdUser(), selectedDirectory.getAbsolutePath());
        } else {
            // Se l'utente annulla, usa una cartella predefinita
            String defaultPath = System.getProperty("user.home") + java.io.File.separator + "DigitalLibrary";
            userDAO.updateChosenPathAndFirstLogin(user.getIdUser(), defaultPath);
            
            javafx.scene.control.Alert infoAlert = new javafx.scene.control.Alert(
                javafx.scene.control.Alert.AlertType.INFORMATION);
            infoAlert.setTitle("Cartella predefinita");
            infoAlert.setHeaderText("Cartella predefinita selezionata");
            infoAlert.setContentText("I tuoi libri verranno salvati in: " + defaultPath);
            infoAlert.showAndWait();
        }
    }
}
