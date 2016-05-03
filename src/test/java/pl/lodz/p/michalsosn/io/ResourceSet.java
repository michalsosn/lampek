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
public enum ResourceSet {
    IMAGES("/images"), BINARY("/images/binary"),
    COLOR("/images/color"), COLOR_NOISE("/images/color-noise"),
    GRAY("/images/gray"), GRAY_NOISE("/images/gray-noise"),
    SOUNDS("/sounds"), ARTIFICIAL("/sounds/artificial"),
    NATURAL("/sounds/natural"), SEQ("/sounds/seq");

    private final String resource;

    ResourceSet(String resource) {
        this.resource = resource;
    }

    public URI getUri() {
        try {
            return BufferedImageIOTest.class.getResource(resource).toURI();
        } catch (URISyntaxException ex) {
            throw new IllegalStateException("Can't get URI for an image set", ex);
        }
    }

    public static Stream<Path> listResources(ResourceSet resourceSet) throws IOException {
        Path root = Paths.get(resourceSet.getUri());
        return Files.walk(root).filter(Files::isRegularFile);
    }

    public static Path tempResource() throws IOException {
        Path tempPath = Files.createTempFile("lampek", null);
        tempPath.toFile().deleteOnExit();
        return tempPath;
    }
}
