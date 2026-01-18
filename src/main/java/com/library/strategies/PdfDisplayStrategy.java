package com.library.strategies;

import javafx.embed.swing.SwingNode;
import javafx.scene.layout.StackPane;
import org.icepdf.ri.common.SwingController;
import org.icepdf.ri.common.SwingViewBuilder;

import javax.swing.JPanel;
import java.io.File;

/**
 * Strategia per visualizzare file PDF usando ICEpdf.
 * Design Pattern: Strategy
 */
public class PdfDisplayStrategy implements BookDisplayStrategy {
    
    @Override
    public void display(String filePath, StackPane contentArea) {
        try {
            SwingController controller = new SwingController();
            SwingViewBuilder factory = new SwingViewBuilder(controller);
            JPanel viewerPanel = factory.buildViewerPanel();
            
            controller.openDocument(new File(filePath).getAbsolutePath());
            
            SwingNode swingNode = new SwingNode();
            swingNode.setContent(viewerPanel);
            
            contentArea.getChildren().clear();
            contentArea.getChildren().add(swingNode);
        } catch (Exception e) {
            System.err.println("Errore nel caricamento del PDF: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
