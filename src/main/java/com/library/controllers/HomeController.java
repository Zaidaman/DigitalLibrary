package com.library.controllers;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Optional;

import javax.swing.JPanel;

import org.icepdf.ri.common.SwingController;
import org.icepdf.ri.common.SwingViewBuilder;

import com.library.dao.BookDAO;
import com.library.dao.LibAccessDAO;
import com.library.dao.LibrariesDAO;
import com.library.models.Book;
import com.library.models.LibAccess;
import com.library.models.LibUser;
import com.library.models.Libraries;

import javafx.embed.swing.SwingNode;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

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

    @FXML
    private Button addLibraryBtn;


    private boolean booksPanelVisible = true;

    private LibUser currentUser;

    @FXML
    public void initialize() {
        // L'inizializzazione vera avviene dopo che l'utente è stato settato
        // (tramite setUser)
        booksPanel.setVisible(false);
        booksPanel.setManaged(false);
        booksPanelVisible = true;
        setupToggleButton();
        setupBookSelection();
        setupAddLibraryBtn();
        showDefaultMessage();
    }

    private void setupAddLibraryBtn() {
        if (addLibraryBtn != null) {
            addLibraryBtn.setOnAction(e -> {
                if (currentUser == null) return;
                TextInputDialog dialog = new TextInputDialog();
                dialog.setTitle("Nuova Libreria");
                dialog.setHeaderText("Crea una nuova libreria");
                dialog.setContentText("Nome della libreria:");
                Optional<String> result = dialog.showAndWait();
                result.ifPresent(libName -> {
                    if (libName.trim().isEmpty()) return;
                    LibrariesDAO librariesDAO = new LibrariesDAO();
                    // Calcola nuovo id manualmente
                    int newId = 1;
                    List<Libraries> allLibs = librariesDAO.findAll();
                    for (Libraries l : allLibs) {
                        if (l.getIdLibrary() >= newId) newId = l.getIdLibrary() + 1;
                    }
                    Libraries newLib = new Libraries(newId, libName.trim());
                    librariesDAO.insert(newLib);
                    // Associa all'utente corrente
                    LibAccessDAO accessDAO = new LibAccessDAO();
                    accessDAO.insert(new LibAccess(currentUser.getIdUser(), newId));
                    // Aggiorna lista
                    loadLibraries();
                });
            });
        }
    }

    // Metodo per ricevere l'utente loggato
    public void setUser(LibUser user) {
        this.currentUser = user;
        loadLibraries();
        setupLibrarySelection();
    }

    private void loadLibraries() {
        libraryList.getItems().clear();
        if (currentUser == null) return;
        LibAccessDAO accessDAO = new LibAccessDAO();
        LibrariesDAO librariesDAO = new LibrariesDAO();
        for (LibAccess access : accessDAO.findByUserId(currentUser.getIdUser())) {
            Libraries lib = librariesDAO.findById(access.getIdLibrary());
            if (lib != null) {
                libraryList.getItems().add(lib.getLibName());
            }
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
