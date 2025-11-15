package com.library.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.layout.StackPane;

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
}
