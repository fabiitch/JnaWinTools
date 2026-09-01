package com.nz.jnawintools.utils;

import com.nz.jnawintools.win32.Shell32;

import java.nio.file.Path;
import java.nio.file.Paths;

public class WindowsFilesUtils {

    public static Path getDocuments() {
        return Paths.get(Shell32.getDocumentsPath());
    }
}
