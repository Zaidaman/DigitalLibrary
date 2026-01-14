package com.library.controllers;

import java.io.File;
import java.util.List;

import com.library.dao.AuthorDAO;
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
    private Author selectedAuthor;
    private Genre selectedGenre;
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
        for (Author a : authors) {
            if (a.getAuthorName().toLowerCase().contains(query.toLowerCase()) ||
                a.getSurname().toLowerCase().contains(query.toLowerCase())) {
                authorSuggestionsList.getItems().add(a.getAuthorName() + " " + a.getSurname());
            }
        }
        authorSuggestionsList.setVisible(!authorSuggestionsList.getItems().isEmpty());
        authorSuggestionsList.setManaged(!authorSuggestionsList.getItems().isEmpty());
        
    }

    private void openAddAuthorDialog() {
        // Implementation for opening a dialog to add a new author
    }

    private void suggestGenres(String query) {
        GenreDAO genreDAO = new GenreDAO();
        List<Genre> genres = genreDAO.findAll();
        genreSuggestionsList.getItems().clear();
        for (Genre g : genres) {
            if (g.getGenreName().toLowerCase().contains(query.toLowerCase())) {
                genreSuggestionsList.getItems().add(g.getGenreName());
            }
        }
        genreSuggestionsList.setVisible(!genreSuggestionsList.getItems().isEmpty());
        genreSuggestionsList.setManaged(!genreSuggestionsList.getItems().isEmpty());
    }

    private void openAddGenreDialog() {
        // Implementation for opening a dialog to add a new genre
    }

    private void saveBook() {
        // Implementation for saving the new book to the database
    }
}