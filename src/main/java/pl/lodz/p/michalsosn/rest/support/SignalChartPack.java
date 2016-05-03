package pl.lodz.p.michalsosn.rest.support;

import pl.lodz.p.michalsosn.domain.sound.signal.Signal;
import pl.lodz.p.michalsosn.entities.ResultEntity.SignalResultEntity;

import java.io.IOException;
import java.util.stream.IntStream;

/**
 * @author Michał Sośnicki
 */
public class SignalChartPack {

    private static final int PLOT_SIZE = 1000;

    private final long[] values;
    private final double start;
    private final double end;
    private final double frequency;
    private final double duration;

    public SignalChartPack(SignalResultEntity result) throws IOException {
        this(result, null, null);
    }

    public SignalChartPack(
            SignalResultEntity result, Double start, Double end
    ) throws IOException {
        Signal signal = result.getSignal();
        int length = signal.getLength();

        if (start == null || start < 0) {
            start = 0.0;
        }
        if (end == null || end > length) {
            end = (double) length;
        }

        int sampleStart = (int) Math.ceil(start);
        int sampleEnd = (int) Math.floor(end);
        int spanLength = sampleEnd - sampleStart;

        int step = (int) Math.floor((double) spanLength / PLOT_SIZE);
        if (step < 0) {
            this.values = new long[0];
        } else if (step <= 1) {
            this.values = signal.values().skip(sampleStart).limit(spanLength).toArray();
        } else {
            int resultLength = (int) Math.ceil((double) spanLength / step);
            this.values = IntStream.range(0, resultLength)
                    .map(i -> i * step + sampleStart)
                    .mapToLong(signal::getValue)
                    .toArray();
        }
        this.start = start;
        this.end = end;
        this.frequency = signal.getSamplingTime().getFrequency();
        this.duration = signal.getSamplingTime().getDuration();
    }

    public long[] getValues() {
        return values;
    }

    public double getStart() {
        return start;
    }

    public double getEnd() {
        return end;
    }

    public double getFrequency() {
        return frequency;
    }

    public double getDuration() {
        return duration;
    }
}

