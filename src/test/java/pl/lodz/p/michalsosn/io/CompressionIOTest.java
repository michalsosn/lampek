package pl.lodz.p.michalsosn.io;

import org.junit.Test;
import pl.lodz.p.michalsosn.domain.image.channel.*;
import pl.lodz.p.michalsosn.domain.image.spectrum.ImageSpectrum;
import pl.lodz.p.michalsosn.domain.image.spectrum.Spectrum2d;
import pl.lodz.p.michalsosn.domain.image.transform.DitFastFourierTransform;
import pl.lodz.p.michalsosn.util.Maps;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import java.util.stream.Stream;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static pl.lodz.p.michalsosn.io.BufferedImageIO.readImage;
import static pl.lodz.p.michalsosn.io.CompressionIO.*;

/**
 * @author Michał Sośnicki
 */
public class CompressionIOTest {
    @Test
    public void testReadWriteDoubleArray() throws Exception {
        double[][] data = {{2, 1, 0.3}, {4, 3, 1}, {100, 9, 0.5}};

        byte[] transformed = fromDoubleArray(data);

        double[][] recovered = toDoubleArray(transformed);

        assertThat(recovered, is(data));
    }

    @Test
    public void testReadWriteImageSpectrum() throws Exception {
        try (Stream<Path> paths = ResourceSet.listResources(ResourceSet.IMAGES)) {
            paths.forEach(path -> {
                try {
                    System.out.println("Test FFTing: " + path);
                    Image initialImage = readImage(path);

                    Map<String, Channel> initialChannels =
                            initialImage.getChannels();
                    Map<String, Spectrum2d> transformedSpectra =
                            Maps.applyToValues(
                            initialChannels, DitFastFourierTransform::transform
                    );
                    ImageSpectrum transformedImageSpectrum
                            = new ImageSpectrum(transformedSpectra);

                    byte[] data = fromImageSpectrum(transformedImageSpectrum);
                    ImageSpectrum recoveredImageSpectrum
                            = toImageSpectrum(data);

                    Map<String, Spectrum2d> recoveredSpectra
                            = recoveredImageSpectrum.getSpectra();
                    Map<String, Channel> inversedChannels = Maps.applyToValues(
                            recoveredSpectra, DitFastFourierTransform::inverse
                    );

                    Image recoveredImage = initialImage.accept(
                            ImageVisitor.imageVisitor(
                        grayImage -> GrayImage.fromChannels(inversedChannels),
                        rgbImage -> RgbImage.fromChannels(inversedChannels)
                    ));

                    assertThat(recoveredImage, is(initialImage));
                } catch (IOException ex) {
                    throw new AssertionError("IO operation failed", ex);
                }
            });
        }
    }

}