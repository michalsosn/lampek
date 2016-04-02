package pl.lodz.p.michalsosn.domain.image.transform;

import pl.lodz.p.michalsosn.domain.image.channel.Channel;
import pl.lodz.p.michalsosn.domain.image.statistic.Histograms;

import java.util.function.UnaryOperator;

/**
 * @author Michał Sośnicki
 */
public final class HistogramAdjustments {

    private HistogramAdjustments() {
    }

    public static UnaryOperator<Channel> uniformDensity(int minValue,
                                                        int maxValue) {
        return channel -> {
            int[] accumulatedHistogram = accumulatedHistogram(channel);
            int length = accumulatedHistogram[accumulatedHistogram.length - 1];
            int valueWidth = maxValue - minValue;

            return channel.map(value ->
                    minValue + valueWidth * accumulatedHistogram[value] / length
            );
        };
    }

    public static UnaryOperator<Channel> hiperbolicDensity(int minValue,
                                                           int maxValue) {
        return channel -> {
            int[] accumHistogram = accumulatedHistogram(channel);
            double length = accumHistogram[accumHistogram.length - 1];
            double valueRatio = (double) maxValue / minValue;

            return channel.map(value -> (int) (
                minValue * Math.pow(valueRatio, accumHistogram[value] / length)
            ));
        };
    }

    private static int[] accumulatedHistogram(Channel channel) {
        int[] histogram = Histograms.valueHistogram(channel);
        for (int i = 1; i < histogram.length; i++) {
            histogram[i] += histogram[i - 1];
        }
        return histogram;
    }

}
