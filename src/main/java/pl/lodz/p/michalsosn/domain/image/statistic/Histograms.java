package pl.lodz.p.michalsosn.domain.image.statistic;

import pl.lodz.p.michalsosn.domain.image.channel.Channel;

/**
 * @author Michał Sośnicki
 */
public final class Histograms {

    private Histograms() {
    }

    public static int[] values(Channel channel) {
        int[] counters = new int[256];
        channel.forEach((y, x) -> ++counters[channel.getValue(y, x)]);
        return counters;
    }

}
