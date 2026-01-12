package com.library.controllers;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import com.library.dao.LibAccessDAO;
import com.library.dao.LibrariesDAO;
import com.library.dao.AuthorDAO;
import com.library.dao.GenreDAO;
import com.library.models.LibAccess;
import com.library.models.Libraries;
import com.library.models.Author;
import com.library.models.Genre;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Window;

public class UploadBookController implements Initializable {
    @FXML
    private ComboBox<String> libraryComboBox;
    @FXML
    private TextField titleField;
    @FXML
    private ComboBox<Author> authorComboBox;
    @FXML
    private ComboBox<Genre> genreComboBox;
    @FXML
    private TextField fileField;
    @FXML
    private Button browseBtn;
    @FXML
    public Button uploadBtn;
    public Button cancelBtn;

    private File selectedFile;
    private int currentUserId;
    private ObservableList<Author> authors = FXCollections.observableArrayList();
    private ObservableList<Genre> genres = FXCollections.observableArrayList();

    public void setCurrentUserId(int userId) {
        this.currentUserId = userId;
        loadLibrariesForUser();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        browseBtn.setOnAction(e -> onBrowse());
        // Popola autori e generi
        AuthorDAO authorDAO = new AuthorDAO();
        authors.setAll(authorDAO.findAll());
        authorComboBox.setItems(authors);
        authorComboBox.setEditable(true);
        authorComboBox.setPromptText("Seleziona autore");
        authorComboBox.setConverter(new javafx.util.StringConverter<Author>() {
            @Override public String toString(Author a) { return a == null ? "" : a.getAuthorName(); }
            @Override public Author fromString(String s) { return authors.stream().filter(a -> a.getAuthorName().equals(s)).findFirst().orElse(null); }
        });

        GenreDAO genreDAO = new GenreDAO();
        genres.setAll(genreDAO.findAll());
        genreComboBox.setItems(genres);
        genreComboBox.setEditable(true);
        genreComboBox.setPromptText("Seleziona genere");
        genreComboBox.setConverter(new javafx.util.StringConverter<Genre>() {
            @Override public String toString(Genre g) { return g == null ? "" : g.getGenreName(); }
            @Override public Genre fromString(String s) { return genres.stream().filter(g -> g.getGenreName().equals(s)).findFirst().orElse(null); }
        });
        // uploadBtn/cancelBtn handler da collegare dal chiamante
    }

    private void loadLibrariesForUser() {
        LibAccessDAO accessDAO = new LibAccessDAO();
        LibrariesDAO librariesDAO = new LibrariesDAO();
        List<LibAccess> accesses = accessDAO.findByUserId(currentUserId);
        ObservableList<String> libNames = FXCollections.observableArrayList();
        for (LibAccess access : accesses) {
            Libraries lib = librariesDAO.findById(access.getIdLibrary());
            if (lib != null) libNames.add(lib.getLibName());
        }
        libraryComboBox.setItems(libNames);
        if (!libNames.isEmpty()) libraryComboBox.getSelectionModel().selectFirst();
    }

    private void onBrowse() {
        Window window = browseBtn.getScene().getWindow();
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Seleziona file libro");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("PDF/EPUB", "*.pdf", "*.epub")
        );
        File file = fileChooser.showOpenDialog(window);
        if (file != null) {
            selectedFile = file;
            fileField.setText(file.getAbsolutePath());
            // Imposta il nome file (senza estensione) come titolo se vuoto
            if (titleField.getText() == null || titleField.getText().isEmpty()) {
                String name = file.getName();
                int dot = name.lastIndexOf('.');
                if (dot > 0) name = name.substring(0, dot);
                titleField.setText(name);
            }
        }
    }

    public File getSelectedFile() {
        return selectedFile;
    }
    public String getSelectedLibrary() {
        return libraryComboBox.getValue();
    }
    public String getTitle() {
        return titleField.getText();
    }
    public Author getSelectedAuthor() {
        return authorComboBox.getValue();
    }
    public Genre getSelectedGenre() {
        return genreComboBox.getValue();
    }
}
