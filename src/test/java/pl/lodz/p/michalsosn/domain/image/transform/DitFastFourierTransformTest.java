package pl.lodz.p.michalsosn.domain.image.transform;

import org.junit.Test;
import pl.lodz.p.michalsosn.domain.image.channel.*;
import pl.lodz.p.michalsosn.domain.image.spectrum.Spectrum;
import pl.lodz.p.michalsosn.io.BufferedImageIO;
import pl.lodz.p.michalsosn.io.ImageSet;
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
    public void textInverse() throws Exception {
        try (Stream<Path> paths = ImageSet.listImages(ImageSet.ALL)) {
            paths.forEach(path -> {
                try {
                    Image image = BufferedImageIO.readImage(path);

                    Map<String, Channel> channels = image.getChannels();
                    Map<String, Spectrum> transformed = Maps.applyToValues(
                            channels, DitFastFourierTransform::transform
                    );
                    Map<String, Channel> inversed = Maps.applyToValues(
                            transformed, DitFastFourierTransform::inverse
                    );

                    Image recovered = image.accept(ImageVisitor.imageVisitor(
                            grayImage -> GrayImage.fromChannels(inversed),
                            rgbImage -> RgbImage.fromChannels(inversed)
                    ));

                    assertThat(recovered, is(image));
                } catch (IOException ex) {
                    throw new AssertionError("IO operation failed", ex);
                }
            });
        }

    }

}