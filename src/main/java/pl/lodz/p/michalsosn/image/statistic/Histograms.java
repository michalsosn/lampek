package pl.lodz.p.michalsosn.image.statistic;

import pl.lodz.p.michalsosn.image.Channel;

/**
 * @author Michał Sośnicki
 */
public final class Histograms {

    private Histograms() {
    }

    public static int[] values(Channel channel) {
        int[] counters = new int[256];
        channel.forEach((x, y) -> ++counters[channel.getValue(x, y)]);
        return counters;
    }

}
