package com.library.dao;

import java.util.List;

/**
 * Interfaccia base per tutti i DAO del sistema.
 * Definisce le operazioni CRUD standard.
 * 
 * @param <T> Il tipo di entità gestita dal DAO
 */
public interface BaseDAO<T> {
    /**
     * Inserisce una nuova entità nel database
     * @param entity L'entità da inserire
     * @return L'ID generato per la nuova entità, -1 in caso di errore
     */
    int insert(T entity);
    
    /**
     * Recupera tutte le entità dal database
     * @return Lista di tutte le entità
     */
    List<T> findAll();
    
    /**
     * Trova un'entità per ID
     * @param id L'ID dell'entità da cercare
     * @return L'entità trovata, null se non esiste
     */
    T findById(int id);
    
    /**
     * Elimina un'entità dal database
     * @param id L'ID dell'entità da eliminare
     */
    void delete(int id);
}
