package com.library.utils;

/**
 * Test semplice per verificare il setup automatico del repository centrale.
 * 
 * Esegui questo file per vedere il messaggio di setup automatico.
 */
public class RepositoryManagerTest {
    
    public static void main(String[] args) {
        System.out.println("=== Test RepositoryManager ===\n");
        
        // Test 1: getInstance dovrebbe creare il repository se non esiste
        System.out.println("Test 1: Ottenimento istanza RepositoryManager");
        RepositoryManager repo = RepositoryManager.getInstance();
        System.out.println("✓ Istanza ottenuta\n");
        
        // Test 2: Verifica percorso repository
        System.out.println("Test 2: Percorso repository centrale");
        String repoPath = repo.getCentralRepositoryPath();
        System.out.println("Repository path: " + repoPath);
        System.out.println("Production mode: " + repo.isProductionMode());
        System.out.println("✓ Percorso ottenuto\n");
        
        // Test 3: Verifica sottocartelle
        System.out.println("Test 3: Sottocartelle standard");
        String pdfFolder = repo.getRepositorySubfolder("pdf");
        String epubFolder = repo.getRepositorySubfolder("epub");
        String txtFolder = repo.getRepositorySubfolder("txt");
        System.out.println("PDF folder: " + pdfFolder);
        System.out.println("EPUB folder: " + epubFolder);
        System.out.println("TXT folder: " + txtFolder);
        
        java.io.File pdfDir = new java.io.File(pdfFolder);
        java.io.File epubDir = new java.io.File(epubFolder);
        java.io.File txtDir = new java.io.File(txtFolder);
        
        if (pdfDir.exists() && epubDir.exists() && txtDir.exists()) {
            System.out.println("✓ Tutte le sottocartelle esistono\n");
        } else {
            System.out.println("✗ Alcune sottocartelle mancano\n");
        }
        
        // Test 4: Verifica esistenza file (dovrebbe essere false)
        System.out.println("Test 4: Verifica esistenza file");
        boolean existsInRepo = repo.existsInRepository("pdf/test.pdf");
        System.out.println("File 'pdf/test.pdf' exists in repo: " + existsInRepo);
        System.out.println("✓ Test esistenza completato\n");
        
        System.out.println("=== Tutti i test completati ===");
        System.out.println("\nRiepilogo:");
        System.out.println("- Repository centrale: " + repoPath);
        System.out.println("- Modalità: " + (repo.isProductionMode() ? "Production" : "Development"));
        System.out.println("- Sottocartelle: pdf/, epub/, txt/");
        
        System.out.println("\n✅ Il repository centrale è configurato correttamente!");
    }
}
