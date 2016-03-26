package pl.lodz.p.michalsosn.io;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

/**
 * @author Michał Sośnicki
 */
public enum ImageSet {
    ALL("/images"), BINARY("/images/binary"),
    COLOR("/images/color"), COLOR_NOISE("/images/color-noise"),
    GRAY("/images/gray"), GRAY_NOISE("/images/gray-noise");

    private final String resource;

    ImageSet(String resource) {
        this.resource = resource;
    }

    public URI getUri() {
        try {
            return BufferedImageIOTest.class.getResource(resource).toURI();
        } catch (URISyntaxException ex) {
            throw new IllegalStateException("Can't get URI for an image set", ex);
        }
    }

    public static Stream<Path> listImages(ImageSet imageSet) throws IOException {
        Path root = Paths.get(imageSet.getUri());
        return Files.walk(root).filter(Files::isRegularFile);
    }

    public static Path tempImage() throws IOException {
        Path tempPath = Files.createTempFile("lampek", null);
        tempPath.toFile().deleteOnExit();
        return tempPath;
    }
}
