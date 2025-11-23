package com.library.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.control.Button;

import com.library.dao.BookDAO;
import com.library.dao.LibrariesDAO;
import com.library.models.Book;
import com.library.models.Libraries;

import java.util.List;

public class HomeController {

    @FXML
    private ListView<String> libraryList;

    @FXML
    private VBox booksPanel;

    @FXML
    private ListView<String> booksList;

    @FXML
    private StackPane contentArea;

    @FXML
    private Button toggleBooksBtn;

    private boolean booksPanelVisible = true; // stato iniziale

    @FXML
    public void initialize() {
        loadLibraries();
        setupLibrarySelection();

        // pannello libri inizialmente nascosto finché non selezioni una libreria
        booksPanel.setVisible(false);
        booksPanel.setManaged(false);
        booksPanelVisible = true; // lo consideriamo pronto ad aprirsi quando selezioniamo libreria

        setupToggleButton();
    }

    private void loadLibraries() {
        LibrariesDAO librariesDAO = new LibrariesDAO();
        libraryList.getItems().clear();
        for (Libraries lib : librariesDAO.findAll()) {
            libraryList.getItems().add(lib.getLibName());
        }
    }

    private void setupLibrarySelection() {
        libraryList.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                loadBooksForLibrary(newVal);
            }
        });
    }

    private void loadBooksForLibrary(String libraryName) {
        LibrariesDAO libDAO = new LibrariesDAO();
        Libraries library = libDAO.findByName(libraryName);
        if (library == null) return;

        BookDAO bookDAO = new BookDAO();
        List<Book> books = bookDAO.findByLibraryId(library.getIdLibrary());

        // Mostra il pannello dei libri
        booksPanel.setVisible(true);
        booksPanel.setManaged(true);
        booksPanelVisible = true;
        toggleBooksBtn.setText("⮜");

        // Popola la lista dei libri
        booksList.getItems().clear();
        if (books.isEmpty()) {
            booksList.getItems().add("Nessun libro presente in questa libreria.");
        } else {
            for (Book b : books) {
                booksList.getItems().add(b.getTitle());
            }
        }

        // Aggiorna area centrale con titolo informativo
        Label info = new Label("Libri della libreria: " + library.getLibName());
        info.setStyle("-fx-font-size: 18px;");
        contentArea.getChildren().setAll(info);
    }

    private void setupToggleButton() {
        toggleBooksBtn.setOnAction(e -> {
            if (booksPanelVisible) {
                // Nascondi pannello libri
                booksPanel.setVisible(false);
                booksPanel.setManaged(false);
                toggleBooksBtn.setText("⮞"); // freccia verso destra
                booksPanelVisible = false;
            } else {
                // Mostra pannello libri
                booksPanel.setVisible(true);
                booksPanel.setManaged(true);
                toggleBooksBtn.setText("⮜"); // freccia verso sinistra
                booksPanelVisible = true;
            }
        });
    }
}
