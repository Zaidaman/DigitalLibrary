package com.library.controllers;

import java.io.File;

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
        loadLibraries();
        loadBooks();
    }

    private void loadLibraries() {
        com.library.dao.LibrariesDAO librariesDAO = new com.library.dao.LibrariesDAO();
        libraryList.getItems().clear();
        for (com.library.models.Libraries lib : librariesDAO.findAll()) {
            libraryList.getItems().add(lib.getLibName());
        }
    }

    private void loadBooks() {
        com.library.dao.BookDAO bookDAO = new com.library.dao.BookDAO();
        java.util.List<com.library.models.Book> books = bookDAO.findAll();
        StringBuilder sb = new StringBuilder();
        sb.append("Libri presenti nel DB:\n");
        for (com.library.models.Book book : books) {
            sb.append("- ").append(book.getTitle()).append("\n");
        }
        javafx.scene.control.Label booksLabel = new javafx.scene.control.Label(sb.toString());
        booksLabel.setStyle("-fx-font-size: 16px;");
        contentArea.getChildren().clear();
        contentArea.getChildren().add(booksLabel);
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
