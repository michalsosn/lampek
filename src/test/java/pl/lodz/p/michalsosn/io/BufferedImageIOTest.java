package pl.lodz.p.michalsosn.io;

import org.junit.Test;
import pl.lodz.p.michalsosn.domain.image.image.Image;
import pl.lodz.p.michalsosn.domain.image.image.ImageVisitor;
import pl.lodz.p.michalsosn.domain.utils.Lift;

import java.io.IOException;
import java.nio.file.Path;
import java.util.function.IntUnaryOperator;
import java.util.stream.Stream;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static pl.lodz.p.michalsosn.io.BufferedImageIO.readImage;
import static pl.lodz.p.michalsosn.io.BufferedImageIO.writeImage;

/**
 * @author Michał Sośnicki
 */
public class BufferedImageIOTest {

    @Test
    public void testReadImage() throws Exception {
        try (Stream<Path> paths = ImageSet.listImages(ImageSet.ALL)) {
            paths.forEach(path -> {
                try {
                    System.out.println("Test reading: " + path);
                    Image image = readImage(path);

                    String type = image.accept(ImageVisitor.imageVisitor(
                            grayImage -> "Gray", rgbImage -> "RGB"
                    ));
                    System.out.println("Image type: " + type);

                    image.map(Lift.lift(IntUnaryOperator.identity())); // dummy op
                } catch (IOException ex) {
                    throw new AssertionError("Reading failed", ex);
                }
            });
        }
    }

    @Test
    public void testReadWriteImage() throws Exception {
        try (Stream<Path> paths = ImageSet.listImages(ImageSet.ALL)) {
            paths.forEach(path -> {
                try {
                    System.out.println("Test writing: " + path);
                    Image image = readImage(path);

                    Path writePath = ImageSet.tempImage();
                    writeImage(image, writePath, "png");

                    Image recovered = readImage(writePath);

                    assertThat(recovered, is(image));
                } catch (IOException ex) {
                    throw new AssertionError("IO operation failed", ex);
                }
            });
        };
    }

}