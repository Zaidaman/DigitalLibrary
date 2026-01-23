package com.library;

/**
 * Launcher class per JavaFX fat JAR
 * Questa classe serve per aggirare il problema del module system di JavaFX
 * quando si crea un JAR eseguibile con tutte le dipendenze incluse.
 */
public class Launcher {
    public static void main(String[] args) {
        Main.main(args);
    }
}
