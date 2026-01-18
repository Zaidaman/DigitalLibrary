package com.library.strategies;

import javafx.scene.layout.StackPane;
import javafx.scene.web.WebView;
import nl.siegmann.epublib.domain.Resource;
import nl.siegmann.epublib.epub.EpubReader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

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

            String firstChapter = epub.getContents().get(0).getHref();
            File chapterFile = new File(basePath + firstChapter);

            WebView webView = new WebView();
            webView.getEngine().load(chapterFile.toURI().toString());
            
            contentArea.getChildren().clear();
            contentArea.getChildren().add(webView);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Errore IO durante il caricamento dell'EPUB", e);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Errore generico durante il caricamento dell'EPUB", e);
        }
    }
}
