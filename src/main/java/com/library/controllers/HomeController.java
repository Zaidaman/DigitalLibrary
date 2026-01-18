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
import com.library.observers.LibrarySubject;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
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
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

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
    private MenuItem removeBookFromLibraryMenuItem;

    @FXML
    private MenuItem logoutMenuItem;

    @FXML
    private ComboBox<String> sortCombo;
    
    @FXML
    private MenuButton filterMenuBtn;
    
    @FXML
    private CheckMenuItem pdfFilterItem;
    
    @FXML
    private CheckMenuItem epubFilterItem;
    
    @FXML
    private Button clearFiltersBtn;

    private boolean booksPanelVisible = true;

    private LibUser currentUser;
    private final LibrarySubject librarySubject = new LibrarySubject();

    private List<Book> currentLibraryBooks = new ArrayList<>();

    private String selectedSort = "NONE";
    private String activeFilterType = "NONE";

    @FXML
    public void initialize() {
        // L'inizializzazione vera avviene dopo che l'utente è stato settato
        // (tramite setUser)
        booksPanel.setVisible(false);
        booksPanel.setManaged(false);
        booksPanelVisible = true;
        setupToggleButton();
        setupBookSelection();
        setupAddLibraryMenuItem();
        setupShareLibraryMenuItem();
        setupDeleteLibraryMenuItem();
        setupAddBookMenuItem();
        setupAddExistingBookMenuItem();
        setupRemoveBookFromLibraryMenuItem();
        setupLogoutMenuItem();
        showDefaultMessage();
        setupSort();
        setupFilter();
        setupClearButton();
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
                            // Aggiorna lista
                            loadLibraries();
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
                    booksList.getItems().clear();
                    showDefaultMessage();
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
                activeFilterType = "PDF";
            } else {
                activeFilterType = "NONE";
            }
                
            applyFiltersAndSort();
        });

        epubFilterItem.setOnAction(e -> {
            if (epubFilterItem.isSelected()) {

                pdfFilterItem.setSelected(false);
                activeFilterType = "EPUB";

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

        // Mostra pannello libri
        booksPanel.setVisible(true);
        booksPanel.setManaged(true);
        booksPanelVisible = true;
        toggleBooksBtn.setText("⮜");
    }

    private void refreshBooksList(List<Book> books) {
        booksList.getItems().clear();

        if (books.isEmpty()) {
            booksList.getItems().add("Nessun libro presente in questa libreria.");
            showDefaultMessage();
            return;
        }

        // Usa stream per popolare la lista
        books.stream()
            .map(Book::getTitle)
            .forEach(title -> booksList.getItems().add(title));
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
                Book book = bookDAO.findByTitle(newVal);

                if (book != null && book.getFilePath() != null && !book.getFilePath().isEmpty()) {
                    // Passa il percorso base dell'utente al viewer
                    String userBasePath = currentUser != null ? currentUser.getChosenPath() : null;
                    BookViewerWindow viewer = new BookViewerWindow(book, userBasePath);
                    viewer.show();
                }
            }
        });
    }

    private void showDefaultMessage() {
        Label defaultLabel = new Label("Benvenuto nella tua Libreria Digitale!");
        defaultLabel.setStyle("-fx-font-size: 20px;");
        contentArea.getChildren().setAll(defaultLabel);
    }
}
