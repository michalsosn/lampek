package pl.lodz.p.michalsosn.domain.image.transform;

import pl.lodz.p.michalsosn.domain.image.channel.Channel;

import java.util.function.UnaryOperator;

import static pl.lodz.p.michalsosn.domain.image.statistic.Histograms.valueHistogramRunningTotal;

/**
 * @author Michał Sośnicki
 */
public final class HistogramAdjustments {

    private HistogramAdjustments() {
    }

    public static UnaryOperator<Channel> uniformDensity(int minValue,
                                                        int maxValue) {
        return channel -> {
            int[] runHistogram = valueHistogramRunningTotal(channel);
            int length = runHistogram[runHistogram.length - 1];
            double valueWidth = maxValue - minValue;

            return channel.map(value -> (int) Math.round(
                    minValue + valueWidth * runHistogram[value] / length
            ));
        };
    }

    public static UnaryOperator<Channel> hyperbolicDensity(int minValue,
                                                           int maxValue) {
        return channel -> {
            int[] runHistogram = valueHistogramRunningTotal(channel);
            double length = runHistogram[runHistogram.length - 1];
            double valueRatio = (double) maxValue / minValue;

            return channel.map(value -> (int) Math.round(
                minValue * Math.pow(valueRatio, runHistogram[value] / length)
            ));
        };
    }

}
