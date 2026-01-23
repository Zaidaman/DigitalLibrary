package com.library.utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RepositoryManager {
    
    private static final Logger logger = LoggerFactory.getLogger(RepositoryManager.class);
    private static final String APP_NAME = "DigitalLibrary";
    private static final String REPOSITORY_FOLDER = "repository";
    
    private static RepositoryManager instance;
    private String centralRepositoryPath;
    private final boolean isProduction;
    
    private RepositoryManager() {
        File devRepo = new File("library-data");
        this.isProduction = !devRepo.exists() || System.getProperty("app.production") != null;
        
        if (isProduction) {
            this.centralRepositoryPath = getProductionRepositoryPath();
        } else {
            this.centralRepositoryPath = devRepo.getAbsolutePath();
        }
        
        setupCentralRepository();
        
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
    
    public String getCentralRepositoryPath() {
        return centralRepositoryPath;
    }
    
    public String getCentralFilePath(String relativeFilePath) {
        return centralRepositoryPath + File.separator + relativeFilePath;
    }
    
    public String saveToRepository(File sourceFile, String relativeDestPath) throws IOException {
        String destPath = getCentralFilePath(relativeDestPath);
        File destFile = new File(destPath);
        
        destFile.getParentFile().mkdirs();
        
        // Copia il file
        Files.copy(
            sourceFile.toPath(),
            destFile.toPath(),
            StandardCopyOption.REPLACE_EXISTING
        );
        
        return destFile.getAbsolutePath();
    }
    
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
        
        destFile.getParentFile().mkdirs();
        
        // Copia il file
        Files.copy(
            sourceFile.toPath(),
            destFile.toPath(),
            StandardCopyOption.REPLACE_EXISTING
        );
        
        return destFile.getAbsolutePath();
    }
    
    public String resolveFilePath(String relativeFilePath, String userBasePath) {
        if (userBasePath != null && !userBasePath.trim().isEmpty()) {
            String userFilePath = userBasePath + File.separator + relativeFilePath;
            File userFile = new File(userFilePath);
            if (userFile.exists()) {
                return userFile.getAbsolutePath();
            }
        }
        
        String centralFilePath = getCentralFilePath(relativeFilePath);
        File centralFile = new File(centralFilePath);
        if (centralFile.exists()) {
            if (userBasePath != null && !userBasePath.trim().isEmpty()) {
                try {
                    return copyToUserFolder(relativeFilePath, userBasePath);
                } catch (IOException e) {
                    logger.warn("Could not copy file to user folder: {}", e.getMessage());
                    return centralFilePath;
                }
            }
            return centralFilePath;
        }
        
        logger.error("File not found: {}", relativeFilePath);
        return null;
    }
    
    public boolean existsInRepository(String relativeFilePath) {
        String centralPath = getCentralFilePath(relativeFilePath);
        return new File(centralPath).exists();
    }
    
    public boolean existsInUserFolder(String relativeFilePath, String userBasePath) {
        if (userBasePath == null || userBasePath.trim().isEmpty()) {
            return false;
        }
        String userPath = userBasePath + File.separator + relativeFilePath;
        return new File(userPath).exists();
    }
    
    private String getProductionRepositoryPath() {
        String os = System.getProperty("os.name").toLowerCase();
        
        if (os.contains("win")) {
            String programData = System.getenv("ProgramData");
            if (programData == null || programData.isEmpty()) {
                programData = "C:\\ProgramData";
            }
            return programData + File.separator + APP_NAME + File.separator + REPOSITORY_FOLDER;
            
        } else if (os.contains("mac")) {
            return "/Library/Application Support/" + APP_NAME + "/" + REPOSITORY_FOLDER;
            
        } else {
            return "/var/lib/" + APP_NAME.toLowerCase() + "/" + REPOSITORY_FOLDER;
        }
    }
    
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
                
                createStandardSubfolders();
                
                if (isProduction && System.getProperty("os.name").toLowerCase().contains("win")) {
                    configureWindowsPermissions();
                }
                
                logger.info("========================================");
                logger.info("Setup completed successfully!");
                logger.info("========================================");
            } else {
                if (isProduction) {
                    logger.warn("Could not create repository in system location.");
                    handleProductionFallback();
                } else {
                    logger.error("Could not create repository at: {}", centralRepositoryPath);
                }
            }
        } else {
            createStandardSubfolders();
            logger.info("Using existing repository at: {}", centralRepositoryPath);
        }
    }
    
    private void createStandardSubfolders() {
        String[] subfolders = {"pdf", "epub", "txt"};
        for (String folder : subfolders) {
            ensureDirectoryExists(centralRepositoryPath + File.separator + folder);
        }
    }
    
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
            logger.info("Permissions configuration skipped: {}", e.getMessage());
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
        }
    }
    
    private void handleProductionFallback() {
        logger.info("Attempting fallback to user home directory...");
        
        String userHome = System.getProperty("user.home");
        String fallbackPath = userHome + File.separator + ".digitallibrary" + File.separator + "repository";
        
        File fallbackDir = new File(fallbackPath);
        if (fallbackDir.mkdirs() || fallbackDir.exists()) {
            logger.info("[OK] Using fallback repository at: {}", fallbackPath);
            this.centralRepositoryPath = fallbackPath;
            createStandardSubfolders();
        } else {
            logger.error("Could not create repository even in fallback location!");
            logger.error("Please check file system permissions.");
        }
    }
    
    public boolean isProductionMode() {
        return isProduction;
    }
    
    public String getRepositorySubfolder(String fileType) {
        String subfolder = centralRepositoryPath + File.separator + fileType.toLowerCase();
        ensureDirectoryExists(subfolder);
        return subfolder;
    }
    
    private void syncFromLibraryData() {
        File libraryDataDir = new File("library-data");
        
        if (!libraryDataDir.exists() || !libraryDataDir.isDirectory()) {
            logger.info("library-data not found, skipping sync.");
            return;
        }
        
        logger.info("========================================");
        logger.info("Syncing files from library-data...");
        logger.info("========================================");
        
        int copiedCount = 0;
        int skippedCount = 0;
        
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
                
                if (existsInRepository(relativeFilePath)) {
                    logger.info("[SKIP] {} (already exists)", relativeFilePath);
                    skippedCount++;
                    continue;
                }
                
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
