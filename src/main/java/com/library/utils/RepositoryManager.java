package com.library.utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Gestisce il repository centrale dei file condivisi e i percorsi delle cartelle personali degli utenti.
 * 
 * Repository centrale:
 * - Development: ./library-data/ (cartella del progetto)
 * - Production (Windows): C:\ProgramData\DigitalLibrary\repository\
 * - Production (Linux/Mac): /var/lib/digitallibrary/repository/
 */
public class RepositoryManager {
    
    private static final Logger logger = LoggerFactory.getLogger(RepositoryManager.class);
    private static final String APP_NAME = "DigitalLibrary";
    private static final String REPOSITORY_FOLDER = "repository";
    
    private static RepositoryManager instance;
    private String centralRepositoryPath;  // Non final per permettere fallback
    private final boolean isProduction;
    
    private RepositoryManager() {
        // Determina se siamo in modalità development o production
        // In development, la cartella library-data esiste già nel progetto
        File devRepo = new File("library-data");
        this.isProduction = !devRepo.exists() || System.getProperty("app.production") != null;
        
        if (isProduction) {
            this.centralRepositoryPath = getProductionRepositoryPath();
        } else {
            this.centralRepositoryPath = devRepo.getAbsolutePath();
        }
        
        // Setup automatico del repository centrale
        setupCentralRepository();
        
        // Sincronizza i file da library-data se siamo in production
        if (isProduction) {
            syncFromLibraryData();
        }
    }
    
    public static synchronized RepositoryManager getInstance() {
        if (instance == null) {
            instance = new RepositoryManager();
        }
        return instance;
    }
    
    /**
     * Restituisce il percorso del repository centrale.
     */
    public String getCentralRepositoryPath() {
        return centralRepositoryPath;
    }
    
    /**
     * Restituisce il percorso completo per un file nel repository centrale.
     * 
     * @param relativeFilePath percorso relativo (es. "epub/libro.epub")
     * @return percorso assoluto nel repository centrale
     */
    public String getCentralFilePath(String relativeFilePath) {
        return centralRepositoryPath + File.separator + relativeFilePath;
    }
    
    /**
     * Salva un file nel repository centrale.
     * 
     * @param sourceFile file sorgente da copiare
     * @param relativeDestPath percorso relativo di destinazione (es. "epub/libro.epub")
     * @return percorso assoluto del file salvato
     * @throws IOException se la copia fallisce
     */
    public String saveToRepository(File sourceFile, String relativeDestPath) throws IOException {
        String destPath = getCentralFilePath(relativeDestPath);
        File destFile = new File(destPath);
        
        // Crea le directory padre se non esistono
        destFile.getParentFile().mkdirs();
        
        // Copia il file
        Files.copy(
            sourceFile.toPath(),
            destFile.toPath(),
            StandardCopyOption.REPLACE_EXISTING
        );
        
        return destFile.getAbsolutePath();
    }
    
    /**
     * Copia un file dal repository centrale alla cartella personale dell'utente.
     * 
     * @param relativeFilePath percorso relativo del file (es. "epub/libro.epub")
     * @param userBasePath cartella base dell'utente
     * @return percorso assoluto del file copiato nella cartella utente
     * @throws IOException se la copia fallisce
     */
    public String copyToUserFolder(String relativeFilePath, String userBasePath) throws IOException {
        if (userBasePath == null || userBasePath.trim().isEmpty()) {
            throw new IllegalArgumentException("User base path cannot be null or empty");
        }
        
        String sourcePath = getCentralFilePath(relativeFilePath);
        File sourceFile = new File(sourcePath);
        
        if (!sourceFile.exists()) {
            throw new IOException("File not found in central repository: " + sourcePath);
        }
        
        String destPath = userBasePath + File.separator + relativeFilePath;
        File destFile = new File(destPath);
        
        // Crea le directory padre se non esistono
        destFile.getParentFile().mkdirs();
        
        // Copia il file
        Files.copy(
            sourceFile.toPath(),
            destFile.toPath(),
            StandardCopyOption.REPLACE_EXISTING
        );
        
        return destFile.getAbsolutePath();
    }
    
    /**
     * Trova un file cercando prima nella cartella utente, poi nel repository centrale.
     * Se trovato solo nel repository centrale, lo copia automaticamente nella cartella utente.
     * 
     * @param relativeFilePath percorso relativo del file
     * @param userBasePath cartella base dell'utente (può essere null)
     * @return percorso assoluto del file, o null se non trovato
     */
    public String resolveFilePath(String relativeFilePath, String userBasePath) {
        // 1. Prova nella cartella utente
        if (userBasePath != null && !userBasePath.trim().isEmpty()) {
            String userFilePath = userBasePath + File.separator + relativeFilePath;
            File userFile = new File(userFilePath);
            if (userFile.exists()) {
                return userFile.getAbsolutePath();
            }
        }
        
        // 2. Prova nel repository centrale
        String centralFilePath = getCentralFilePath(relativeFilePath);
        File centralFile = new File(centralFilePath);
        if (centralFile.exists()) {
            // Se l'utente ha una cartella personale, copia il file lì per accessi futuri
            if (userBasePath != null && !userBasePath.trim().isEmpty()) {
                try {
                    return copyToUserFolder(relativeFilePath, userBasePath);
                } catch (IOException e) {
                    logger.warn("Could not copy file to user folder: {}", e.getMessage());
                    // Restituisci comunque il percorso centrale
                    return centralFilePath;
                }
            }
            return centralFilePath;
        }
        
        // 3. File non trovato
        logger.error("File not found: {}", relativeFilePath);
        return null;
    }
    
