package pl.lodz.p.michalsosn.image.io;

import org.junit.Test;
import pl.lodz.p.michalsosn.image.image.Image;
import pl.lodz.p.michalsosn.image.image.ImageVisitor;
import pl.lodz.p.michalsosn.util.Lift;

import java.io.IOException;
import java.nio.file.Path;
import java.util.function.IntUnaryOperator;

import static pl.lodz.p.michalsosn.image.io.BufferedImageIO.*;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author Michał Sośnicki
 */
public class BufferedImageIOTest {

    @Test
    public void testReadImage() throws Exception {
        ImageSet.listImages(ImageSet.ALL).forEach(path -> {
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

    @Test
    public void testReadWriteImage() throws Exception {
        ImageSet.listImages(ImageSet.ALL).forEach(path -> {
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
    }

}