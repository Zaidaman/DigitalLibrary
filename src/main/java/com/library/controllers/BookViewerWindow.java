package com.library.controllers;

import java.io.File;

import com.library.models.Book;
import com.library.strategies.BookDisplayStrategy;
import com.library.strategies.EpubDisplayStrategy;
import com.library.strategies.PdfDisplayStrategy;

import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class BookViewerWindow {

    private final Book book;
    private Stage stage;
    private final String userBasePath;

    public BookViewerWindow(Book book) {
        this.book = book;
        this.userBasePath = null;
    }
    
    public BookViewerWindow(Book book, String userBasePath) {
        this.book = book;
        this.userBasePath = userBasePath;
    }

    public void show() {
        try {
            stage = new Stage();
            stage.setTitle(book.getTitle());
            stage.initModality(Modality.APPLICATION_MODAL);

            StackPane contentArea = new StackPane();
            contentArea.setStyle("-fx-padding: 10; -fx-background-color: white;");
            
            // Pattern Strategy: seleziona la strategia in base al tipo di file
            BookDisplayStrategy strategy = getDisplayStrategy(book.getFilePath());
            
            if (strategy != null) {
                String resolvedPath = resolveFilePath(book.getFilePath());
                strategy.display(resolvedPath, contentArea);
            } else {
                javafx.scene.control.Label errorLabel = new javafx.scene.control.Label(
                    "Formato file non supportato: " + book.getFilePath());
                errorLabel.setStyle("-fx-text-fill: red; -fx-font-size: 14px;");
                contentArea.getChildren().add(errorLabel);
            }

            // Calcolare dimensioni proporzionate all'altezza dello schermo
            javafx.stage.Screen screen = javafx.stage.Screen.getPrimary();
            javafx.geometry.Rectangle2D bounds = screen.getVisualBounds();
            
            // Utilizzare il 90% dell'altezza dello schermo per dimensioni più grandi
            double windowHeight = bounds.getHeight() * 0.9;
            // Proporzione rettangolare più larga (circa A4: 1:1.414)
            double windowWidth = windowHeight * 0.75;
            
            Scene scene = new Scene(contentArea, windowWidth, windowHeight);
            stage.setScene(scene);
            
            // Centrare la finestra sullo schermo
            stage.centerOnScreen();
            
            stage.show();

        } catch (Exception e) {
            System.err.println("Errore apertura finestra libro: " + e.getMessage());
        }
    }
    
    /**
     * Pattern Strategy: seleziona la strategia appropriata in base al formato del file
     */
    private BookDisplayStrategy getDisplayStrategy(String filePath) {
        if (filePath.toLowerCase().endsWith(".pdf")) {
            return new PdfDisplayStrategy();
        } else if (filePath.toLowerCase().endsWith(".epub")) {
            return new EpubDisplayStrategy();
        }
        return null; // Formato non supportato
    }
    
    /**
     * Risolve il percorso del file cercando in diverse posizioni
     */
    private String resolveFilePath(String filePath) {
        // Prova prima nella cartella scelta dall'utente
        if (userBasePath != null && !userBasePath.trim().isEmpty()) {
            File userFile = new File(userBasePath + File.separator + filePath);
            if (userFile.exists()) {
                return userFile.getAbsolutePath();
            }
        }
        
        // Fallback: prova in library-data
        File dataFile = new File("library-data/" + filePath);
        if (dataFile.exists()) {
            return dataFile.getAbsolutePath();
        }
        
        // Se nessuno esiste, restituisce il percorso originale
        return filePath;
    }
}