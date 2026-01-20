package com.library.controllers;

import java.io.File;
import java.util.List;

import com.library.dao.AuthorDAO;
import com.library.dao.BookDAO;
import com.library.dao.DAOFactory;
import com.library.dao.GenreDAO;
import com.library.dao.LibAccessDAO;
import com.library.dao.LibrariesDAO;
import com.library.models.Author;
import com.library.models.Genre;
import com.library.models.LibUser;
import com.library.models.Libraries;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.CustomMenuItem;
import javafx.scene.control.Label;
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
    @FXML private TextField genreField;
    @FXML private Button addGenreBtn;
    @FXML private Button saveBookBtn;

    private File selectedBookFile;
    private LibUser currentUser;
    private ContextMenu authorContextMenu;
    private ContextMenu genreContextMenu;

    public void setUser(LibUser user) {
        this.currentUser = user;
        loadLibraries();
    }

    @FXML
    @SuppressWarnings("unused")
    private void initialize() {
        // Inizializza i ContextMenu
        authorContextMenu = new ContextMenu();
        genreContextMenu = new ContextMenu();
        
        // Property Binding: disabilita saveBookBtn se i campi obbligatori sono vuoti
        saveBookBtn.disableProperty().bind(
            titleField.textProperty().isEmpty()
            .or(authorField.textProperty().isEmpty())
            .or(genreField.textProperty().isEmpty())
            .or(libraryComboBox.valueProperty().isNull())
        );
        
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
                // Nascondi menu quando perde il focus (con un piccolo delay)
                javafx.application.Platform.runLater(() -> {
                    if (!authorContextMenu.isShowing()) {
                        authorContextMenu.hide();
                    }
                });
            }
        });
        
        // Mostra suggerimenti quando il campo genere riceve il focus
        genreField.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
            if (isNowFocused) {
                suggestGenres(genreField.getText());
            } else {
                // Nascondi menu quando perde il focus (con un piccolo delay)
                javafx.application.Platform.runLater(() -> {
                    if (!genreContextMenu.isShowing()) {
                        genreContextMenu.hide();
                    }
                });
            }
        });
    }

    private void loadLibraries() {
        LibAccessDAO accessDAO = DAOFactory.getInstance().getLibAccessDAO();
        LibrariesDAO librariesDAO = DAOFactory.getInstance().getLibrariesDAO();
        
        // Usa stream per caricare le librerie
        accessDAO.findByUserId(currentUser.getIdUser()).stream()
            .map(access -> librariesDAO.findById(access.getIdLibrary()))
            .filter(lib -> lib != null)
            .map(Libraries::getLibName)
            .forEach(libName -> libraryComboBox.getItems().add(libName));
    }

    private void selectBookFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("PDF Files", "*.pdf"),
            new FileChooser.ExtensionFilter("EPUB Files", "*.epub"),
            new FileChooser.ExtensionFilter("TEXT Files", "*.txt")
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
        AuthorDAO authorDAO = DAOFactory.getInstance().getAuthorDAO();
        List<Author> authors = authorDAO.findAll();
        authorContextMenu.getItems().clear();
        
        // Usa stream per filtrare e creare menu items
        authors.stream()
            .filter(a -> query == null || query.trim().isEmpty() || 
                         a.getAuthorName().toLowerCase().contains(query.toLowerCase()) ||
                         a.getSurname().toLowerCase().contains(query.toLowerCase()))
            .limit(10) // Limita a 10 suggerimenti per non sovraccaricare il menu
            .forEach(author -> {
                String fullName = author.getAuthorName() + " " + author.getSurname();
                Label menuLabel = new Label(fullName);
                menuLabel.setStyle("-fx-padding: 5 10; -fx-cursor: hand;");
                CustomMenuItem item = new CustomMenuItem(menuLabel, false);
                item.setOnAction(e -> {
                    authorField.setText(fullName);
                    authorContextMenu.hide();
                });
                authorContextMenu.getItems().add(item);
            });
        
        // Mostra il menu se ci sono suggerimenti
        if (!authorContextMenu.getItems().isEmpty()) {
            if (!authorContextMenu.isShowing()) {
                authorContextMenu.show(authorField, javafx.geometry.Side.BOTTOM, 0, 0);
            }
        } else {
            authorContextMenu.hide();
        }
    }

    private void openAddAuthorDialog() {
        try {
        javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
            getClass().getResource("/fxml/add-author-view.fxml"));
        javafx.scene.Parent root = loader.load();

        javafx.stage.Stage dialogStage = new javafx.stage.Stage();
        dialogStage.setTitle("Aggiungi autore");
        dialogStage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
        dialogStage.setScene(new javafx.scene.Scene(root));
        dialogStage.showAndWait();

        AddAuthorController controller = loader.getController();
        Author newAuthor = controller.getCreatedAuthor();
        if (newAuthor != null) {
            // Aggiorna suggerimenti o seleziona l'autore appena creato
            authorField.setText(newAuthor.getAuthorName() + " " + newAuthor.getSurname());
            suggestAuthors(authorField.getText());
        }
    } catch (java.io.IOException e) {
        System.err.println("Errore nell'apertura della dialog autore: " + e.getMessage());
    }
    }

    private void suggestGenres(String query) {
        GenreDAO genreDAO = DAOFactory.getInstance().getGenreDAO();
        List<Genre> genres = genreDAO.findAll();
        genreContextMenu.getItems().clear();
        
        // Usa stream per filtrare e creare menu items
        genres.stream()
            .filter(g -> query == null || query.trim().isEmpty() || 
                         g.getGenreName().toLowerCase().contains(query.toLowerCase()))
            .limit(10) // Limita a 10 suggerimenti per non sovraccaricare il menu
            .forEach(genre -> {
                String genreName = genre.getGenreName();
                Label menuLabel = new Label(genreName);
                menuLabel.setStyle("-fx-padding: 5 10; -fx-cursor: hand;");
                CustomMenuItem item = new CustomMenuItem(menuLabel, false);
                item.setOnAction(e -> {
                    genreField.setText(genreName);
                    genreContextMenu.hide();
                });
                genreContextMenu.getItems().add(item);
            });
        
        // Mostra il menu se ci sono suggerimenti
        if (!genreContextMenu.getItems().isEmpty()) {
            if (!genreContextMenu.isShowing()) {
                genreContextMenu.show(genreField, javafx.geometry.Side.BOTTOM, 0, 0);
            }
        } else {
            genreContextMenu.hide();
        }
    }

    private void openAddGenreDialog() {
        try {
        javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
            getClass().getResource("/fxml/add-genre-view.fxml"));
        javafx.scene.Parent root = loader.load();

        javafx.stage.Stage dialogStage = new javafx.stage.Stage();
        dialogStage.setTitle("Aggiungi genere");
        dialogStage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
        dialogStage.setScene(new javafx.scene.Scene(root));
        dialogStage.showAndWait();

        AddGenreController controller = loader.getController();
        Genre newGenre = controller.getCreatedGenre();
        if (newGenre != null) {
            genreField.setText(newGenre.getGenreName());
            suggestGenres(genreField.getText());
        }
    } catch (java.io.IOException e) {
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

            AuthorDAO authorDAO = DAOFactory.getInstance().getAuthorDAO();
            // Usa stream per trovare l'autore
            Author author = authorDAO.findAll().stream()
                .filter(a -> (a.getAuthorName() + " " + a.getSurname()).equals(authorName))
                .findFirst().orElse(null);
            if (author == null) return; // Autore non trovato

            GenreDAO genreDAO = DAOFactory.getInstance().getGenreDAO();
            // Usa stream per trovare il genere
            Genre genre = genreDAO.findAll().stream()
                .filter(g -> g.getGenreName().equals(genreName))
                .findFirst().orElse(null);
            if (genre == null) return; // Genere non trovato

            LibrariesDAO librariesDAO = DAOFactory.getInstance().getLibrariesDAO();
            Libraries library = librariesDAO.findByName(libraryName);
            if (library == null) return; // Libreria non trovata

            String ext = selectedBookFile.getName().toLowerCase().endsWith(".epub") ? "epub" : "pdf";
            // Usa la cartella scelta dall'utente
            String userChosenPath = currentUser.getChosenPath();
            if (userChosenPath == null || userChosenPath.trim().isEmpty()) {
                userChosenPath = System.getProperty("user.home") + java.io.File.separator + "DigitalLibrary";
            }
            String destPath = userChosenPath + java.io.File.separator + ext + java.io.File.separator;
            java.io.File destFolder = new java.io.File(destPath);
            if (!destFolder.exists()) destFolder.mkdirs();
            java.io.File destFile = new java.io.File(destPath + selectedBookFile.getName());
            java.nio.file.Files.copy(selectedBookFile.toPath(), destFile.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);

            // Salva con percorso relativo: pdf/file.pdf o epub/file.epub
            String relativeFilePath = ext + "/" + selectedBookFile.getName();
            
            BookDAO bookDAO = DAOFactory.getInstance().getBookDAO();
            // Usa Builder pattern per creare Book
            com.library.models.Book book = new com.library.models.Book.Builder()
                .title(title)
                .filePath(relativeFilePath)
                .idLibrary(library.getIdLibrary())
                .build();
            int bookId = bookDAO.insert(book, author.getIdAuthor(), annoPub);

            if (bookId == -1) return; // Errore nell'inserimento

            // Salva relazioni N-N
            com.library.dao.BookLibDAO bookLibDAO = DAOFactory.getInstance().getBookLibDAO();
            bookLibDAO.insert(new com.library.models.BookLib(bookId, library.getIdLibrary()));

            com.library.dao.BookGenreDAO bookGenreDAO = DAOFactory.getInstance().getBookGenreDAO();
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
    } catch (java.io.IOException | NumberFormatException e) {
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