package pl.lodz.p.michalsosn.domain.image.transform;

import org.junit.Test;
import pl.lodz.p.michalsosn.domain.image.image.Image;
import pl.lodz.p.michalsosn.io.BufferedImageIO;
import pl.lodz.p.michalsosn.io.ImageSet;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static pl.lodz.p.michalsosn.domain.image.transform.ValueOps.*;
import static pl.lodz.p.michalsosn.domain.util.Lift.lift;

/**
 * @author Michał Sośnicki
 */
public class ValueOpsTest {

    @Test
    public void testOpsDontCrash() throws Exception {
        List<UnaryOperator<Image>> valueOperations = Arrays.asList(
                lift(lift(negate())),
                lift(lift(precalculating(ValueOps.negate()))),
                lift(lift(changeBrightness(50))),
                lift(lift(changeBrightness(0))),
                lift(lift(changeBrightness(-100))),
                lift(lift(changeContrast(0.5))),
                lift(lift(changeContrast(1))),
                lift(lift(changeContrast(20))),
                lift(lift(clipBelow(40)))
        );

        try (Stream<Path> paths = ImageSet.listImages(ImageSet.ALL)) {
            paths.forEach(path -> {
                try {
                    Image image = BufferedImageIO.readImage(path);
                    valueOperations.forEach(operation -> operation.apply(image));
                } catch (IOException ex) {
                    throw new AssertionError("IO operation failed", ex);
                }
            });
        }
    }

    @Test
    public void testOpsSelfInverse() throws Exception {
        List<UnaryOperator<Image>> operations = Arrays.asList(
                lift(lift(negate())),
                lift(lift(precalculating(ValueOps.negate())))
        );

        try (Stream<Path> paths = ImageSet.listImages(ImageSet.ALL)) {
            paths.forEach(path -> {
                try {
                    Image image = BufferedImageIO.readImage(path);

                    for (UnaryOperator<Image> operation : operations) {
                        Image transformedImage = operation.apply(image);
                        Image recoveredImage = operation.apply(transformedImage);

                        assertThat(recoveredImage, is(image));
                    }
                } catch (IOException ex) {
                    throw new AssertionError("IO operation failed", ex);
                }
            });
        }
    }

}