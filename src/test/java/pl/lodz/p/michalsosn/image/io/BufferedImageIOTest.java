package pl.lodz.p.michalsosn.image.io;

import org.junit.Test;
import pl.lodz.p.michalsosn.image.Image;
import pl.lodz.p.michalsosn.image.ImageVisitor;
import pl.lodz.p.michalsosn.image.transform.ColorConvertions;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

import static pl.lodz.p.michalsosn.image.io.BufferedImageIO.*;

/**
 * @author Michał Sośnicki
 */
public class BufferedImageIOTest {

    @Test
    public void testReadImage() throws Exception {
        Path input = Paths.get("images/color/mandrilc.bmp");
//        Path input = Paths.get("images/gray/aero.bmp");
        Path[] outputs = Stream.of("out1.png", "out2.png", "out3.png")
                               .map(str -> Paths.get(str))
                               .toArray(Path[]::new);

        Image image = readImage(input);

        image.accept(ImageVisitor.rgbVisitor(rgbImage -> {
            try {
                writeImage(ColorConvertions.extractRed(rgbImage), outputs[0]);
                writeImage(ColorConvertions.extractGreen(rgbImage), outputs[1]);
                writeImage(ColorConvertions.extractBlue(rgbImage), outputs[2]);
            } catch (IOException e) {
                throw new AssertionError("Writing failed", e);
            }
        }));

    }

    @Test
    public void testWriteImage() throws Exception {

    }

}