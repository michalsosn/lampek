package pl.lodz.p.michalsosn.rest.support;

import pl.lodz.p.michalsosn.domain.sound.filter.Filter;

import java.io.IOException;
import java.util.stream.IntStream;

import static pl.lodz.p.michalsosn.entities.ResultEntity.SoundFilterResultEntity;

/**
 * @author Michał Sośnicki
 */
public class SoundFilterChartPack {

    private static final int PLOT_SIZE = 1000;

    private final double[] values;
    private final double startTime;
    private final double duration;

    public SoundFilterChartPack(SoundFilterResultEntity result) throws IOException {
        this(result, null, null);
    }

    public SoundFilterChartPack(
            SoundFilterResultEntity result, Double startTime, Double endTime
    ) throws IOException {
        final Filter filter = result.getFilter();
        final int positiveLength = filter.getPositiveLength();
        final int negativeLength = filter.getNegativeLength();
        final double sampleDuration = filter.getSamplingTime().getDuration();

        if (startTime == null || startTime < -negativeLength * sampleDuration) {
            startTime = -negativeLength * sampleDuration;
        }
        if (endTime == null || endTime > positiveLength * sampleDuration) {
            endTime = positiveLength * sampleDuration;
        }

        int sampleStart = Math.max(-negativeLength,
                                   (int) Math.ceil(startTime / sampleDuration));
        int sampleEnd = Math.min(positiveLength,
                                 (int) Math.floor(endTime / sampleDuration));
        int spanLength = sampleEnd - sampleStart;

        int step = (int) Math.floor((double) spanLength / PLOT_SIZE);
        if (step < 0) {
            this.values = new double[0];
            this.duration = Double.NaN;
        } else if (step <= 1) {
            this.values = IntStream.range(sampleStart, sampleEnd)
                    .mapToDouble(filter::getValue).toArray();
            this.duration = sampleDuration;
        } else {
            int resultLength = (int) Math.ceil((double) spanLength / step);
            this.values = IntStream.range(0, resultLength)
                    .map(i -> i * step + sampleStart)
                    .mapToDouble(filter::getValue)
                    .toArray();
            this.duration = sampleDuration * step;
        }
        this.startTime = startTime;
    }

    public double[] getValues() {
        return values;
    }

    public double getStartTime() {
        return startTime;
    }

    public double getDuration() {
        return duration;
    }
}

