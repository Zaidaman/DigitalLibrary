package com.library.observers;

import com.library.models.Libraries;
import java.util.ArrayList;
import java.util.List;

/**
 * Subject che gestisce gli observer delle librerie.
 * Notifica gli observer quando avvengono cambiamenti alle librerie.
 * Design Pattern: Observer
 */
public class LibrarySubject {
    private final List<LibraryObserver> observers = new ArrayList<>();
    
    /**
     * Registra un nuovo observer
     * @param observer L'observer da aggiungere
     */
    public void addObserver(LibraryObserver observer) {
        if (!observers.contains(observer)) {
            observers.add(observer);
        }
    }
    
    /**
     * Rimuove un observer
     * @param observer L'observer da rimuovere
     */
    public void removeObserver(LibraryObserver observer) {
        observers.remove(observer);
    }
    
    /**
     * Notifica tutti gli observer che una libreria è stata aggiunta
     * @param library La libreria aggiunta
     */
    public void notifyLibraryAdded(Libraries library) {
        observers.forEach(obs -> obs.onLibraryAdded(library));
    }
    
    /**
     * Notifica tutti gli observer che una libreria è stata eliminata
     * @param library La libreria eliminata
     */
    public void notifyLibraryDeleted(Libraries library) {
        observers.forEach(obs -> obs.onLibraryDeleted(library));
    }
    
    /**
     * Notifica tutti gli observer che una libreria è stata condivisa
     * @param library La libreria condivisa
     * @param username L'username dell'utente con cui è stata condivisa
     */
    public void notifyLibraryShared(Libraries library, String username) {
        observers.forEach(obs -> obs.onLibraryShared(library, username));
    }
}
