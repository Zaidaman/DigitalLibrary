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

import javax.swing.JPanel;

import org.icepdf.ri.common.SwingController;
import org.icepdf.ri.common.SwingViewBuilder;

import com.library.models.Book;

import javafx.embed.swing.SwingNode;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import nl.siegmann.epublib.domain.Resource;
import nl.siegmann.epublib.epub.EpubReader;

public class BookViewerWindow {

    private Book book;
    private Stage stage;

    public BookViewerWindow(Book book) {
        this.book = book;
    }

    public void show() {
        try {
            stage = new Stage();
            stage.setTitle(book.getTitle());
            stage.initModality(Modality.APPLICATION_MODAL);

            VBox container = new VBox(10);
            container.setStyle("-fx-padding: 10; -fx-background-color: white;");

            if (book.getFilePath().endsWith(".pdf")) {
                SwingNode pdfNode = loadPdfContent();
                if (pdfNode != null) {
                    container.getChildren().add(pdfNode);
                }
            } else if (book.getFilePath().endsWith(".epub")) {
                WebView webView = new WebView();

                HBox navButtons = new HBox(10);
                navButtons.setAlignment(Pos.CENTER);
                Button prevButton = new Button("◀ Precedente");
                Button nextButton = new Button("Successivo ▶");
                navButtons.getChildren().addAll(prevButton, nextButton);

                container.getChildren().addAll(webView, navButtons);
                loadEpubContent(webView, prevButton, nextButton);
            }

            Scene scene = new Scene(container, 900, 700);
            stage.setScene(scene);
            stage.show();

        } catch (Exception e) {
            System.err.println("Errore apertura finestra libro: " + e.getMessage());
        }
    }

    private SwingNode loadPdfContent() {
        try {
            File pdfFile = new File("library-data/" + book.getFilePath());
            InputStream pdfStream;

            if (pdfFile.exists()) {
                pdfStream = new java.io.FileInputStream(pdfFile);
            } else {
                pdfStream = getClass().getClassLoader().getResourceAsStream(book.getFilePath());
            }

            if (pdfStream == null) {
                System.out.println("PDF non trovato: " + book.getFilePath());
                return null;
            }

            File tempFile = File.createTempFile("temp_pdf_", ".pdf");
            tempFile.deleteOnExit();
            Files.copy(pdfStream, tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

            SwingController controller = new SwingController();
            SwingViewBuilder factory = new SwingViewBuilder(controller);
            JPanel viewerPanel = factory.buildViewerPanel();
            controller.openDocument(tempFile.getAbsolutePath());

            SwingNode swingNode = new SwingNode();
            swingNode.setContent(viewerPanel);

            return swingNode;

        } catch (IOException e) {
            System.err.println("Errore caricamento PDF: " + e.getMessage());
            return null;
        }
    }

    private void loadEpubContent(WebView webView, Button prevBtn, Button nextBtn) {
        try {
            File epubFile = new File("library-data/epub/" + new File(book.getFilePath()).getName());
            InputStream epubStream;

            if (epubFile.exists()) {
                epubStream = new java.io.FileInputStream(epubFile);
            } else {
                epubStream = getClass().getClassLoader()
                        .getResourceAsStream("epub/" + new File(book.getFilePath()).getName());
            }

            if (epubStream == null) {
                System.out.println("EPUB non trovato: " + book.getFilePath());
                return;
            }

            nl.siegmann.epublib.domain.Book epub = new EpubReader().readEpub(epubStream);
            File tempDir = Files.createTempDirectory("epub_view_").toFile();
            tempDir.deleteOnExit();

            for (Resource res : epub.getResources().getAll()) {
                File outFile = new File(tempDir, res.getHref());
                outFile.getParentFile().mkdirs();
                try (FileOutputStream fos = new FileOutputStream(outFile)) {
                    fos.write(res.getData());
                }
            }

            // Carica copertina
            File coverFile = null;
            for (Resource res : epub.getResources().getAll()) {
                String href = res.getHref().toLowerCase();
                if ((href.endsWith(".jpg") || href.endsWith(".jpeg") || href.endsWith(".png"))
                        && href.contains("cover")) {
                    coverFile = new File(tempDir, res.getHref());
                    break;
                }
            }

            if (coverFile != null && coverFile.exists()) {
                webView.getEngine().load(coverFile.toURI().toString());
                applyMargin(webView);
            }

            // Trova capitoli
            List<Resource> chapters = new ArrayList<>();
            for (Resource res : epub.getContents()) {
                String href = res.getHref().toLowerCase();
                if (!href.contains("cover") && !href.contains("titlepage")
                        && (href.endsWith(".html") || href.endsWith(".xhtml"))) {
                    String content = new String(res.getData(), StandardCharsets.UTF_8);
                    if (content.trim().length() > 50) {
                        chapters.add(res);
                    }
                }
            }

            if (!chapters.isEmpty()) {
                final int[] currentIndex = { 0 };

                prevBtn.setOnAction(e -> {
                    if (currentIndex[0] > 0) {
                        currentIndex[0]--;
                        loadChapter(webView, tempDir, chapters.get(currentIndex[0]));
                    }
                });

                nextBtn.setOnAction(e -> {
                    if (currentIndex[0] < chapters.size() - 1) {
                        currentIndex[0]++;
                        loadChapter(webView, tempDir, chapters.get(currentIndex[0]));
                    }
                });
            }

        } catch (IOException e) {
            System.err.println("Errore caricamento EPUB: " + e.getMessage());
        }
    }

    private void loadChapter(WebView webView, File tempDir, Resource chapter) {
        File chapterFile = new File(tempDir, chapter.getHref());
        webView.getEngine().load(chapterFile.toURI().toString());
        applyMargin(webView);
    }

    private void applyMargin(WebView webView) {
        webView.getEngine().getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            if (newState == javafx.concurrent.Worker.State.SUCCEEDED) {
                webView.getEngine().executeScript(
                        "document.body.style.margin='20px';" +
                                "document.body.style.padding='10px';");
            }
        });
    }
}