package com.nz.jnawintools.utils;

import com.sun.jna.platform.win32.KnownFolders;
import com.sun.jna.platform.win32.Shell32Util;

import java.nio.file.Path;
import java.nio.file.Paths;

public class WindowsFilesUtils {

    public static Path getDocuments(){
        return Paths.get(
            Shell32Util.getKnownFolderPath(KnownFolders.FOLDERID_Documents
            ));
    }
}
