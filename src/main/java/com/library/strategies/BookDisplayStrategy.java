package com.library.strategies;

import javafx.scene.layout.StackPane;

/**
 * Interfaccia Strategy per la visualizzazione di diversi formati di libri.
 * Design Pattern: Strategy
 */
public interface BookDisplayStrategy {
    /**
     * Visualizza un libro nel contentArea specificato
     * @param filePath Il percorso del file del libro
     * @param contentArea L'area dove visualizzare il libro
     */
    void display(String filePath, StackPane contentArea);
}