    /**
     * Verifica se un file esiste nel repository centrale.
     */
    public boolean existsInRepository(String relativeFilePath) {
        String centralPath = getCentralFilePath(relativeFilePath);
        return new File(centralPath).exists();
    }
    
    /**
     * Verifica se un file esiste nella cartella utente.
     */
    public boolean existsInUserFolder(String relativeFilePath, String userBasePath) {
        if (userBasePath == null || userBasePath.trim().isEmpty()) {
            return false;
        }
        String userPath = userBasePath + File.separator + relativeFilePath;
        return new File(userPath).exists();
    }
    
    /**
     * Determina il percorso del repository in modalità production.
     */
    private String getProductionRepositoryPath() {
        String os = System.getProperty("os.name").toLowerCase();
        
        if (os.contains("win")) {
            // Windows: C:\ProgramData\DigitalLibrary\repository
            String programData = System.getenv("ProgramData");
            if (programData == null || programData.isEmpty()) {
                programData = "C:\\ProgramData";
            }
            return programData + File.separator + APP_NAME + File.separator + REPOSITORY_FOLDER;
            
        } else if (os.contains("mac")) {
            // macOS: /Library/Application Support/DigitalLibrary/repository
            return "/Library/Application Support/" + APP_NAME + "/" + REPOSITORY_FOLDER;
            
        } else {
            // Linux: /var/lib/digitallibrary/repository
            return "/var/lib/" + APP_NAME.toLowerCase() + "/" + REPOSITORY_FOLDER;
        }
    }
    
    /**
     * Crea una directory se non esiste.
     */
    private void ensureDirectoryExists(String path) {
        File dir = new File(path);
        if (!dir.exists()) {
            boolean created = dir.mkdirs();
            if (created) {
                logger.info("Created directory: {}", path);
            } else {
                logger.warn("Could not create directory at: {}", path);
            }
        }
    }
    
    /**
     * Setup automatico del repository centrale.
     * Crea la struttura di cartelle necessaria e gestisce i permessi.
     */
    private void setupCentralRepository() {
        File repoDir = new File(centralRepositoryPath);
        
        if (!repoDir.exists()) {
            logger.info("========================================");
            logger.info("Digital Library - First Time Setup");
            logger.info("========================================");
            logger.info("Creating central repository at: {}", centralRepositoryPath);
            
            boolean created = repoDir.mkdirs();
            
            if (created) {
                logger.info("[OK] Central repository created successfully!");
                
                // Crea le sottocartelle standard
                createStandardSubfolders();
                
                // Su Windows, prova a configurare i permessi (best effort)
                if (isProduction && System.getProperty("os.name").toLowerCase().contains("win")) {
                    configureWindowsPermissions();
                }
                
                logger.info("========================================");
                logger.info("Setup completed successfully!");
                logger.info("========================================");
            } else {
                // Se non riusciamo a creare in modalità production, prova fallback
                if (isProduction) {
                    logger.warn("Could not create repository in system location.");
                    handleProductionFallback();
                } else {
                    logger.error("Could not create repository at: {}", centralRepositoryPath);
                }
            }
        } else {
            // Repository già esistente - verifica le sottocartelle
            createStandardSubfolders();
            logger.info("Using existing repository at: {}", centralRepositoryPath);
        }
    }
    
    /**
     * Crea le sottocartelle standard per i tipi di file supportati.
     */
    private void createStandardSubfolders() {
        String[] subfolders = {"pdf", "epub", "txt"};
        for (String folder : subfolders) {
            ensureDirectoryExists(centralRepositoryPath + File.separator + folder);
        }
    }
    
    /**
     * Configura i permessi su Windows (best effort).
     * Questo metodo tenta di dare permessi di lettura/scrittura al gruppo Users.
     */
    private void configureWindowsPermissions() {
        try {
            // Esegui icacls per dare permessi al gruppo Users
            ProcessBuilder processBuilder = new ProcessBuilder(
                "icacls", 
                centralRepositoryPath, 
                "/grant", 
                "Users:(OI)(CI)M", 
                "/T"
            );
            Process process = processBuilder.start();
            int exitCode = process.waitFor();
            
            if (exitCode == 0) {
                logger.info("[OK] Windows permissions configured successfully");
            } else {
                logger.info("Could not configure permissions automatically (may require admin rights)");
            }
        } catch (IOException | InterruptedException e) {
            // Permessi non configurati - non è critico, continua comunque
            logger.info("Permissions configuration skipped: {}", e.getMessage());
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
        }
    }
    
