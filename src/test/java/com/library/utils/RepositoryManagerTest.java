package com.library.utils;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Test JUnit per verificare il setup automatico del repository centrale.
 */
@DisplayName("Test del RepositoryManager")
class RepositoryManagerTest {
    
    private RepositoryManager repo;
    
    @BeforeEach
    @SuppressWarnings("unused") // Called by JUnit framework
    void setUp() {
        repo = RepositoryManager.getInstance();
    }
    
    @Test
    @DisplayName("getInstance dovrebbe restituire un'istanza valida")
    void testGetInstance() {
        assertNotNull(repo, "L'istanza del RepositoryManager non dovrebbe essere null");
    }
    
    @Test
    @DisplayName("Il percorso del repository centrale dovrebbe essere valido")
    void testCentralRepositoryPath() {
        String repoPath = repo.getCentralRepositoryPath();
        
        assertNotNull(repoPath, "Il percorso del repository non dovrebbe essere null");
        assertFalse(repoPath.isEmpty(), "Il percorso del repository non dovrebbe essere vuoto");
        
        File repoDir = new File(repoPath);
        assertTrue(repoDir.exists(), "Il repository centrale dovrebbe esistere: " + repoPath);
        assertTrue(repoDir.isDirectory(), "Il repository dovrebbe essere una directory");
    }
    
    @Test
    @DisplayName("Tutte le sottocartelle standard dovrebbero esistere")
    void testSubfoldersExist() {
        String pdfFolder = repo.getRepositorySubfolder("pdf");
        String epubFolder = repo.getRepositorySubfolder("epub");
        String txtFolder = repo.getRepositorySubfolder("txt");
        
        File pdfDir = new File(pdfFolder);
        File epubDir = new File(epubFolder);
        File txtDir = new File(txtFolder);
        
        assertTrue(pdfDir.exists() && pdfDir.isDirectory(), 
                   "La cartella PDF dovrebbe esistere: " + pdfFolder);
        assertTrue(epubDir.exists() && epubDir.isDirectory(), 
                   "La cartella EPUB dovrebbe esistere: " + epubFolder);
        assertTrue(txtDir.exists() && txtDir.isDirectory(), 
                   "La cartella TXT dovrebbe esistere: " + txtFolder);
    }
    
    @Test
    @DisplayName("existsInRepository dovrebbe restituire false per file inesistenti")
    void testFileExistence() {
        boolean exists = repo.existsInRepository("pdf/file-che-non-esiste.pdf");
        assertFalse(exists, "Un file inesistente dovrebbe restituire false");
    }
    
    @Test
    @DisplayName("getRepositorySubfolder dovrebbe restituire percorsi validi")
    void testGetRepositorySubfolder() {
        String pdfFolder = repo.getRepositorySubfolder("pdf");
        
        assertNotNull(pdfFolder, "Il percorso della sottocartella non dovrebbe essere null");
        assertTrue(pdfFolder.contains("pdf"), "Il percorso dovrebbe contenere 'pdf'");
    }
}
