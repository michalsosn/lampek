package pl.lodz.p.michalsosn.domain.image.transform.segmentation;

import org.junit.Test;
import pl.lodz.p.michalsosn.domain.image.channel.Image;
import pl.lodz.p.michalsosn.io.BufferedImageIO;
import pl.lodz.p.michalsosn.io.ImageSet;

import java.nio.file.Path;

//import static org.hamcrest.CoreMatchers.is;
//import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author Michał Sośnicki
 */
public class SegmentationsTest {
    @Test
    public void splitMergeImageMaxRange() throws Exception {
        Path path = ImageSet.listImages(ImageSet.GRAY).findAny().get();
        Image image = BufferedImageIO.readImage(path);
//        int height = image.getHeight();
//        int width = image.getWidth();
//        Image fullImage = new GrayImage(new ConstChannel(height, width, 255));

        Mask[] masks = Segmentations.splitMergeImageMaxRange(image, 100);

        for (int i = 0; i < masks.length; ++i) {
//            Image maskedImage = fullImage.map(masks[i].toOperator());
            Image maskedImage = image.map(masks[i].toOperator());
        }

//      assertThat(recovered, is(image));
    }
}
