@echo off
REM Script di configurazione per l'ambiente di produzione di DigitalLibrary
REM Questo script crea la cartella del repository centrale e configura i permessi

echo ========================================
echo Digital Library - Production Setup
echo ========================================
echo.

REM Controlla se l'utente ha privilegi di amministratore
net session >nul 2>&1
if %errorLevel% neq 0 (
    echo ERRORE: Questo script richiede privilegi di amministratore.
    echo Esegui questo script come amministratore.
    pause
    exit /b 1
)

REM Imposta il percorso del repository centrale
set REPO_PATH=%ProgramData%\DigitalLibrary\repository

echo Creazione repository centrale in: %REPO_PATH%
echo.

REM Crea la directory del repository
if not exist "%REPO_PATH%" (
    mkdir "%REPO_PATH%"
    echo [OK] Repository centrale creato
) else (
    echo [INFO] Repository centrale già esistente
)

REM Crea le sottocartelle per i diversi tipi di file
mkdir "%REPO_PATH%\pdf" 2>nul
mkdir "%REPO_PATH%\epub" 2>nul
mkdir "%REPO_PATH%\txt" 2>nul

echo [OK] Sottocartelle create (pdf, epub, txt)
echo.

REM Imposta i permessi (tutti gli utenti possono leggere e scrivere)
echo Configurazione permessi...
icacls "%REPO_PATH%" /grant Users:(OI)(CI)M /T >nul
echo [OK] Permessi configurati
echo.

REM Migrazione dati esistenti da library-data (se presente)
if exist "library-data" (
    echo Trovata cartella library-data esistente.
    set /p MIGRATE="Vuoi migrare i dati esistenti al repository centrale? (S/N): "
    if /i "%MIGRATE%"=="S" (
        echo Migrazione in corso...
        xcopy /E /I /Y "library-data\*" "%REPO_PATH%\" >nul
        echo [OK] Dati migrati con successo
    )
)

echo.
echo ========================================
echo Setup completato con successo!
echo ========================================
echo.
echo Repository centrale: %REPO_PATH%
echo.
echo Puoi ora distribuire l'applicazione come JAR/EXE.
echo.
pause
