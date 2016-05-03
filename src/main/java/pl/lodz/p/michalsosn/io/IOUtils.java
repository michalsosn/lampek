package pl.lodz.p.michalsosn.io;

import java.nio.file.Path;

/**
 * @author Michał Sośnicki
 */
public final class IOUtils {

    private IOUtils() {
    }

    public static String separateExtension(Path path) {
        String fileName = path.getFileName().toString();
        int formatStart = fileName.lastIndexOf('.');
        if (formatStart < 0) {
            throw new IllegalArgumentException(path + " has no extension.");
        }
        return fileName.substring(formatStart + 1);
    }

}
