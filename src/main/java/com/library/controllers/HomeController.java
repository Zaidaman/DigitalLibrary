package com.library.controllers;

import java.io.File;

import com.library.models.Library;
import com.library.models.LibraryDAO;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.layout.StackPane;
import javafx.scene.web.WebView;

public class HomeController {

    @FXML
    private ListView<String> libraryList;
    @FXML
    private Button addLibraryBtn;
    @FXML
    private StackPane contentArea;

    @FXML
    public void initialize() {
        // Carica le librerie dal database
        for (Library lib : LibraryDAO.getAllLibraries()) {
            libraryList.getItems().add(lib.getName());
        }
        libraryList.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null)
                openLibrary(newVal);
        });
    }

    private void openLibrary(String libraryName) {
        contentArea.getChildren().clear();
        contentArea.getChildren().add(new javafx.scene.control.Label("Libri nella libreria: " + libraryName));
    }

    // Lettura file (esempio semplice)
    private void openBook(File file) {
        String ext = getFileExtension(file.getName());
        contentArea.getChildren().clear();

        switch (ext) {
            case "txt":
            case "html":
                WebView webView = new WebView();
                webView.getEngine().load(file.toURI().toString());
                contentArea.getChildren().add(webView);
                break;
            case "pdf":
                contentArea.getChildren().add(new javafx.scene.control.Label("Apertura PDF: " + file.getName()));
                // (Puoi integrare Apache PDFBox qui)
                break;
            case "epub":
                contentArea.getChildren().add(new javafx.scene.control.Label("Apertura EPUB: " + file.getName()));
                // (Integrazione epublib qui)
                break;
            default:
                contentArea.getChildren().add(new javafx.scene.control.Label("Formato non supportato"));
        }
    }

    private String getFileExtension(String fileName) {
        int lastDot = fileName.lastIndexOf('.');
        return (lastDot > 0) ? fileName.substring(lastDot + 1).toLowerCase() : "";
    }
}
