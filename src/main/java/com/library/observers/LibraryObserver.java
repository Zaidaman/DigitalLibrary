package com.library.observers;

import com.library.models.Libraries;

/**
 * Interfaccia Observer per reagire ai cambiamenti nelle librerie.
 * Design Pattern: Observer
 */
public interface LibraryObserver {
    /**
     * Notifica quando una nuova libreria viene aggiunta
     * @param library La libreria aggiunta
     */
    void onLibraryAdded(Libraries library);
    
    /**
     * Notifica quando una libreria viene eliminata
     * @param library La libreria eliminata
     */
    void onLibraryDeleted(Libraries library);
    
    /**
     * Notifica quando una libreria viene condivisa con un utente
     * @param library La libreria condivisa
     * @param username L'username dell'utente con cui è stata condivisa
     */
    void onLibraryShared(Libraries library, String username);
}
