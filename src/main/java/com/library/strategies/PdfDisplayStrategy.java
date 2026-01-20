package com.library.strategies;

import java.io.File;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.icepdf.ri.common.SwingController;
import org.icepdf.ri.common.SwingViewBuilder;
import org.icepdf.ri.common.views.DocumentViewController;

import javafx.embed.swing.SwingNode;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

/**
 * Strategia per visualizzare file PDF usando ICEpdf.
 * Design Pattern: Strategy
 */
public class PdfDisplayStrategy implements BookDisplayStrategy {
    
    private SwingController controller;
    private boolean toolbarVisible = false;
    
    @Override
    public void display(String filePath, StackPane contentArea) {
        try {
            controller = new SwingController();
            
            SwingViewBuilder factory = new SwingViewBuilder(controller);
            JPanel viewerPanel = factory.buildViewerPanel();
            
            // Aprire il documento
            controller.openDocument(new File(filePath).getAbsolutePath());
            
            // Nascondere toolbar e utility pane
            SwingUtilities.invokeLater(() -> {
                controller.setToolBarVisible(false);
                controller.setUtilityPaneVisible(false);
            });
            
            // Creare il nodo Swing per il viewer PDF
            SwingNode swingNode = new SwingNode();
            swingNode.setContent(viewerPanel);
            
            // Applicare il fit mode dopo che il componente è stato aggiunto alla scena
            // Questo risolve il problema dello zoom iniziale
            contentArea.sceneProperty().addListener((obs, oldScene, newScene) -> {
                if (newScene != null) {
                    newScene.windowProperty().addListener((obsWin, oldWindow, newWindow) -> {
                        if (newWindow != null) {
                            newWindow.showingProperty().addListener((obsShowing, wasShowing, isShowing) -> {
                                if (isShowing) {
                                    // Aspettare che la finestra sia completamente renderizzata
                                    javafx.application.Platform.runLater(() -> {
                                        SwingUtilities.invokeLater(() -> {
                                            controller.setPageFitMode(DocumentViewController.PAGE_FIT_WINDOW_HEIGHT, true);
                                        });
                                    });
                                }
                            });
                        }
                    });
                }
            });
            
            // Creare il pulsante per mostrare/nascondere le opzioni
            Button toggleButton = new Button("Mostra Opzioni");
            toggleButton.setStyle("-fx-padding: 10 20; -fx-font-size: 14px; -fx-background-color: #4CAF50; -fx-text-fill: white; -fx-cursor: hand;");
            toggleButton.setOnAction(e -> toggleToolbar(toggleButton));
            
            // Container per il pulsante - sempre visibile in alto
            HBox buttonBar = new HBox(10, toggleButton);
            buttonBar.setAlignment(Pos.CENTER_RIGHT);
            buttonBar.setStyle("-fx-padding: 10; -fx-background-color: #e0e0e0; -fx-min-height: 50;");
            
            // Layout principale con pulsante sopra e viewer sotto
            VBox mainLayout = new VBox(0);
            mainLayout.getChildren().addAll(buttonBar, swingNode);
            VBox.setVgrow(swingNode, Priority.ALWAYS);
            
            contentArea.getChildren().clear();
            contentArea.getChildren().add(mainLayout);
            
        } catch (Exception e) {
            System.err.println("Errore nel caricamento del PDF: " + e.getMessage());
        }
    }
    
    /**
     * Toggle della visibilità della toolbar e utility pane
     */
    private void toggleToolbar(Button button) {
        toolbarVisible = !toolbarVisible;
        
        SwingUtilities.invokeLater(() -> {
            controller.setToolBarVisible(toolbarVisible);
            controller.setUtilityPaneVisible(toolbarVisible);
        });
        
        // Aggiornare il testo e lo stile del pulsante
        button.setText(toolbarVisible ? "Nascondi Opzioni" : "Mostra Opzioni");
        button.setStyle(toolbarVisible 
            ? "-fx-padding: 10 20; -fx-font-size: 14px; -fx-background-color: #f44336; -fx-text-fill: white; -fx-cursor: hand;"
            : "-fx-padding: 10 20; -fx-font-size: 14px; -fx-background-color: #4CAF50; -fx-text-fill: white; -fx-cursor: hand;");
    }
}
