package pl.lodz.p.michalsosn.domain.image.transform.segmentation;

import pl.lodz.p.michalsosn.domain.image.channel.Channel;
import pl.lodz.p.michalsosn.domain.image.channel.Image;

import java.util.Collection;
import java.util.stream.DoubleStream;

/**
 * @author Michał Sośnicki
 */
public final class Segmentations {

    private Segmentations() {
    }

    public static Mask[] splitMergeImageMaxRange(Image image, int maxRange) {
        return splitMergeImage(image, new DoubleMaxRangeClassifier(maxRange));
    }

    public static Mask[] splitMergeImageMaxStdDev(Image image, double maxStdDev) {
        return splitMergeImage(image, new DoubleMaxStdDevClassifier(maxStdDev));
    }

    private static Mask[] splitMergeImage(
            Image image, RegionClassifier<Double, DoubleStream> classifier
    ) {
        DoubleArrayRegion region = new DoubleArrayRegion(
                averageValues(image), 0, 0, image.getHeight(), image.getWidth()
        );

        RegionTree<Double, DoubleStream> tree
                = new RegionTree<>(classifier, DoubleStream::concat, region);

        tree.merge();

        return tree.collectMasks();
    }

    private static double[][] averageValues(Image image) {
        if (image.getSize() == 0) {
            return new double[0][0];
        }

        int height = image.getHeight();
        int width = image.getWidth();
        double[][] averages = new double[height][width];

        Collection<Channel> channels = image.getChannels().values();
        int channelCount = channels.size();
        for (int y = 0; y < height; ++y) {
            for (int x = 0; x < width; ++x) {
                double average = 0;
                for (Channel channel : channels) {
                    average += channel.getValue(y, x);
                }
                averages[y][x] = average / channelCount;
            }
        }

        return averages;
    }

}
