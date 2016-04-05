package pl.lodz.p.michalsosn.domain.image.statistic;

import pl.lodz.p.michalsosn.domain.image.channel.Channel;
import pl.lodz.p.michalsosn.domain.image.image.Image;

/**
 * @author Michał Sośnicki
 */
public final class Histograms {

    private Histograms() {
    }

    public static int[] valueHistogram(Channel channel) {
        int[] counters = new int[Image.MAX_VALUE + 1];
        channel.forEach((y, x) -> ++counters[channel.getValue(y, x)]);
        return counters;
    }

    public static int[] valueHistogramRunningTotal(Channel channel) {
        int[] histogram = valueHistogram(channel);
        for (int i = 1; i < histogram.length; i++) {
            histogram[i] += histogram[i - 1];
        }
        return histogram;
    }

}
