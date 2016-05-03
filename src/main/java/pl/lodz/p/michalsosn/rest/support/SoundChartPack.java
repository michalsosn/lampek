package pl.lodz.p.michalsosn.rest.support;

import pl.lodz.p.michalsosn.domain.sound.sound.Sound;

import java.io.IOException;
import java.util.stream.IntStream;

import static pl.lodz.p.michalsosn.entities.ResultEntity.SoundResultEntity;

/**
 * @author Michał Sośnicki
 */
public class SoundChartPack {

    private static final int PLOT_SIZE = 1000;

    private final int[] values;
    private final double startTime;
    private final double duration;

    public SoundChartPack(SoundResultEntity result) throws IOException {
        this(result, null, null);
    }

    public SoundChartPack(
            SoundResultEntity result, Double startTime, Double endTime
    ) throws IOException {
        Sound sound = result.getSound();
        int length = sound.getLength();
        double sampleDuration = sound.getSamplingTime().getDuration();

        if (startTime == null || startTime < 0) {
            startTime = 0.0;
        }
        if (endTime == null || endTime > sampleDuration * length) {
            endTime = sampleDuration * length;
        }

        int sampleStart = Math.max(0, (int) Math.ceil(startTime / sampleDuration));
        int sampleEnd = Math.min(length, (int) Math.floor(endTime / sampleDuration));
        int spanLength = sampleEnd - sampleStart;

        int step = (int) Math.floor((double) spanLength / PLOT_SIZE);
        if (step < 0) {
            this.values = new int[0];
            this.duration = Double.NaN;
        } else if (step <= 1) {
            this.values = sound.values().skip(sampleStart).limit(spanLength).toArray();
            this.duration = sampleDuration;
        } else {
            int resultLength = (int) Math.ceil((double) spanLength / step);
            this.values = IntStream.range(0, resultLength)
                    .map(i -> i * step + sampleStart)
                    .map(sound::getValue)
                    .toArray();
            this.duration = sampleDuration * step;
        }
        this.startTime = startTime;
    }

    public int[] getValues() {
        return values;
    }

    public double getDuration() {
        return duration;
    }

    public double getStartTime() {
        return startTime;
    }
}


