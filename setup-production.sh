#!/bin/bash
# Script di configurazione per l'ambiente di produzione di DigitalLibrary (Linux/macOS)
# Questo script crea la cartella del repository centrale e configura i permessi

echo "========================================"
echo "Digital Library - Production Setup"
echo "========================================"
echo ""

# Controlla se l'utente ha privilegi di root
if [ "$EUID" -ne 0 ]; then 
    echo "ERRORE: Questo script richiede privilegi di root."
    echo "Esegui con: sudo ./setup-production.sh"
    exit 1
fi

# Determina il sistema operativo
if [[ "$OSTYPE" == "darwin"* ]]; then
    # macOS
    REPO_PATH="/Library/Application Support/DigitalLibrary/repository"
else
    # Linux
    REPO_PATH="/var/lib/digitallibrary/repository"
fi

echo "Creazione repository centrale in: $REPO_PATH"
echo ""

# Crea la directory del repository
if [ ! -d "$REPO_PATH" ]; then
    mkdir -p "$REPO_PATH"
    echo "[OK] Repository centrale creato"
else
    echo "[INFO] Repository centrale già esistente"
fi

# Crea le sottocartelle per i diversi tipi di file
mkdir -p "$REPO_PATH/pdf"
mkdir -p "$REPO_PATH/epub"
mkdir -p "$REPO_PATH/txt"

echo "[OK] Sottocartelle create (pdf, epub, txt)"
echo ""

# Imposta i permessi (tutti gli utenti possono leggere e scrivere)
echo "Configurazione permessi..."
chmod -R 755 "$REPO_PATH"
chown -R root:users "$REPO_PATH"
echo "[OK] Permessi configurati"
echo ""

# Migrazione dati esistenti da library-data (se presente)
if [ -d "library-data" ]; then
    echo "Trovata cartella library-data esistente."
    read -p "Vuoi migrare i dati esistenti al repository centrale? (s/n): " MIGRATE
    if [[ "$MIGRATE" == "s" || "$MIGRATE" == "S" ]]; then
        echo "Migrazione in corso..."
        cp -r library-data/* "$REPO_PATH/"
        echo "[OK] Dati migrati con successo"
    fi
fi

echo ""
echo "========================================"
echo "Setup completato con successo!"
echo "========================================"
echo ""
echo "Repository centrale: $REPO_PATH"
echo ""
echo "Puoi ora distribuire l'applicazione come JAR."
echo ""
