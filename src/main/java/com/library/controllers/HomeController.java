package com.library.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.control.Button;
import javafx.embed.swing.SwingNode;

import com.library.dao.BookDAO;
import com.library.dao.LibrariesDAO;
import com.library.models.Book;
import com.library.models.Libraries;

import javax.swing.*;

import org.icepdf.ri.common.SwingController;
import org.icepdf.ri.common.SwingViewBuilder;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
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

    private boolean booksPanelVisible = true;

    @FXML
    public void initialize() {
        loadLibraries();
        setupLibrarySelection();

        // pannello libri nascosto inizialmente
        booksPanel.setVisible(false);
        booksPanel.setManaged(false);
        booksPanelVisible = true;

        setupToggleButton();
        setupBookSelection();

        showDefaultMessage();
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

        // mostra pannello libri
        booksPanel.setVisible(true);
        booksPanel.setManaged(true);
        booksPanelVisible = true;
        toggleBooksBtn.setText("⮜");

        booksList.getItems().clear();
        if (books.isEmpty()) {
            booksList.getItems().add("Nessun libro presente in questa libreria.");
            showDefaultMessage();
        } else {
            for (Book b : books) {
                booksList.getItems().add(b.getTitle());
            }
            showDefaultMessage(); // messaggio di base
        }
    }

    private void setupToggleButton() {
        toggleBooksBtn.setOnAction(e -> {
            if (booksPanelVisible) {
                booksPanel.setVisible(false);
                booksPanel.setManaged(false);
                toggleBooksBtn.setText("⮞");
                booksPanelVisible = false;
            } else {
                booksPanel.setVisible(true);
                booksPanel.setManaged(true);
                toggleBooksBtn.setText("⮜");
                booksPanelVisible = true;
            }
        });
    }

    private void setupBookSelection() {
        booksList.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && !newVal.equals("Nessun libro presente in questa libreria.")) {
                BookDAO bookDAO = new BookDAO();
                Book book = bookDAO.findByTitle(newVal); // restituisce filePath relativo

                if (book != null && book.getFilePath() != null && !book.getFilePath().isEmpty()) {
                    showPdfFromResource(book.getFilePath());
                } else {
                    showDefaultMessage();
                }
            } else {
                showDefaultMessage();
            }
        });
    }

    private void showDefaultMessage() {
        Label defaultLabel = new Label("Benvenuto nella tua Libreria Digitale!");
        defaultLabel.setStyle("-fx-font-size: 20px;");
        contentArea.getChildren().setAll(defaultLabel);
    }

    private void showPdfFromResource(String resourcePath) {
        try {
            // Carica la risorsa dal classpath
            InputStream pdfStream = getClass().getClassLoader().getResourceAsStream(resourcePath);
            if (pdfStream == null) {
                System.out.println("PDF non trovato: " + resourcePath);
                showDefaultMessage();
                return;
            }

            // Crea un file temporaneo leggibile da IcePDF
            File tempFile = File.createTempFile("temp_pdf_", ".pdf");
            tempFile.deleteOnExit();
            Files.copy(pdfStream, tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

            // IcePDF
            SwingController controller = new SwingController();
            SwingViewBuilder factory = new SwingViewBuilder(controller);
            JPanel viewerPanel = factory.buildViewerPanel();
            controller.openDocument(tempFile.getAbsolutePath());

            SwingNode swingNode = new SwingNode();
            swingNode.setContent(viewerPanel);

            contentArea.getChildren().setAll(swingNode);

        } catch (Exception e) {
            e.printStackTrace();
            showDefaultMessage();
        }
    }
}
