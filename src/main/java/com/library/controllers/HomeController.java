package com.library.controllers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.library.dao.BookDAO;
import com.library.dao.BookLibDAO;
import com.library.dao.DAOFactory;
import com.library.dao.LibAccessDAO;
import com.library.dao.LibUserDAO;
import com.library.dao.LibrariesDAO;
import com.library.models.Book;
import com.library.models.LibAccess;
import com.library.models.LibUser;
import com.library.models.Libraries;
import com.library.observers.LibraryObserver;
import com.library.observers.LibrarySubject;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class HomeController implements LibraryObserver {

    @FXML
    private ListView<String> libraryList;

    @FXML
    private StackPane contentArea;
    
    @FXML
    private HBox filterBar;

    @FXML
    private MenuItem addLibraryMenuItem;

    @FXML
    private MenuItem shareLibraryMenuItem;

    @FXML
    private MenuItem deleteLibraryMenuItem;

    @FXML
    private MenuItem addBookMenuItem;

    @FXML
    private MenuItem addExistingBookMenuItem;

    @FXML
    private MenuItem editBookMenuItem;

    @FXML
    private MenuItem removeBookFromLibraryMenuItem;

    @FXML
    private MenuItem editUserMenuItem;

    @FXML
    private MenuItem logoutMenuItem;

    @FXML
    private MenuItem addAuthorMenuItem;

    @FXML
    private MenuItem addGenreMenuItem;

    @FXML
    private ComboBox<String> sortCombo;
    
    @FXML
    private MenuButton filterMenuBtn;
    
    @FXML
    private CheckMenuItem pdfFilterItem;
    
    @FXML
    private CheckMenuItem epubFilterItem;

    @FXML
    private CheckMenuItem txtFilterItem;

    @FXML
    private Button clearFiltersBtn;

    private LibUser currentUser;
    private final LibrarySubject librarySubject = new LibrarySubject();

    private List<Book> currentLibraryBooks = new ArrayList<>();

    private String selectedSort = "NONE";
    private String activeFilterType = "NONE";

    @FXML
    public void initialize() {
        // L'inizializzazione vera avviene dopo che l'utente è stato settato
        // (tramite setUser)
        setupAddLibraryMenuItem();
        setupShareLibraryMenuItem();
        setupDeleteLibraryMenuItem();
        setupAddBookMenuItem();
        setupAddExistingBookMenuItem();
        setupEditBookMenuItem();
        setupRemoveBookFromLibraryMenuItem();
        setupAddAuthorMenuItem();
        setupAddGenreMenuItem();
        setupEditUserMenuItem();
        setupLogoutMenuItem();
        showDefaultMessage();
        setupSort();
        setupFilter();
        setupClearButton();
        
        // Registra questo controller come observer
        librarySubject.addObserver(this);
    }

    private void setupAddBookMenuItem() {
        if (addBookMenuItem != null) {
            addBookMenuItem.setOnAction(e -> {
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/add-book-view.fxml"));
                    Parent root = loader.load();
                    
                    // Passa l'utente corrente al controller
                    AddBookController controller = loader.getController();
                    controller.setUser(currentUser);
                    
                    Stage stage = new Stage();
                    stage.setTitle("Aggiungi un nuovo libro");
                    stage.initModality(Modality.APPLICATION_MODAL);
                    stage.setScene(new Scene(root));
                    stage.showAndWait(); // Aspetta che la finestra si chiuda
                    
                    // Dopo la chiusura, ricarica i libri della libreria selezionata
                    String selectedLibrary = libraryList.getSelectionModel().getSelectedItem();
                    if (selectedLibrary != null) {
                        loadBooksForLibrary(selectedLibrary);
                    }
                } catch (IOException ex) {
                    System.err.println("Errore durante il caricamento della finestra: " + ex.getMessage());
                }
            });
        }
    }

    private void setupAddExistingBookMenuItem() {
        if (addExistingBookMenuItem != null) {
            addExistingBookMenuItem.setOnAction(e -> {
                String selectedLibrary = libraryList.getSelectionModel().getSelectedItem();
                if (selectedLibrary == null) {
                    showAlert("Nessuna libreria selezionata", "Seleziona una libreria prima di aggiungere un libro.");
                    return;
                }
                
                LibrariesDAO libDAO = new LibrariesDAO();
                Libraries library = libDAO.findByName(selectedLibrary);
                if (library == null) return;
                
                // Ottieni tutti i libri dal DB
                BookDAO bookDAO = DAOFactory.getInstance().getBookDAO();
                List<Book> allBooks = bookDAO.findAll();
                
                // Ottieni i libri già nella libreria
                List<Book> booksInLibrary = bookDAO.findByLibraryId(library.getIdLibrary());
                List<String> titlesInLibrary = booksInLibrary.stream()
                    .map(Book::getTitle)
                    .collect(Collectors.toList());
                
                // Filtra i libri disponibili usando stream
                List<String> availableBooks = allBooks.stream()
                    .map(Book::getTitle)
                    .filter(title -> !titlesInLibrary.contains(title))
                    .collect(Collectors.toList());
                
                if (availableBooks.isEmpty()) {
                    showAlert("Nessun libro disponibile", "Tutti i libri sono già presenti in questa libreria.");
                    return;
                }
                
                // Dialog per selezionare il libro
                javafx.scene.control.ChoiceDialog<String> dialog = 
                    new javafx.scene.control.ChoiceDialog<>(availableBooks.get(0), availableBooks);
                dialog.setTitle("Aggiungi Libro alla Libreria");
                dialog.setHeaderText("Aggiungi un libro esistente a \"" + selectedLibrary + "\"");
                dialog.setContentText("Seleziona libro:");
                
                Optional<String> result = dialog.showAndWait();
                result.ifPresent(bookTitle -> {
                    Book book = bookDAO.findByTitle(bookTitle);
                    if (book != null) {
                        // Trova l'ID del libro
                        int idBook = bookDAO.findIdByTitle(bookTitle);
                        if (idBook != -1) {
                            BookLibDAO bookLibDAO = DAOFactory.getInstance().getBookLibDAO();
                            bookLibDAO.insert(new com.library.models.BookLib(idBook, library.getIdLibrary()));
                            showAlert("Successo", "Libro aggiunto alla libreria!");
                            // Ricarica i libri della libreria
                            loadBooksForLibrary(selectedLibrary);
                        }
                    }
                });
            });
        }
    }

    private void setupEditBookMenuItem() {
        if (editBookMenuItem != null) {
            editBookMenuItem.setOnAction(e -> {
                String selectedLibrary = libraryList.getSelectionModel().getSelectedItem();
                if (selectedLibrary == null) {
                    showAlert("Nessuna libreria selezionata", "Seleziona una libreria prima di modificare un libro.");
                    return;
                }
                
                LibrariesDAO libDAO = DAOFactory.getInstance().getLibrariesDAO();
                Libraries library = libDAO.findByName(selectedLibrary);
                if (library == null) return;
                
                // Ottieni i libri nella libreria
                BookDAO bookDAO = DAOFactory.getInstance().getBookDAO();
                List<Book> booksInLibrary = bookDAO.findByLibraryId(library.getIdLibrary());
                
                if (booksInLibrary.isEmpty()) {
                    showAlert("Nessun libro", "Non ci sono libri in questa libreria.");
                    return;
                }
                
                List<String> bookTitles = booksInLibrary.stream()
                    .map(Book::getTitle)
                    .collect(Collectors.toList());
                
                // Dialog per selezionare il libro da modificare
                javafx.scene.control.ChoiceDialog<String> selectDialog = 
                    new javafx.scene.control.ChoiceDialog<>(bookTitles.get(0), bookTitles);
                selectDialog.setTitle("Modifica Libro");
                selectDialog.setHeaderText("Seleziona il libro da modificare");
                selectDialog.setContentText("Libro:");
                
                Optional<String> selectResult = selectDialog.showAndWait();
                selectResult.ifPresent(bookTitle -> {
                    int idBook = bookDAO.findIdByTitle(bookTitle);
                    if (idBook == -1) return;
                    
                    // Ottieni i dati completi del libro
                    var bookData = bookDAO.findBookDetailsById(idBook);
                    if (bookData == null) return;
                    
                    // Dialog per modificare i dati
                    javafx.scene.control.Dialog<java.util.Map<String, String>> dialog = 
                        new javafx.scene.control.Dialog<>();
                    dialog.setTitle("Modifica Libro");
                    dialog.setHeaderText("Modifica i dati del libro\n(Lascia vuoto per non modificare)");
                    
                    javafx.scene.control.ButtonType saveButtonType = 
                        new javafx.scene.control.ButtonType("Salva", javafx.scene.control.ButtonBar.ButtonData.OK_DONE);
                    dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, javafx.scene.control.ButtonType.CANCEL);
                    
                    // Campi input
                    javafx.scene.layout.GridPane grid = new javafx.scene.layout.GridPane();
                    grid.setHgap(10);
                    grid.setVgap(10);
                    grid.setPadding(new javafx.geometry.Insets(20, 150, 10, 10));
                    
                    javafx.scene.control.TextField titleField = new javafx.scene.control.TextField();
                    titleField.setPromptText("Titolo attuale: " + bookData.get("title"));
                    
                    javafx.scene.control.TextField yearField = new javafx.scene.control.TextField();
                    yearField.setPromptText("Anno attuale: " + bookData.get("year"));
                    
                    // ComboBox per autore
                    com.library.dao.AuthorDAO authorDAO = new com.library.dao.AuthorDAO();
                    List<com.library.models.Author> allAuthors = authorDAO.findAll();
                    javafx.scene.control.ComboBox<String> authorCombo = new javafx.scene.control.ComboBox<>();
                    authorCombo.setPromptText("Autore attuale: " + bookData.get("author"));
                    allAuthors.forEach(author -> 
                        authorCombo.getItems().add(author.getAuthorName() + " " + 
                            (author.getMidName() != null ? author.getMidName() + " " : "") + 
                            author.getSurname()));
                    
                    // ComboBox per genere
                    com.library.dao.GenreDAO genreDAO = new com.library.dao.GenreDAO();
                    List<com.library.models.Genre> allGenres = genreDAO.findAll();
                    javafx.scene.control.ComboBox<String> genreCombo = new javafx.scene.control.ComboBox<>();
                    genreCombo.setPromptText("Genere attuale: " + bookData.get("genre"));
                    allGenres.forEach(genre -> genreCombo.getItems().add(genre.getGenreName()));
                    
                    grid.add(new javafx.scene.control.Label("Nuovo Titolo:"), 0, 0);
                    grid.add(titleField, 1, 0);
                    grid.add(new javafx.scene.control.Label("Nuovo Anno:"), 0, 1);
                    grid.add(yearField, 1, 1);
                    grid.add(new javafx.scene.control.Label("Nuovo Autore:"), 0, 2);
                    grid.add(authorCombo, 1, 2);
                    grid.add(new javafx.scene.control.Label("Nuovo Genere:"), 0, 3);
                    grid.add(genreCombo, 1, 3);
                    
                    dialog.getDialogPane().setContent(grid);
                    
                    javafx.application.Platform.runLater(() -> titleField.requestFocus());
                    
                    dialog.setResultConverter(dialogButton -> {
                        if (dialogButton == saveButtonType) {
                            java.util.Map<String, String> result = new java.util.HashMap<>();
                            result.put("title", titleField.getText());
                            result.put("year", yearField.getText());
                            result.put("author", authorCombo.getValue());
                            result.put("genre", genreCombo.getValue());
                            return result;
                        }
                        return null;
                    });
                    
                    Optional<java.util.Map<String, String>> result = dialog.showAndWait();
                    
                    result.ifPresent(data -> {
                        String newTitle = data.get("title").trim();
                        String newYear = data.get("year").trim();
                        String newAuthor = data.get("author");
                        String newGenre = data.get("genre");
                        
                        // Se tutti i campi sono vuoti, non fare nulla
                        if (newTitle.isEmpty() && newYear.isEmpty() && newAuthor == null && newGenre == null) {
                            showAlert("Nessuna modifica", "Non hai modificato nessun dato.");
                            return;
                        }
                        
                        // Verifica che il titolo non esista già (se modificato)
                        if (!newTitle.isEmpty() && !newTitle.equals(bookData.get("title"))) {
                            Book existingBook = bookDAO.findByTitle(newTitle);
                            if (existingBook != null) {
                                showAlert("Titolo esistente", "Un libro con questo titolo esiste già.");
                                return;
                            }
                        }
                        
                        // Trova IdAuthor se l'autore è stato modificato
                        Integer newIdAuthor = null;
                        if (newAuthor != null) {
                            for (com.library.models.Author author : allAuthors) {
                                String fullName = author.getAuthorName() + " " + 
                                    (author.getMidName() != null ? author.getMidName() + " " : "") + 
                                    author.getSurname();
                                if (fullName.equals(newAuthor)) {
                                    newIdAuthor = author.getIdAuthor();
                                    break;
                                }
                            }
                        }
                        
                        // Trova IdGenre se il genere è stato modificato
                        Integer newIdGenre = null;
                        if (newGenre != null) {
                            for (com.library.models.Genre genre : allGenres) {
                                if (genre.getGenreName().equals(newGenre)) {
                                    newIdGenre = genre.getIdGenre();
                                    break;
                                }
                            }
                        }
                        
                        // Valida anno se fornito
                        Integer newYearInt = null;
                        if (!newYear.isEmpty()) {
                            try {
                                newYearInt = Integer.valueOf(newYear);
                                if (newYearInt < 0 || newYearInt > java.time.Year.now().getValue()) {
                                    showAlert("Anno non valido", "L'anno deve essere compreso tra 0 e " + java.time.Year.now().getValue());
                                    return;
                                }
                            } catch (NumberFormatException ex) {
                                showAlert("Anno non valido", "Inserisci un numero valido per l'anno.");
                                return;
                            }
                        }
                        
                        // Aggiorna il libro
                        boolean updated = bookDAO.updateBook(
                            idBook,
                            newTitle.isEmpty() ? null : newTitle,
                            newIdAuthor,
                            newYearInt
                        );
                        
                        // Aggiorna il genere se specificato
                        if (newIdGenre != null) {
                            com.library.dao.BookGenreDAO bookGenreDAO = new com.library.dao.BookGenreDAO();
                            bookGenreDAO.updateGenreForBook(idBook, newIdGenre);
                        }
                        
                        if (updated) {
                            showAlert("Successo", "I dati del libro sono stati aggiornati con successo!");
                            loadBooksForLibrary(selectedLibrary); // Ricarica la lista libri
                        } else {
                            showAlert("Errore", "Si è verificato un errore durante l'aggiornamento.");
                        }
                    });
                });
            });
        }
    }

    private void setupRemoveBookFromLibraryMenuItem() {
        if (removeBookFromLibraryMenuItem != null) {
            removeBookFromLibraryMenuItem.setOnAction(e -> {
                String selectedLibrary = libraryList.getSelectionModel().getSelectedItem();
                if (selectedLibrary == null) {
                    showAlert("Nessuna libreria selezionata", "Seleziona una libreria prima di rimuovere un libro.");
                    return;
                }
                
                LibrariesDAO libDAO = DAOFactory.getInstance().getLibrariesDAO();
                Libraries library = libDAO.findByName(selectedLibrary);
                if (library == null) return;
                
                // Ottieni i libri nella libreria
                BookDAO bookDAO = DAOFactory.getInstance().getBookDAO();
                List<Book> booksInLibrary = bookDAO.findByLibraryId(library.getIdLibrary());
                
                if (booksInLibrary.isEmpty()) {
                    showAlert("Nessun libro", "Non ci sono libri in questa libreria.");
                    return;
                }
                
                // Usa stream per ottenere i titoli
                List<String> bookTitles = booksInLibrary.stream()
                    .map(Book::getTitle)
                    .collect(Collectors.toList());
                
                // Dialog per selezionare il libro da rimuovere
                javafx.scene.control.ChoiceDialog<String> dialog = 
                    new javafx.scene.control.ChoiceDialog<>(bookTitles.get(0), bookTitles);
                dialog.setTitle("Rimuovi Libro dalla Libreria");
                dialog.setHeaderText("Rimuovi un libro da \"" + selectedLibrary + "\"");
                dialog.setContentText("Seleziona libro:");
                
                Optional<String> result = dialog.showAndWait();
                result.ifPresent(bookTitle -> {
                    // Conferma rimozione
                    javafx.scene.control.Alert confirmAlert = new javafx.scene.control.Alert(
                        javafx.scene.control.Alert.AlertType.CONFIRMATION);
                    confirmAlert.setTitle("Conferma Rimozione");
                    confirmAlert.setHeaderText("Rimuovere \"" + bookTitle + "\"?");
                    confirmAlert.setContentText("Il libro verrà rimosso solo da questa libreria, non dal database.");
                    
                    Optional<javafx.scene.control.ButtonType> confirmResult = confirmAlert.showAndWait();
                    if (confirmResult.isPresent() && confirmResult.get() == javafx.scene.control.ButtonType.OK) {
                        int idBook = bookDAO.findIdByTitle(bookTitle);
                        if (idBook != -1) {
                            BookLibDAO bookLibDAO = DAOFactory.getInstance().getBookLibDAO();
                            bookLibDAO.deleteByBookAndLibrary(idBook, library.getIdLibrary());
                            showAlert("Successo", "Libro rimosso dalla libreria!");
                            // Ricarica i libri della libreria
                            loadBooksForLibrary(selectedLibrary);
                        }
                    }
                });
            });
        }
    }

    private void setupAddAuthorMenuItem() {
        if (addAuthorMenuItem != null) {
            addAuthorMenuItem.setOnAction(e -> {
                try {
                    FXMLLoader loader = new FXMLLoader(
                        getClass().getResource("/fxml/add-author-view.fxml"));
                    Parent root = loader.load();

                    Stage stage = new Stage();
                    stage.setTitle("Aggiungi Autore");
                    stage.initModality(Modality.APPLICATION_MODAL);
                    stage.setScene(new Scene(root));
                    stage.showAndWait();
                } catch (IOException ex) {
                    showAlert("Errore", "Impossibile aprire la finestra per aggiungere un autore: " + ex.getMessage());
                }
            });
        }
    }

    private void setupAddGenreMenuItem() {
        if (addGenreMenuItem != null) {
            addGenreMenuItem.setOnAction(e -> {
                try {
                    FXMLLoader loader = new FXMLLoader(
                        getClass().getResource("/fxml/add-genre-view.fxml"));
                    Parent root = loader.load();

                    Stage stage = new Stage();
                    stage.setTitle("Aggiungi Genere");
                    stage.initModality(Modality.APPLICATION_MODAL);
                    stage.setScene(new Scene(root));
                    stage.showAndWait();
                } catch (IOException ex) {
                    showAlert("Errore", "Impossibile aprire la finestra per aggiungere un genere: " + ex.getMessage());
                }
            });
        }
    }

    private void setupEditUserMenuItem() {
        if (editUserMenuItem != null) {
            editUserMenuItem.setOnAction(e -> {
                if (currentUser == null) return;
                
                // Dialog personalizzata per modificare username e password
                javafx.scene.control.Dialog<javafx.util.Pair<String, String>> dialog = 
                    new javafx.scene.control.Dialog<>();
                dialog.setTitle("Modifica Dati Utente");
                dialog.setHeaderText("Modifica username e/o password\n(Lascia vuoto per non modificare)");
                
                // Pulsanti
                javafx.scene.control.ButtonType saveButtonType = 
                    new javafx.scene.control.ButtonType("Salva", javafx.scene.control.ButtonBar.ButtonData.OK_DONE);
                dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, javafx.scene.control.ButtonType.CANCEL);
                
                // Campi input
                javafx.scene.layout.GridPane grid = new javafx.scene.layout.GridPane();
                grid.setHgap(10);
                grid.setVgap(10);
                grid.setPadding(new javafx.geometry.Insets(20, 150, 10, 10));
                
                javafx.scene.control.TextField usernameField = new javafx.scene.control.TextField();
                usernameField.setPromptText("Username attuale: " + currentUser.getUsername());
                javafx.scene.control.PasswordField passwordField = new javafx.scene.control.PasswordField();
                passwordField.setPromptText("Nuova password");
                
                grid.add(new javafx.scene.control.Label("Nuovo Username:"), 0, 0);
                grid.add(usernameField, 1, 0);
                grid.add(new javafx.scene.control.Label("Nuova Password:"), 0, 1);
                grid.add(passwordField, 1, 1);
                
                dialog.getDialogPane().setContent(grid);
                
                // Richiedi focus sul campo username
                javafx.application.Platform.runLater(() -> usernameField.requestFocus());
                
                // Converti il risultato quando viene premuto il pulsante Salva
                dialog.setResultConverter(dialogButton -> {
                    if (dialogButton == saveButtonType) {
                        return new javafx.util.Pair<>(usernameField.getText(), passwordField.getText());
                    }
                    return null;
                });
                
                Optional<javafx.util.Pair<String, String>> result = dialog.showAndWait();
                
                result.ifPresent(pair -> {
                    String newUsername = pair.getKey().trim();
                    String newPassword = pair.getValue().trim();
                    
                    // Se entrambi sono vuoti, non fare nulla
                    if (newUsername.isEmpty() && newPassword.isEmpty()) {
                        showAlert("Nessuna modifica", "Non hai modificato nessun dato.");
                        return;
                    }
                    
                    LibUserDAO userDAO = DAOFactory.getInstance().getLibUserDAO();
                    
                    // Se l'username è cambiato, verifica che non esista già
                    if (!newUsername.isEmpty() && !newUsername.equals(currentUser.getUsername())) {
                        LibUser existingUser = userDAO.findAll().stream()
                            .filter(u -> u.getUsername().equals(newUsername))
                            .findFirst()
                            .orElse(null);
                        
                        if (existingUser != null) {
                            showAlert("Username già esistente", "L'username \"" + newUsername + "\" è già in uso.");
                            return;
                        }
                    }
                    
                    // Aggiorna i dati
                    boolean updated = userDAO.update(
                        currentUser.getIdUser(),
                        newUsername.isEmpty() ? null : newUsername,
                        newPassword.isEmpty() ? null : newPassword
                    );
                    
                    if (updated) {
                        // Ricarica l'utente dal database con i dati aggiornati
                        currentUser = userDAO.findById(currentUser.getIdUser());
                        showAlert("Successo", "I tuoi dati sono stati aggiornati con successo!");
                    } else {
                        showAlert("Errore", "Si è verificato un errore durante l'aggiornamento dei dati.");
                    }
                });
            });
        }
    }

    private void setupLogoutMenuItem() {
        if (logoutMenuItem != null) {
            logoutMenuItem.setOnAction(e -> {
                try {
                    // Ottieni lo stage corrente
                    Stage currentStage = (Stage) contentArea.getScene().getWindow();
                    
                    // Carica la schermata di login
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login-view.fxml"));
                    Scene loginScene = new Scene(loader.load(), 400, 350);
                    
                    // Imposta la nuova scena
                    currentStage.setScene(loginScene);
                    currentStage.setTitle("Login - Digital Library");
                    
                    // Reset dell'utente corrente
                    currentUser = null;
                } catch (IOException ex) {
                    System.err.println("Errore durante il logout: " + ex.getMessage());
                }
            });
        }
    }

    private void setupAddLibraryMenuItem() {
        if (addLibraryMenuItem != null) {
            addLibraryMenuItem.setOnAction(e -> {
                if (currentUser == null) return;
                TextInputDialog dialog = new TextInputDialog();
                dialog.setTitle("Nuova Libreria");
                dialog.setHeaderText("Crea una nuova libreria");
                dialog.setContentText("Nome della libreria:");
                Optional<String> result = dialog.showAndWait();
                result.ifPresent(libName -> {
                    if (libName.trim().isEmpty()) return;
                    LibrariesDAO librariesDAO = new LibrariesDAO();
                    
                    // Verifica se la libreria esiste già
                    Libraries existingLib = librariesDAO.findByName(libName.trim());
                    
                    if (existingLib != null) {
                        // La libreria esiste già, verifica se l'utente ha già accesso
                        LibAccessDAO accessDAO = new LibAccessDAO();
                        boolean hasAccess = accessDAO.findByUserId(currentUser.getIdUser())
                            .stream()
                            .anyMatch(a -> a.getIdLibrary() == existingLib.getIdLibrary());
                        
                        if (hasAccess) {
                            showAlert("Libreria già presente", "Hai già accesso a una libreria con questo nome.");
                        } else {
                            showAlert("Libreria già esistente", "Una libreria con questo nome esiste già. Scegli un nome diverso.");
                        }
                    } else {
                        // La libreria non esiste, creala
                        Libraries newLib = new Libraries(0, libName.trim());
                        int generatedId = librariesDAO.insert(newLib);
                        
                        if (generatedId != -1) {
                            // Associa all'utente corrente usando l'ID generato
                            LibAccessDAO accessDAO = new LibAccessDAO();
                            accessDAO.insert(new LibAccess(currentUser.getIdUser(), generatedId));
                            
                            // Notifica gli observer
                            Libraries createdLib = librariesDAO.findById(generatedId);
                            if (createdLib != null) {
                                librarySubject.notifyLibraryAdded(createdLib);
                            }
                            
                            showAlert("Successo", "Libreria creata con successo!");
                        } else {
                            showAlert("Errore", "Errore durante la creazione della libreria.");
                        }
                    }
                });
            });
        }
    }

    private void setupShareLibraryMenuItem() {
        if (shareLibraryMenuItem != null) {
            shareLibraryMenuItem.setOnAction(e -> {
                String selectedLibrary = libraryList.getSelectionModel().getSelectedItem();
                if (selectedLibrary == null) {
                    showAlert("Nessuna libreria selezionata", "Seleziona una libreria da condividere.");
                    return;
                }
                
                LibrariesDAO libDAO = DAOFactory.getInstance().getLibrariesDAO();
                Libraries library = libDAO.findByName(selectedLibrary);
                if (library == null) return;
                
                // Ottieni lista di tutti gli utenti tranne quello corrente usando stream
                LibUserDAO userDAO = DAOFactory.getInstance().getLibUserDAO();
                List<LibUser> allUsers = userDAO.findAll();
                List<String> usernames = allUsers.stream()
                    .filter(user -> user.getIdUser() != currentUser.getIdUser())
                    .map(LibUser::getUsername)
                    .collect(Collectors.toList());
                
                if (usernames.isEmpty()) {
                    showAlert("Nessun utente disponibile", "Non ci sono altri utenti con cui condividere.");
                    return;
                }
                
                // Dialog per selezionare l'utente
                javafx.scene.control.ChoiceDialog<String> dialog = 
                    new javafx.scene.control.ChoiceDialog<>(usernames.get(0), usernames);
                dialog.setTitle("Condividi Libreria");
                dialog.setHeaderText("Condividi \"" + selectedLibrary + "\" con:");
                dialog.setContentText("Seleziona utente:");
                
                Optional<String> result = dialog.showAndWait();
                result.ifPresent(username -> {
                    LibUser targetUser = allUsers.stream()
                        .filter(u -> u.getUsername().equals(username))
                        .findFirst().orElse(null);
                    
                    if (targetUser != null) {
                        LibAccessDAO accessDAO = DAOFactory.getInstance().getLibAccessDAO();
                        // Verifica se l'utente ha già accesso
                        boolean alreadyHasAccess = accessDAO.findByUserId(targetUser.getIdUser())
                            .stream().anyMatch(a -> a.getIdLibrary() == library.getIdLibrary());
                        
                        if (alreadyHasAccess) {
                            showAlert("Già condivisa", "L'utente ha già accesso a questa libreria.");
                        } else {
                            accessDAO.insert(new LibAccess(targetUser.getIdUser(), library.getIdLibrary()));
                            
                            // Notifica gli observer
                            librarySubject.notifyLibraryShared(library, username);
                            
                            showAlert("Successo", "Libreria condivisa con " + username + "!");
                        }
                    }
                });
            });
        }
    }

    private void setupDeleteLibraryMenuItem() {
        if (deleteLibraryMenuItem != null) {
            deleteLibraryMenuItem.setOnAction(e -> {
                String selectedLibrary = libraryList.getSelectionModel().getSelectedItem();
                if (selectedLibrary == null) {
                    showAlert("Nessuna libreria selezionata", "Seleziona una libreria da eliminare.");
                    return;
                }
                
                LibrariesDAO libDAO = new LibrariesDAO();
                Libraries library = libDAO.findByName(selectedLibrary);
                if (library == null) return;
                
                // Conferma eliminazione
                javafx.scene.control.Alert confirmAlert = new javafx.scene.control.Alert(
                    javafx.scene.control.Alert.AlertType.CONFIRMATION);
                confirmAlert.setTitle("Conferma Eliminazione");
                confirmAlert.setHeaderText("Eliminare \"" + selectedLibrary + "\"?");
                confirmAlert.setContentText("Questa azione rimuoverà il tuo accesso alla libreria.");
                
                Optional<javafx.scene.control.ButtonType> result = confirmAlert.showAndWait();
                if (result.isPresent() && result.get() == javafx.scene.control.ButtonType.OK) {
                    LibAccessDAO accessDAO = DAOFactory.getInstance().getLibAccessDAO();
                    
                    // Rimuovi l'accesso dell'utente corrente
                    accessDAO.deleteByUserAndLibrary(currentUser.getIdUser(), library.getIdLibrary());
                    
                    // Verifica se altri utenti hanno ancora accesso
                    List<LibAccess> remainingAccess = accessDAO.findByLibraryId(library.getIdLibrary());
                    
                    if (remainingAccess.isEmpty()) {
                        // Nessun altro ha accesso, elimina completamente
                        BookLibDAO bookLibDAO = DAOFactory.getInstance().getBookLibDAO();
                        bookLibDAO.deleteByLibraryId(library.getIdLibrary());
                        libDAO.delete(library.getIdLibrary());
                        
                        // Notifica gli observer
                        librarySubject.notifyLibraryDeleted(library);
                        
                        showAlert("Successo", "Libreria eliminata completamente.");
                    } else {
                        showAlert("Successo", "Il tuo accesso alla libreria è stato rimosso.");
                    }
                    
                    // Aggiorna la lista
                    loadLibraries();
                    showDefaultMessage();
                    filterBar.setVisible(false);
                    filterBar.setManaged(false);
                }
            });
        }
    }

   private void setupSort() {
        sortCombo.getItems().addAll(
                "Nessuno",
                "Titolo A → Z",
                "Titolo Z → A"
        );

        sortCombo.getSelectionModel().selectFirst();

        sortCombo.setOnAction(e -> {
            selectedSort = sortCombo.getValue();
            applyFiltersAndSort();
        });
    }

    private void setupFilter() {
        pdfFilterItem.setOnAction(e -> {

            if (pdfFilterItem.isSelected()) {
                epubFilterItem.setSelected(false);
                txtFilterItem.setSelected(false);
                activeFilterType = "PDF";
            } else {
                activeFilterType = "NONE";
            }
                
            applyFiltersAndSort();
        });

        epubFilterItem.setOnAction(e -> {
            if (epubFilterItem.isSelected()) {

                pdfFilterItem.setSelected(false);
                txtFilterItem.setSelected(false);
                activeFilterType = "EPUB";

            } else {
                activeFilterType = "NONE";
            }

            applyFiltersAndSort();
        });

        txtFilterItem.setOnAction(e -> {
            if (txtFilterItem.isSelected()) {
                pdfFilterItem.setSelected(false);
                epubFilterItem.setSelected(false);
                activeFilterType = "TXT";
            } else {
                activeFilterType = "NONE";
            }
            applyFiltersAndSort();
        });
    }

    private void setupClearButton() {
        clearFiltersBtn.setOnAction(e -> {
                selectedSort = "NONE";
                activeFilterType = "NONE";

                sortCombo.getSelectionModel().selectFirst();

                pdfFilterItem.setSelected(false);
                epubFilterItem.setSelected(false);
                txtFilterItem.setSelected(false);

                filterMenuBtn.setText("Filtra");

                refreshBooksList(currentLibraryBooks);
            });
        }

        private void applyFiltersAndSort() {

        List<Book> result = new ArrayList<>(currentLibraryBooks);

        // -------- FILTRI --------
        result.removeIf(book -> {
            String path = book.getFilePath().toLowerCase();

            if ("PDF".equals(activeFilterType)) {
                filterMenuBtn.setText("PDF");
                return !path.endsWith(".pdf");
            }

            if ("EPUB".equals(activeFilterType)) {
                filterMenuBtn.setText("EPUB");
                return !path.endsWith(".epub");
            }

            if ("TXT".equals(activeFilterType)) {
                filterMenuBtn.setText("TXT");
                return !path.endsWith(".txt");
            }

            return false;
        });

        // -------- SORT --------
        if ("Titolo A → Z".equals(selectedSort)) {
            result.sort((a, b) -> a.getTitle().compareToIgnoreCase(b.getTitle()));
        }

        if ("Titolo Z → A".equals(selectedSort)) {
            result.sort((a, b) -> b.getTitle().compareToIgnoreCase(a.getTitle()));
        }

        refreshBooksList(result);
    }

    private void showAlert(String title, String content) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
            javafx.scene.control.Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
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
        
        LibAccessDAO accessDAO = DAOFactory.getInstance().getLibAccessDAO();
        LibrariesDAO librariesDAO = DAOFactory.getInstance().getLibrariesDAO();
        
        // Usa stream per caricare le librerie
        accessDAO.findByUserId(currentUser.getIdUser()).stream()
            .map(access -> librariesDAO.findById(access.getIdLibrary()))
            .filter(lib -> lib != null)
            .map(Libraries::getLibName)
            .forEach(libName -> libraryList.getItems().add(libName));
    }

    private void setupLibrarySelection() {
        libraryList.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                loadBooksForLibrary(newVal);
            }
        });
    }

    private void loadBooksForLibrary(String libraryName) {
        LibrariesDAO libDAO = DAOFactory.getInstance().getLibrariesDAO();
        Libraries library = libDAO.findByName(libraryName);
        if (library == null) return;

        BookDAO bookDAO = DAOFactory.getInstance().getBookDAO();

        // Salva lista originale (serve per filtri/sort)
        currentLibraryBooks = bookDAO.findByLibraryId(library.getIdLibrary());

        // Aggiorna UI
        refreshBooksList(currentLibraryBooks);

        // Mostra barra filtri
        filterBar.setVisible(true);
        filterBar.setManaged(true);
    }

    private void refreshBooksList(List<Book> books) {
        contentArea.getChildren().clear();

        if (books.isEmpty()) {
            Label emptyLabel = new Label("Nessun libro presente in questa libreria.");
            emptyLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #7f8c8d;");
            contentArea.getChildren().add(emptyLabel);
            return;
        }

        // Crea un FlowPane per visualizzare i libri come rettangoli
        FlowPane bookGrid = new FlowPane();
        bookGrid.setHgap(20);
        bookGrid.setVgap(20);
        bookGrid.setPadding(new Insets(10));
        bookGrid.setAlignment(Pos.TOP_LEFT);

        // Crea un rettangolo per ogni libro
        for (Book book : books) {
            VBox bookCard = createBookCard(book);
            bookGrid.getChildren().add(bookCard);
        }

        contentArea.getChildren().add(bookGrid);
    }
    
    private VBox createBookCard(Book book) {
        VBox card = new VBox(10);
        card.setAlignment(Pos.CENTER);
        card.setPrefWidth(150);
        card.setMaxWidth(150);
        card.setCursor(Cursor.HAND);
        
        // Rettangolo che rappresenta il libro
        Region bookRect = new Region();
        bookRect.setPrefSize(150, 200);
        bookRect.setStyle("-fx-background-color: linear-gradient(to bottom, #3498db, #2980b9); " +
                         "-fx-background-radius: 5; " +
                         "-fx-border-color: #2c3e50; " +
                         "-fx-border-width: 2; " +
                         "-fx-border-radius: 5; " +
                         "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.3), 10, 0, 0, 2);");
        
        // Titolo del libro
        Label titleLabel = new Label(book.getTitle());
        titleLabel.setWrapText(true);
        titleLabel.setMaxWidth(150);
        titleLabel.setAlignment(Pos.CENTER);
        titleLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-alignment: center;");
        
        card.getChildren().addAll(bookRect, titleLabel);
        
        // Click sul card per aprire il libro
        card.setOnMouseClicked(e -> {
            if (book.getFilePath() != null && !book.getFilePath().isEmpty()) {
                String userBasePath = currentUser != null ? currentUser.getChosenPath() : null;
                BookViewerWindow viewer = new BookViewerWindow(book, userBasePath);
                viewer.show();
            }
        });
        
        // Effetto hover
        card.setOnMouseEntered(e -> {
            bookRect.setStyle("-fx-background-color: linear-gradient(to bottom, #5dade2, #3498db); " +
                            "-fx-background-radius: 5; " +
                            "-fx-border-color: #2c3e50; " +
                            "-fx-border-width: 2; " +
                            "-fx-border-radius: 5; " +
                            "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.5), 15, 0, 0, 3);");
        });
        
        card.setOnMouseExited(e -> {
            bookRect.setStyle("-fx-background-color: linear-gradient(to bottom, #3498db, #2980b9); " +
                            "-fx-background-radius: 5; " +
                            "-fx-border-color: #2c3e50; " +
                            "-fx-border-width: 2; " +
                            "-fx-border-radius: 5; " +
                            "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.3), 10, 0, 0, 2);");
        });
        
        return card;
    }

    private void showDefaultMessage() {
        Label defaultLabel = new Label("Benvenuto nella tua Libreria Digitale!");
        defaultLabel.setStyle("-fx-font-size: 20px;");
        contentArea.getChildren().setAll(defaultLabel);
    }
    
    // Implementazione del pattern Observer
    @Override
    public void onLibraryAdded(Libraries library) {
        System.out.println("[Observer] Nuova libreria aggiunta: " + library.getLibName());
        // Ricarica automaticamente la lista delle librerie
        loadLibraries();
    }
    
    @Override
    public void onLibraryDeleted(Libraries library) {
        System.out.println("[Observer] Libreria eliminata: " + library.getLibName());
        // L'UI è già stata aggiornata nel metodo setupDeleteLibraryMenuItem
    }
    
    @Override
    public void onLibraryShared(Libraries library, String username) {
        System.out.println("[Observer] Libreria '" + library.getLibName() + "' condivisa con: " + username);
        // Potremmo aggiungere un log o una notifica visiva
    }
}
