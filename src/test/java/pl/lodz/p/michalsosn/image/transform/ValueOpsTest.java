package pl.lodz.p.michalsosn.image.transform;

import org.junit.Test;
import pl.lodz.p.michalsosn.image.image.Image;
import pl.lodz.p.michalsosn.image.io.BufferedImageIO;
import pl.lodz.p.michalsosn.image.io.ImageSet;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.function.UnaryOperator;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static pl.lodz.p.michalsosn.util.Lift.lift;
import static pl.lodz.p.michalsosn.image.transform.ValueOps.*;

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

        for (UnaryOperator<Image> valueOperation : valueOperations) {
            ImageSet.listImages(ImageSet.ALL).forEach(path -> {
                try {
                    Image image = BufferedImageIO.readImage(path);
                    valueOperation.apply(image);
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

        for (UnaryOperator<Image> operation : operations) {
            ImageSet.listImages(ImageSet.ALL).forEach(path -> {
                try {
                    Image image = BufferedImageIO.readImage(path);

                    Image transformedImage = operation.apply(image);
                    Image recoveredImage = operation.apply(transformedImage);

                    assertThat(recoveredImage, is(image));
                } catch (IOException ex) {
                    throw new AssertionError("IO operation failed", ex);
                }
            });
        }
    }

}