package com.library.strategies;

import javafx.geometry.Insets;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.layout.StackPane;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Strategy per visualizzare file di testo (.txt)
 * Implementa BookDisplayStrategy seguendo il Pattern Strategy
 */
public class TxtDisplayStrategy implements BookDisplayStrategy {

    @Override
    public void display(String filePath, StackPane contentArea) {
        File file = new File(filePath);
        
        if (!file.exists()) {
            showError(contentArea, "File non trovato: " + filePath);
            return;
        }
        
        try {
            byte[] bytes = Files.readAllBytes(Paths.get(filePath));

            String content;

            // UTF-16 LE BOM
            if (bytes.length >= 2 && bytes[0] == (byte)0xFF && bytes[1] == (byte)0xFE) {
                content = new String(bytes, StandardCharsets.UTF_16LE);
            }
            // UTF-16 BE BOM
            else if (bytes.length >= 2 && bytes[0] == (byte)0xFE && bytes[1] == (byte)0xFF) {
                content = new String(bytes, StandardCharsets.UTF_16BE);
            }
            // UTF-8 BOM
            else if (bytes.length >= 3 &&
                    bytes[0] == (byte)0xEF &&
                    bytes[1] == (byte)0xBB &&
                    bytes[2] == (byte)0xBF) {

                content = new String(bytes, StandardCharsets.UTF_8);
                content = content.replace("\uFEFF", "");
            }
            // Default UTF-8
            else {
                content = new String(bytes, StandardCharsets.UTF_8);
            }
            content = content.trim();

            if (content.isBlank()) {
                showError(contentArea, "Il file è vuoto o non può essere letto correttamente");
                return;
            }
            
            // Crea TextArea per visualizzare il testo
            TextArea textArea = new TextArea(content);
            textArea.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
            textArea.setEditable(false);
            textArea.setWrapText(true);
            textArea.setStyle(
                "-fx-font-family: 'Courier New', monospace; " +
                "-fx-font-size: 14px; " +
                "-fx-control-inner-background: #fafafa; " +
                "-fx-text-fill: #2c3e50;"
            );
            
            // Aggiungi ScrollPane per gestire testi lunghi
            ScrollPane scrollPane = new ScrollPane(textArea);
            scrollPane.setFitToWidth(true);
            scrollPane.setFitToHeight(true);
            scrollPane.setPadding(new Insets(10));
            
            contentArea.getChildren().clear();
            contentArea.getChildren().add(scrollPane);
            
        } catch (IOException e) {
            showError(contentArea, "Errore nella lettura del file: " + e.getMessage());
        } catch (Exception e) {
            showError(contentArea, "Errore imprevisto: " + e.getMessage());
        }
    }
    
    /**
     * Mostra un messaggio di errore nell'area contenuto
     */
    private void showError(StackPane contentArea, String message) {
        javafx.scene.control.Label errorLabel = new javafx.scene.control.Label(message);
        errorLabel.setStyle("-fx-text-fill: red; -fx-font-size: 14px; -fx-padding: 20;");
        errorLabel.setWrapText(true);
        
        contentArea.getChildren().clear();
        contentArea.getChildren().add(errorLabel);
    }
}