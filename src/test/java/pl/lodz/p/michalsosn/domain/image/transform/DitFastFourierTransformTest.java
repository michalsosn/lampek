package pl.lodz.p.michalsosn.domain.image.transform;

import org.junit.Test;
import pl.lodz.p.michalsosn.domain.image.channel.*;
import pl.lodz.p.michalsosn.domain.image.spectrum.Spectrum2d;
import pl.lodz.p.michalsosn.io.BufferedImageIO;
import pl.lodz.p.michalsosn.io.ResourceSet;
import pl.lodz.p.michalsosn.util.Maps;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import java.util.stream.Stream;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author Michał Sośnicki
 */
public class DitFastFourierTransformTest {

    @Test
    public void testInverse() throws Exception {
        try (Stream<Path> paths = ResourceSet.listResources(ResourceSet.IMAGES)) {
            paths.forEach(path -> {
                try {
                    Image image = BufferedImageIO.readImage(path);

                    Map<String, Channel> channels = image.getChannels();
                    Map<String, Spectrum2d> transformed = Maps.applyToValues(
                            channels, DitFastFourierTransform::transform
                    );
                    Map<String, Channel> inverted = Maps.applyToValues(
                            transformed, DitFastFourierTransform::inverse
                    );

                    Image recovered = image.accept(ImageVisitor.imageVisitor(
                            grayImage -> GrayImage.fromChannels(inverted),
                            rgbImage -> RgbImage.fromChannels(inverted)
                    ));

                    assertThat(recovered, is(image));
                } catch (IOException ex) {
                    throw new AssertionError("IO operation failed", ex);
                }
            });
        }

    }

}