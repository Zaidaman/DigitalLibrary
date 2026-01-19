package com.library.strategies;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebView;
import nl.siegmann.epublib.domain.Resource;
import nl.siegmann.epublib.epub.EpubReader;

/**
 * Strategia per visualizzare file EPUB usando WebView.
 * Design Pattern: Strategy
 */
public class EpubDisplayStrategy implements BookDisplayStrategy {
    
    private static final Logger LOGGER = Logger.getLogger(EpubDisplayStrategy.class.getName());
    
    @Override
    public void display(String filePath, StackPane contentArea) {
        try {
            nl.siegmann.epublib.domain.Book epub =
                    new EpubReader().readEpub(new FileInputStream(filePath));

            String basePath = "tmp/epub_view/";
            File baseDir = new File(basePath);
            baseDir.mkdirs();

            // Estrai tutte le risorse EPUB
            for (Resource res : epub.getResources().getAll()) {
                File out = new File(basePath + res.getHref());
                out.getParentFile().mkdirs();
                try (FileOutputStream fos = new FileOutputStream(out)) {
                    fos.write(res.getData());
                }
            }

            // Trova i capitoli (contenuti leggibili, non copertine)
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
            
            // Se non ci sono capitoli filtrati, usa tutti i contenuti
            if (chapters.isEmpty() && !epub.getContents().isEmpty()) {
                chapters.addAll(epub.getContents());
            }

            // Crea WebView per visualizzare il contenuto
            WebView webView = new WebView();
            
            // Crea i pulsanti di navigazione
            Button prevButton = new Button("◀ Precedente");
            Button nextButton = new Button("Successivo ▶");
            
            HBox navButtons = new HBox(10);
            navButtons.setAlignment(Pos.CENTER);
            navButtons.setStyle("-fx-padding: 10;");
            navButtons.getChildren().addAll(prevButton, nextButton);
            
            // Container verticale con WebView e pulsanti
            VBox container = new VBox();
            container.getChildren().addAll(webView, navButtons);
            VBox.setVgrow(webView, javafx.scene.layout.Priority.ALWAYS);
            
            // Indice del capitolo corrente
            final int[] currentIndex = { 0 };
            
            // Carica il primo capitolo
            if (!chapters.isEmpty()) {
                loadChapter(webView, basePath, chapters.get(0));
            }
            
            // Gestione pulsante Precedente
            prevButton.setOnAction(e -> {
                if (currentIndex[0] > 0) {
                    currentIndex[0]--;
                    loadChapter(webView, basePath, chapters.get(currentIndex[0]));
                }
            });
            
            // Gestione pulsante Successivo
            nextButton.setOnAction(e -> {
                if (currentIndex[0] < chapters.size() - 1) {
                    currentIndex[0]++;
                    loadChapter(webView, basePath, chapters.get(currentIndex[0]));
                }
            });
            
            contentArea.getChildren().clear();
            contentArea.getChildren().add(container);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Errore IO durante il caricamento dell'EPUB", e);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Errore generico durante il caricamento dell'EPUB", e);
        }
    }
    
    /**
     * Carica un capitolo nel WebView
     */
    private void loadChapter(WebView webView, String basePath, Resource chapter) {
        File chapterFile = new File(basePath + chapter.getHref());
        webView.getEngine().load(chapterFile.toURI().toString());
        applyMargin(webView);
    }
    
    /**
     * Applica margini al contenuto per una migliore leggibilità
     */
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
