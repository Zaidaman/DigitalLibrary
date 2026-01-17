package com.library.controllers;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.swing.JPanel;

import org.icepdf.ri.common.SwingController;
import org.icepdf.ri.common.SwingViewBuilder;

import com.library.dao.BookDAO;
import com.library.dao.BookLibDAO;
import com.library.dao.LibAccessDAO;
import com.library.dao.LibUserDAO;
import com.library.dao.LibrariesDAO;
import com.library.models.Book;
import com.library.models.LibAccess;
import com.library.models.LibUser;
import com.library.models.Libraries;

import javafx.embed.swing.SwingNode;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import nl.siegmann.epublib.domain.Resource;
import nl.siegmann.epublib.epub.EpubReader;

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
    private Button prevBtn;

    @FXML
    private Button nextBtn;

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

    private boolean booksPanelVisible = true;

    private LibUser currentUser;

    private List<Resource> epubChapters;       // Lista dei capitoli dell'EPUB
    private int currentChapterIndex = 0;       // Capitolo attuale
    private File tempDirForEpub;               // Cartella temporanea per i file estratti


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
                    stage.showAndWait();
                } catch (Exception ex) {
                    ex.printStackTrace();
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
                BookDAO bookDAO = new BookDAO();
                List<Book> allBooks = bookDAO.findAll();
                
                // Ottieni i libri già nella libreria
                List<Book> booksInLibrary = bookDAO.findByLibraryId(library.getIdLibrary());
                List<String> titlesInLibrary = new ArrayList<>();
                for (Book b : booksInLibrary) {
                    titlesInLibrary.add(b.getTitle());
                }
                
                // Filtra i libri disponibili (non ancora nella libreria)
                List<String> availableBooks = new ArrayList<>();
                for (Book b : allBooks) {
                    if (!titlesInLibrary.contains(b.getTitle())) {
                        availableBooks.add(b.getTitle());
                    }
                }
                
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
                            BookLibDAO bookLibDAO = new BookLibDAO();
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
                
                LibrariesDAO libDAO = new LibrariesDAO();
                Libraries library = libDAO.findByName(selectedLibrary);
                if (library == null) return;
                
                // Ottieni i libri nella libreria
                BookDAO bookDAO = new BookDAO();
                List<Book> booksInLibrary = bookDAO.findByLibraryId(library.getIdLibrary());
                
                if (booksInLibrary.isEmpty()) {
                    showAlert("Nessun libro", "Non ci sono libri in questa libreria.");
                    return;
                }
                
                List<String> bookTitles = new ArrayList<>();
                for (Book b : booksInLibrary) {
                    bookTitles.add(b.getTitle());
                }
                
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
                            BookLibDAO bookLibDAO = new BookLibDAO();
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

    private void setupShareLibraryMenuItem() {
        if (shareLibraryMenuItem != null) {
            shareLibraryMenuItem.setOnAction(e -> {
                String selectedLibrary = libraryList.getSelectionModel().getSelectedItem();
                if (selectedLibrary == null) {
                    showAlert("Nessuna libreria selezionata", "Seleziona una libreria da condividere.");
                    return;
                }
                
                LibrariesDAO libDAO = new LibrariesDAO();
                Libraries library = libDAO.findByName(selectedLibrary);
                if (library == null) return;
                
                // Ottieni lista di tutti gli utenti tranne quello corrente
                LibUserDAO userDAO = new LibUserDAO();
                List<LibUser> allUsers = userDAO.findAll();
                List<String> usernames = new ArrayList<>();
                for (LibUser user : allUsers) {
                    if (user.getIdUser() != currentUser.getIdUser()) {
                        usernames.add(user.getUsername());
                    }
                }
                
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
                        LibAccessDAO accessDAO = new LibAccessDAO();
                        // Verifica se l'utente ha già accesso
                        boolean alreadyHasAccess = accessDAO.findByUserId(targetUser.getIdUser())
                            .stream().anyMatch(a -> a.getIdLibrary() == library.getIdLibrary());
                        
                        if (alreadyHasAccess) {
                            showAlert("Già condivisa", "L'utente ha già accesso a questa libreria.");
                        } else {
                            accessDAO.insert(new LibAccess(targetUser.getIdUser(), library.getIdLibrary()));
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
                    LibAccessDAO accessDAO = new LibAccessDAO();
                    
                    // Rimuovi l'accesso dell'utente corrente
                    accessDAO.deleteByUserAndLibrary(currentUser.getIdUser(), library.getIdLibrary());
                    
                    // Verifica se altri utenti hanno ancora accesso
                    List<LibAccess> remainingAccess = accessDAO.findByLibraryId(library.getIdLibrary());
                    
                    if (remainingAccess.isEmpty()) {
                        // Nessun altro ha accesso, elimina completamente
                        BookLibDAO bookLibDAO = new BookLibDAO();
                        bookLibDAO.deleteByLibraryId(library.getIdLibrary());
                        libDAO.delete(library.getIdLibrary());
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

    public void showEpub(WebView webView, String epubFileName) {
        try {
            // Nascondi i pulsanti finché non carichi tutto
            prevBtn.setVisible(false);
            nextBtn.setVisible(false);

            // Prima prova a caricare da library-data/ (file aggiunti dall'utente)
            File epubFile = new File("library-data/epub/" + epubFileName);
            InputStream epubStream;
            
            if (epubFile.exists()) {
                epubStream = new java.io.FileInputStream(epubFile);
            } else {
                // Altrimenti carica dal classpath (risorse statiche)
                epubStream = getClass().getClassLoader().getResourceAsStream("epub/" + epubFileName);
            }
            
            if (epubStream == null) {
                System.out.println("❌ EPUB non trovato: " + epubFileName);
                return;
            }

            nl.siegmann.epublib.domain.Book epub = new EpubReader().readEpub(epubStream);

            // Estrai cartella temporanea
            tempDirForEpub = Files.createTempDirectory("epub_view_").toFile();
            tempDirForEpub.deleteOnExit();

            for (Resource res : epub.getResources().getAll()) {
                File outFile = new File(tempDirForEpub, res.getHref());
                outFile.getParentFile().mkdirs();
                try (FileOutputStream fos = new FileOutputStream(outFile)) {
                    fos.write(res.getData());
                }
            }

            // ### COPERTINA ###
            // Cerca tra le risorse la prima immagine che sembra essere la copertina
            File coverFile = null;
            for (Resource res : epub.getResources().getAll()) {
                String href = res.getHref().toLowerCase();
                // filtra per immagini e parole chiave comuni per cover
                if ((href.endsWith(".jpg") || href.endsWith(".jpeg") || href.endsWith(".png"))
                        && href.contains("cover")) {
                    coverFile = new File(tempDirForEpub, res.getHref());
                    break;
                }
            }

            // Se trovi una copertina, caricala prima
            if (coverFile != null && coverFile.exists()) {
                webView.getEngine().load(coverFile.toURI().toString());

                applyMargin(webView);
            } else {
                System.out.println("📌 Copertina non trovata o non nominata come cover");
            }

            // ## TROVA CAPITOLI ##
            epubChapters = new ArrayList<>();
            for (Resource res : epub.getContents()) {
                String href = res.getHref().toLowerCase();
                if (!href.contains("cover") && !href.contains("titlepage")
                        && (href.endsWith(".html") || href.endsWith(".xhtml"))) {
                    String content = new String(res.getData(), StandardCharsets.UTF_8);
                    if (content.trim().length() > 50) {
                        epubChapters.add(res);
                    }
                }
            }

            // Mostra i pulsanti se ci sono capitoli
            if (!epubChapters.isEmpty()) {
                prevBtn.setVisible(true);
                nextBtn.setVisible(true);
                currentChapterIndex = 0;
                setupNavigation(webView);
            } else {
                System.out.println("❌ Nessun capitolo html reale trovato nell'EPUB");
            }

        } catch (IOException | NullPointerException e) {
            System.err.println("Errore durante il caricamento dell'EPUB: " + e.getMessage());
            prevBtn.setVisible(false);
            nextBtn.setVisible(false);
        }
    }

    private void applyMargin(WebView webView) {
        webView.getEngine().getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            if (newState == javafx.concurrent.Worker.State.SUCCEEDED) {
                webView.getEngine().executeScript(
                    "document.body.style.margin='20px';" +
                    "document.body.style.padding='10px';"
                );
            }
        });
    }

    private void loadChapter(WebView webView, Resource chapter) {
        try {
            File chapterFile = new File(tempDirForEpub, chapter.getHref());
            webView.getEngine().load(chapterFile.toURI().toString());
            applyMargin(webView);
        } catch (NullPointerException e) {
            System.err.println("Errore durante il caricamento del capitolo EPUB: " + e.getMessage());
        }
    }

    private void setupNavigation(WebView webView) {
        prevBtn.setOnAction(e -> {
            if (epubChapters != null && currentChapterIndex > 0) {
                currentChapterIndex--;
                loadChapter(webView, epubChapters.get(currentChapterIndex));
            }
        });

        nextBtn.setOnAction(e -> {
            if (epubChapters != null && currentChapterIndex < epubChapters.size() - 1) {
                currentChapterIndex++;
                loadChapter(webView, epubChapters.get(currentChapterIndex));
            }
        });
    }

    private void setupBookSelection() {
        booksList.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && !newVal.equals("Nessun libro presente in questa libreria.")) {
                BookDAO bookDAO = new BookDAO();
                Book book = bookDAO.findByTitle(newVal); // restituisce filePath relativo

                if (book != null && book.getFilePath() != null && !book.getFilePath().isEmpty()) {
                    if (book.getFilePath().endsWith(".pdf")) {
                        showPdfFromResource(book.getFilePath());
                        prevBtn.setVisible(false);
                        nextBtn.setVisible(false);
                    } else if (book.getFilePath().endsWith(".epub")) {
                        // Usa il WebView già presente nella UI
                        WebView webView = new WebView();
                        contentArea.getChildren().setAll(webView); // mostra il WebView nel contentArea
                        showEpub(webView, new File(book.getFilePath()).getName());
                    }
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
            // Prima prova a caricare da library-data/ (file aggiunti dall'utente)
            File pdfFile = new File("library-data/" + resourcePath);
            InputStream pdfStream;
            
            if (pdfFile.exists()) {
                pdfStream = new java.io.FileInputStream(pdfFile);
            } else {
                // Altrimenti carica dal classpath (risorse statiche)
                pdfStream = getClass().getClassLoader().getResourceAsStream(resourcePath);
            }
            
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

        } catch (NullPointerException e) {
            System.err.println("Errore: PDF non trovato o risorsa mancante: " + e.getMessage());
            showDefaultMessage();
        } catch (IOException e) {
            System.err.println("Errore IO durante il caricamento del PDF: " + e.getMessage());
            showDefaultMessage();
        }
    }
}
