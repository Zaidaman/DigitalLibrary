package com.library.dao;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.library.models.Book;
import com.library.utils.DbUtils;

import javafx.scene.web.WebView;
import nl.siegmann.epublib.domain.Resource;
import nl.siegmann.epublib.epub.EpubReader;

public class BookDAO {

    // Inserisce un nuovo libro nel DB con percorso file e restituisce l'id generato
    public int insert(Book book, int idAuthor, int annoPub) {
        String sql = "INSERT INTO Book (Title, IdAuthor, AnnoPub, BookFile) VALUES (?, ?, ?, ?)";
        try (Connection conn = DbUtils.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, book.getTitle());
            ps.setInt(2, idAuthor);
            ps.setInt(3, annoPub);
            ps.setString(4, book.getFilePath());
            ps.executeUpdate();
            
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return -1;
    }

    // Carica EPUB dentro WebView
    public void loadEpubInWebView(String epubFilePath, WebView webView) {
        try {
            // QUI usiamo il nome COMPLETO per evitare conflitti
            nl.siegmann.epublib.domain.Book epub =
                    new EpubReader().readEpub(new FileInputStream(epubFilePath));

            String basePath = "tmp/epub_view/";
            File baseDir = new File(basePath);
            baseDir.mkdirs();

            for (Resource res : epub.getResources().getAll()) {
                File out = new File(basePath + res.getHref());
                out.getParentFile().mkdirs();
                    try (FileOutputStream fos = new FileOutputStream(out)) {
                        fos.write(res.getData());
                    }
            }

            String firstChapter = epub.getContents().get(0).getHref();
            File chapterFile = new File(basePath + firstChapter);

            webView.getEngine().load(chapterFile.toURI().toString());

        } catch (IOException e) {
            Logger.getLogger(BookDAO.class.getName()).log(Level.SEVERE, "Errore IO durante il caricamento EPUB", e);
        } catch (Exception e) {
            Logger.getLogger(BookDAO.class.getName()).log(Level.SEVERE, "Errore generico durante il caricamento EPUB", e);
        }
    }

    // Restituisce tutti i libri
    public List<Book> findAll() {
        List<Book> books = new ArrayList<>();
        String sql = "SELECT IdBook, Title, IdAuthor, AnnoPub, BookFile FROM Book";
        try (Connection conn = DbUtils.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Book book = new Book(
                        rs.getString("Title"),
                        null,
                        null,
                        rs.getString("BookFile"),
                        -1
                );
                books.add(book);
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return books;
    }

    // Restituisce libri per libreria
    public List<Book> findByLibraryId(int idLibrary) {
        List<Book> books = new ArrayList<>();
        String sql =
                "SELECT b.IdBook, b.Title, b.IdAuthor, b.AnnoPub, b.BookFile " +
                "FROM Book b " +
                "JOIN BookLib bl ON b.IdBook = bl.IdBook " +
                "WHERE bl.IdLibrary = ?";

        try (Connection conn = DbUtils.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idLibrary);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Book book = new Book(
                        rs.getString("Title"),
                        null,
                        null,
                        rs.getString("BookFile"),
                        idLibrary
                );
                books.add(book);
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return books;
    }

    // Trova un libro tramite il titolo
    public Book findByTitle(String title) {
        String sql = "SELECT IdBook, Title, IdAuthor, AnnoPub, BookFile FROM Book WHERE Title = ?";
        try (Connection conn = DbUtils.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, title);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new Book(
                        rs.getString("Title"),
                        null,
                        null,
                        rs.getString("BookFile"),
                        -1
                );
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    // Trova l'ID di un libro tramite il titolo
    public int findIdByTitle(String title) {
        String sql = "SELECT IdBook FROM Book WHERE Title = ?";
        try (Connection conn = DbUtils.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, title);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt("IdBook");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return -1;
    }
}