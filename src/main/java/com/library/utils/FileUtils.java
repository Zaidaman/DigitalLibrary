package com.library.utils;

import java.io.File;

public class FileUtils {
    public static String getExtension(File file) {
        String name = file.getName();
        int lastDot = name.lastIndexOf('.');
        return lastDot > 0 ? name.substring(lastDot + 1).toLowerCase() : "";
    }
}