    /**
     * Gestisce il fallback in modalità production quando non è possibile creare
     * il repository nella posizione di sistema.
     */
    private void handleProductionFallback() {
        logger.info("Attempting fallback to user home directory...");
        
        // Fallback: usa la home directory dell'utente
        String userHome = System.getProperty("user.home");
        String fallbackPath = userHome + File.separator + ".digitallibrary" + File.separator + "repository";
        
        File fallbackDir = new File(fallbackPath);
        if (fallbackDir.mkdirs() || fallbackDir.exists()) {
            logger.info("[OK] Using fallback repository at: {}", fallbackPath);
            // Aggiorna il percorso del repository
            this.centralRepositoryPath = fallbackPath;
            createStandardSubfolders();
        } else {
            logger.error("Could not create repository even in fallback location!");
            logger.error("Please check file system permissions.");
        }
    }
    
    /**
     * Restituisce true se l'applicazione è in modalità production.
     */
    public boolean isProductionMode() {
        return isProduction;
    }
    
    /**
     * Restituisce il percorso della cartella per un tipo di file specifico.
     * 
     * @param fileType tipo di file ("epub", "pdf", "txt", ecc.)
     * @return percorso della sottocartella nel repository
     */
    public String getRepositorySubfolder(String fileType) {
        String subfolder = centralRepositoryPath + File.separator + fileType.toLowerCase();
        ensureDirectoryExists(subfolder);
        return subfolder;
    }
    
    /**
     * Sincronizza i file dalla cartella library-data al repository centrale.
     * Questo metodo viene chiamato automaticamente in modalità production per copiare
     * i file di esempio dalla cartella di sviluppo al repository di sistema.
     * 
     * Scansiona le sottocartelle (pdf, epub, txt) in library-data e copia i file
     * che non sono già presenti nel repository centrale.
     */
    private void syncFromLibraryData() {
        File libraryDataDir = new File("library-data");
        
        // Verifica se la cartella library-data esiste
        if (!libraryDataDir.exists() || !libraryDataDir.isDirectory()) {
            logger.info("library-data not found, skipping sync.");
            return;
        }
        
        logger.info("========================================");
        logger.info("Syncing files from library-data...");
        logger.info("========================================");
        
        int copiedCount = 0;
        int skippedCount = 0;
        
        // Scansiona le sottocartelle standard
        String[] subfolders = {"pdf", "epub", "txt"};
        for (String folder : subfolders) {
            File sourceFolder = new File(libraryDataDir, folder);
            
            if (!sourceFolder.exists() || !sourceFolder.isDirectory()) {
                continue;
            }
            
            // Ottieni tutti i file nella sottocartella
            File[] files = sourceFolder.listFiles();
            if (files == null) {
                continue;
            }
            
            for (File file : files) {
                if (!file.isFile()) {
                    continue; // Salta le directory
                }
                
                // Costruisci il percorso relativo
                String relativeFilePath = folder + File.separator + file.getName();
                
                // Verifica se il file esiste già nel repository centrale
                if (existsInRepository(relativeFilePath)) {
                    logger.info("[SKIP] {} (already exists)", relativeFilePath);
                    skippedCount++;
                    continue;
                }
                
                // Copia il file nel repository centrale
                try {
                    String destPath = saveToRepository(file, relativeFilePath);
                    logger.info("[COPY] {} -> {}", relativeFilePath, destPath);
                    copiedCount++;
                } catch (IOException e) {
                    logger.error("Failed to copy {}: {}", relativeFilePath, e.getMessage());
                }
            }
        }
        
        logger.info("========================================");
        logger.info("Sync completed!");
        logger.info("Files copied: {}", copiedCount);
        logger.info("Files skipped: {}", skippedCount);
        logger.info("========================================");
    }
    
    /**
     * Sincronizza manualmente i file da library-data al repository centrale.
     * Questo metodo può essere chiamato pubblicamente per forzare una sincronizzazione.
     * 
     * @return numero di file copiati
     */
    public int manualSyncFromLibraryData() {
        File libraryDataDir = new File("library-data");
        
        if (!libraryDataDir.exists() || !libraryDataDir.isDirectory()) {
            logger.error("library-data folder not found.");
            return 0;
        }
        
        int copiedCount = 0;
        String[] subfolders = {"pdf", "epub", "txt"};
        
        for (String folder : subfolders) {
            File sourceFolder = new File(libraryDataDir, folder);
            
            if (!sourceFolder.exists() || !sourceFolder.isDirectory()) {
                continue;
            }
            
            File[] files = sourceFolder.listFiles();
            if (files == null) {
                continue;
            }
            
            for (File file : files) {
                if (!file.isFile()) {
                    continue;
                }
                
                String relativeFilePath = folder + File.separator + file.getName();
                
                if (!existsInRepository(relativeFilePath)) {
                    try {
                        saveToRepository(file, relativeFilePath);
                        copiedCount++;
                    } catch (IOException e) {
                        logger.error("Failed to copy {}: {}", relativeFilePath, e.getMessage());
                    }
                }
            }
        }
        
        return copiedCount;
    }
}
