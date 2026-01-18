package com.library.dao;

/**
 * Factory Singleton per la creazione e gestione centralizzata dei DAO.
 * Garantisce un'unica istanza di ogni DAO per tutta l'applicazione.
 * 
 * Design Pattern: Singleton + Factory
 */
public class DAOFactory {
    private static DAOFactory instance;
    
    private final AuthorDAO authorDAO;
    private final BookDAO bookDAO;
    private final GenreDAO genreDAO;
    private final LibrariesDAO librariesDAO;
    private final LibUserDAO libUserDAO;
    private final BookLibDAO bookLibDAO;
    private final BookGenreDAO bookGenreDAO;
    private final LibAccessDAO libAccessDAO;
    
    /**
     * Costruttore privato per impedire istanziazione esterna
     */
    private DAOFactory() {
        this.authorDAO = new AuthorDAO();
        this.bookDAO = new BookDAO();
        this.genreDAO = new GenreDAO();
        this.librariesDAO = new LibrariesDAO();
        this.libUserDAO = new LibUserDAO();
        this.bookLibDAO = new BookLibDAO();
        this.bookGenreDAO = new BookGenreDAO();
        this.libAccessDAO = new LibAccessDAO();
    }
    
    /**
     * Restituisce l'unica istanza di DAOFactory
     * Thread-safe
     * @return L'istanza singleton di DAOFactory
     */
    public static synchronized DAOFactory getInstance() {
        if (instance == null) {
            instance = new DAOFactory();
        }
        return instance;
    }
    
    // Getters per i vari DAO
    public AuthorDAO getAuthorDAO() { return authorDAO; }
    public BookDAO getBookDAO() { return bookDAO; }
    public GenreDAO getGenreDAO() { return genreDAO; }
    public LibrariesDAO getLibrariesDAO() { return librariesDAO; }
    public LibUserDAO getLibUserDAO() { return libUserDAO; }
    public BookLibDAO getBookLibDAO() { return bookLibDAO; }
    public BookGenreDAO getBookGenreDAO() { return bookGenreDAO; }
    public LibAccessDAO getLibAccessDAO() { return libAccessDAO; }
}
