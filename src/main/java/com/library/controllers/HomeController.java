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
import com.library.dao.LibAccessDAO;
import com.library.dao.LibrariesDAO;
import com.library.models.Author;
import com.library.models.Book;
import com.library.models.Genre;
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
import javafx.scene.web.WebView;
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
    private Button addLibraryBtn;

    @FXML
    private Button uploadBookBtn;

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
        setupAddLibraryBtn();
        setupUploadBookBtn();
        showDefaultMessage();
    }

    private void setupUploadBookBtn() {
        if (uploadBookBtn != null) {
            uploadBookBtn.setOnAction(e -> {
                if (currentUser == null) return;
                try {
                    javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/fxml/upload-book-dialog.fxml"));
                    javafx.scene.control.DialogPane dialogPane = loader.load();
                    UploadBookController controller = loader.getController();
                    controller.setCurrentUserId(currentUser.getIdUser());

                    javafx.scene.control.Dialog<Void> dialog = new javafx.scene.control.Dialog<>();
                    dialog.setDialogPane(dialogPane);
                    dialog.setTitle("Carica Libro");
                    dialog.setResizable(false);
                    dialog.initModality(javafx.stage.Modality.APPLICATION_MODAL);

                    // Chiudi su X o ESC
                    dialog.setResultConverter(buttonType -> null);
                    dialog.setOnCloseRequest(ev -> dialog.close());

                    controller.cancelBtn.setOnAction(ev -> dialog.close());

                    controller.uploadBtn.setOnAction(ev -> {
                        // Validazione campi
                        String title = controller.getTitle();
                        Author author = controller.getSelectedAuthor();
                        Genre genre = controller.getSelectedGenre();
                        File file = controller.getSelectedFile();
                        String libName = controller.getSelectedLibrary();
                        if (title == null || title.isEmpty() || author == null || genre == null || file == null || libName == null) {
                            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR, "Compila tutti i campi e seleziona un file.");
                            alert.showAndWait();
                            return;
                        }

                        // Copia file nella cartella risorse (pdf/epub)
                        String ext = file.getName().toLowerCase().endsWith(".pdf") ? ".pdf" : ".epub";
                        String destFolder = ext.equals(".pdf") ? "src/main/resources/pdf/" : "src/main/resources/epub/";
                        File destDir = new File(destFolder);
                        destDir.mkdirs();
                        String destFileName = System.currentTimeMillis() + "_" + file.getName();
                        File destFile = new File(destDir, destFileName);
                        try {
                            java.nio.file.Files.copy(file.toPath(), destFile.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                        } catch (Exception ex) {
                            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR, "Errore nel salvataggio del file: " + ex.getMessage());
                            alert.showAndWait();
                            return;
                        }

                        // Inserisci libro nel DB
                        BookDAO bookDAO = new BookDAO();
                        Book book = new Book(title, null, null, (ext.equals(".pdf") ? "pdf/" : "epub/") + destFileName, -1);
                        // Anno pubblicazione non gestito, metti -1 o chiedi campo
                        bookDAO.insert(book, author.getIdAuthor(), -1);


                        // Recupera id libro appena inserito (assumendo titolo univoco)
                        Book inserted = bookDAO.findByTitle(title);
                        if (inserted == null) {
                            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR, "Errore inserimento libro nel database.");
                            alert.showAndWait();
                            return;
                        }
                        int idBook = -1;
                        try {
                            java.lang.reflect.Field f = inserted.getClass().getDeclaredField("IdBook");
                            f.setAccessible(true);
                            idBook = f.getInt(inserted);
                        } catch (Exception ex) {
                            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR, "Impossibile recuperare l'ID del libro.");
                            alert.showAndWait();
                            return;
                        }

                        // Associa libro a libreria
                        LibrariesDAO librariesDAO = new LibrariesDAO();
                        Libraries lib = librariesDAO.findByName(libName);
                        if (lib != null) {
                            com.library.dao.BookLibDAO bookLibDAO = new com.library.dao.BookLibDAO();
                            bookLibDAO.insert(new com.library.models.BookLib(idBook, lib.getIdLibrary()));
                        }

                        // Associa libro a genere
                        com.library.dao.BookGenreDAO bookGenreDAO = new com.library.dao.BookGenreDAO();
                        bookGenreDAO.insert(new com.library.models.BookGenre(idBook, genre.getIdGenre()));

                        dialog.close();
                        // Aggiorna lista libri
                        loadBooksForLibrary(libName);
                    });

                    dialog.showAndWait();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            });
        }
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

    public void showEpub(WebView webView, String epubFileName) {
        try {
            // Nascondi i pulsanti finché non carichi tutto
            prevBtn.setVisible(false);
            nextBtn.setVisible(false);

            InputStream epubStream = getClass().getClassLoader()
                                        .getResourceAsStream("epub/" + epubFileName);
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

        } catch (Exception e) {
            e.printStackTrace();
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
        } catch (Exception e) {
            e.printStackTrace();
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

        } catch (NullPointerException e) {
            System.err.println("Errore: PDF non trovato o risorsa mancante: " + e.getMessage());
            showDefaultMessage();
        } catch (IOException e) {
            System.err.println("Errore IO durante il caricamento del PDF: " + e.getMessage());
            showDefaultMessage();
        }
    }
}
