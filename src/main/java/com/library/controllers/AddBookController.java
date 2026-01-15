package com.library.controllers;

import java.io.File;
import java.util.List;

import com.library.dao.AuthorDAO;
import com.library.dao.BookDAO;
import com.library.dao.GenreDAO;
import com.library.dao.LibAccessDAO;
import com.library.dao.LibrariesDAO;
import com.library.models.Author;
import com.library.models.Genre;
import com.library.models.LibAccess;
import com.library.models.LibUser;
import com.library.models.Libraries;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class AddBookController {

    @FXML private ComboBox<String> libraryComboBox;
    @FXML private Button selectBookFileBtn;
    @FXML private Label selectedBookFileLabel;
    @FXML private TextField titleField;
    @FXML private TextField yearField;
    @FXML private TextField authorField;
    @FXML private Button addAuthorBtn;
    @FXML private ListView<String> authorSuggestionsList;
    @FXML private TextField genreField;
    @FXML private Button addGenreBtn;
    @FXML private ListView<String> genreSuggestionsList;
    @FXML private Button saveBookBtn;

    private File selectedBookFile;
    private LibUser currentUser;

    public void setUser(LibUser user) {
        this.currentUser = user;
        loadLibraries();
    }

    @FXML
    private void initialize() {
        selectBookFileBtn.setOnAction(e -> selectBookFile());
        authorField.textProperty().addListener((obs, oldVal, newVal) -> suggestAuthors(newVal));
        addAuthorBtn.setOnAction(e -> openAddAuthorDialog());
        genreField.textProperty().addListener((obs, oldVal, newVal) -> suggestGenres(newVal));
        addGenreBtn.setOnAction(e -> openAddGenreDialog());
        saveBookBtn.setOnAction(e -> saveBook());
        
        // Mostra suggerimenti quando il campo autore riceve il focus
        authorField.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
            if (isNowFocused) {
                suggestAuthors(authorField.getText());
            } else {
                // Nascondi lista quando perde il focus (con un piccolo delay)
                javafx.application.Platform.runLater(() -> {
                    if (!authorSuggestionsList.isFocused()) {
                        authorSuggestionsList.setVisible(false);
                        authorSuggestionsList.setManaged(false);
                    }
                });
            }
        });
        
        // Mostra suggerimenti quando il campo genere riceve il focus
        genreField.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
            if (isNowFocused) {
                suggestGenres(genreField.getText());
            } else {
                // Nascondi lista quando perde il focus (con un piccolo delay)
                javafx.application.Platform.runLater(() -> {
                    if (!genreSuggestionsList.isFocused()) {
                        genreSuggestionsList.setVisible(false);
                        genreSuggestionsList.setManaged(false);
                    }
                });
            }
        });
        
        // Permetti di selezionare autore dalla lista
        authorSuggestionsList.setOnMouseClicked(e -> {
            String selected = authorSuggestionsList.getSelectionModel().getSelectedItem();
            if (selected != null) {
                authorField.setText(selected);
                authorSuggestionsList.setVisible(false);
                authorSuggestionsList.setManaged(false);
            }
        });
        
        // Permetti di selezionare genere dalla lista
        genreSuggestionsList.setOnMouseClicked(e -> {
            String selected = genreSuggestionsList.getSelectionModel().getSelectedItem();
            if (selected != null) {
                genreField.setText(selected);
                genreSuggestionsList.setVisible(false);
                genreSuggestionsList.setManaged(false);
            }
        });
    }

    private void loadLibraries() {
        LibAccessDAO accessDAO = new LibAccessDAO();
        LibrariesDAO librariesDAO = new LibrariesDAO();
        List<LibAccess> accesses = accessDAO.findByUserId(currentUser.getIdUser());
        for (LibAccess access : accesses) {
            Libraries lib = librariesDAO.findById(access.getIdLibrary());
            if (lib != null) libraryComboBox.getItems().add(lib.getLibName());            
        }
    }

    private void selectBookFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("PDF Files", "*.pdf"),
            new FileChooser.ExtensionFilter("EPUB Files", "*.epub")
        );
        Stage stage = (Stage) selectBookFileBtn.getScene().getWindow();
        File file = fileChooser.showOpenDialog(stage);
        if (file != null) {
            selectedBookFile = file;
            selectedBookFileLabel.setText(file.getName());
            titleField.setText(file.getName().replaceFirst("[.][^.]+$", ""));
        }
    }

    private void suggestAuthors(String query) {
        AuthorDAO authorDAO = new AuthorDAO();
        List<Author> authors = authorDAO.findAll();
        authorSuggestionsList.getItems().clear();
        
        if (query == null || query.trim().isEmpty()) {
            // Mostra tutti gli autori se la query è vuota
            for (Author a : authors) {
                authorSuggestionsList.getItems().add(a.getAuthorName() + " " + a.getSurname());
            }
        } else {
            // Filtra gli autori in base alla query
            for (Author a : authors) {
                if (a.getAuthorName().toLowerCase().contains(query.toLowerCase()) ||
                    a.getSurname().toLowerCase().contains(query.toLowerCase())) {
                    authorSuggestionsList.getItems().add(a.getAuthorName() + " " + a.getSurname());
                }
            }
        }
        
        authorSuggestionsList.setVisible(!authorSuggestionsList.getItems().isEmpty());
        authorSuggestionsList.setManaged(!authorSuggestionsList.getItems().isEmpty());
    }

    private void openAddAuthorDialog() {
        try {
        javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
            getClass().getResource("/fxml/add-author-dialog.fxml"));
        javafx.scene.Parent root = loader.load();

        javafx.stage.Stage dialogStage = new javafx.stage.Stage();
        dialogStage.setTitle("Aggiungi autore");
        dialogStage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
        dialogStage.setScene(new javafx.scene.Scene(root));
        dialogStage.showAndWait();

        AddAuthorDialogController controller = loader.getController();
        Author newAuthor = controller.getCreatedAuthor();
        if (newAuthor != null) {
            // Aggiorna suggerimenti o seleziona l'autore appena creato
            authorField.setText(newAuthor.getAuthorName() + " " + newAuthor.getSurname());
            suggestAuthors(authorField.getText());
        }
    } catch (Exception e) {
        System.err.println("Errore nell'apertura della dialog autore: " + e.getMessage());
    }
    }

    private void suggestGenres(String query) {
        GenreDAO genreDAO = new GenreDAO();
        List<Genre> genres = genreDAO.findAll();
        genreSuggestionsList.getItems().clear();
        
        if (query == null || query.trim().isEmpty()) {
            // Mostra tutti i generi se la query è vuota
            for (Genre g : genres) {
                genreSuggestionsList.getItems().add(g.getGenreName());
            }
        } else {
            // Filtra i generi in base alla query
            for (Genre g : genres) {
                if (g.getGenreName().toLowerCase().contains(query.toLowerCase())) {
                    genreSuggestionsList.getItems().add(g.getGenreName());
                }
            }
        }
        
        genreSuggestionsList.setVisible(!genreSuggestionsList.getItems().isEmpty());
        genreSuggestionsList.setManaged(!genreSuggestionsList.getItems().isEmpty());
    }

    private void openAddGenreDialog() {
        try {
        javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
            getClass().getResource("/fxml/add-genre-dialog.fxml"));
        javafx.scene.Parent root = loader.load();

        javafx.stage.Stage dialogStage = new javafx.stage.Stage();
        dialogStage.setTitle("Aggiungi genere");
        dialogStage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
        dialogStage.setScene(new javafx.scene.Scene(root));
        dialogStage.showAndWait();

        AddGenreDialogController controller = loader.getController();
        Genre newGenre = controller.getCreatedGenre();
        if (newGenre != null) {
            genreField.setText(newGenre.getGenreName());
            suggestGenres(genreField.getText());
        }
    } catch (Exception e) {
        System.err.println("Errore nell'apertura della dialog genere: " + e.getMessage());
    }
    }

    private void saveBook() {
        try {
            String title = titleField.getText().trim();
            String yearStr = yearField.getText().trim();
            int annoPub = yearStr.isEmpty() ? 0 : Integer.parseInt(yearStr);
            String libraryName = libraryComboBox.getValue();
            String authorName = authorField.getText().trim();
            String genreName = genreField.getText().trim();

            AuthorDAO authorDAO = new AuthorDAO();
            Author author = null;
            for (Author a : authorDAO.findAll()) {
                if ((a.getAuthorName() + " " + a.getSurname()).equals(authorName)) {
                    author = a;
                    break;
                }
            }
            if (author == null) return; // Autore non trovato

            GenreDAO genreDAO = new GenreDAO();
            Genre genre = null;
            for (Genre g : genreDAO.findAll()) {
                if (g.getGenreName().equals(genreName)) {
                    genre = g;
                    break;
                }
            }
            if (genre == null) return; // Genere non trovato

            LibrariesDAO librariesDAO = new LibrariesDAO();
            Libraries library = librariesDAO.findByName(libraryName);
            if (library == null) return; // Libreria non trovata

            String ext = selectedBookFile.getName().toLowerCase().endsWith(".epub") ? "epub" : "pdf";
            // Usa una cartella dati persistente invece di resources
            String destPath = "library-data/" + ext + "/";
            java.io.File destFolder = new java.io.File(destPath);
            if (!destFolder.exists()) destFolder.mkdirs();
            java.io.File destFile = new java.io.File(destPath + selectedBookFile.getName());
            java.nio.file.Files.copy(selectedBookFile.toPath(), destFile.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);

            // Salva con percorso relativo: pdf/file.pdf o epub/file.epub
            String relativeFilePath = ext + "/" + selectedBookFile.getName();
            
            BookDAO bookDAO = new BookDAO();
            com.library.models.Book book = new com.library.models.Book(title, null, null, relativeFilePath, library.getIdLibrary());
            int bookId = bookDAO.insert(book, author.getIdAuthor(), annoPub);

            if (bookId == -1) return; // Errore nell'inserimento

            // Salva relazioni N-N
            com.library.dao.BookLibDAO bookLibDAO = new com.library.dao.BookLibDAO();
            bookLibDAO.insert(new com.library.models.BookLib(bookId, library.getIdLibrary()));

            com.library.dao.BookGenreDAO bookGenreDAO = new com.library.dao.BookGenreDAO();
            bookGenreDAO.insert(new com.library.models.BookGenre(bookId, genre.getIdGenre()));
            
            // Mostra notifica di successo
            showNotification("Libro aggiunto con successo!");
            
            // Chiudi la finestra dopo un breve delay
            javafx.application.Platform.runLater(() -> {
                try {
                    Thread.sleep(1500);
                    Stage stage = (Stage) saveBookBtn.getScene().getWindow();
                    stage.close();
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
            });
    } catch (Exception e) {
        System.err.println("Errore durante il salvataggio del libro: " + e.getMessage());
        showNotification("Errore durante il salvataggio del libro!");
    }
        
    }
    
    private void showNotification(String message) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
        alert.setTitle("Notifica");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.show();
        
        // Chiudi automaticamente dopo 2 secondi
        new Thread(() -> {
            try {
                Thread.sleep(2000);
                javafx.application.Platform.runLater(() -> alert.close());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }
}