package pl.lodz.p.michalsosn.domain.sound.sound;

import pl.lodz.p.michalsosn.domain.sound.TimeRange;
import pl.lodz.p.michalsosn.domain.sound.signal.Signal;
import pl.lodz.p.michalsosn.domain.util.ArrayUtils;

import java.util.Arrays;
import java.util.function.IntUnaryOperator;
import java.util.stream.IntStream;

/**
 * @author Michał Sośnicki
 */
public final class BufferSound implements Sound {

    private final int[] values;
    private final TimeRange samplingTime;

    public BufferSound(int[] values, TimeRange samplingTime) {
        if (values == null || samplingTime == null) {
            throw new NullPointerException("arguments can't be null");
        }
        this.values = values;
        this.samplingTime = samplingTime;
    }

    public static BufferSound of(Signal signal) {
        final int[] values = signal.values().mapToInt(value -> (int)
                Math.max(Sound.MIN_VALUE, Math.min(Sound.MAX_VALUE, Math.round(value)))
        ).toArray();
        return new BufferSound(values, signal.getSamplingTime());
    }

    @Override
    public int getValue(int sample) {
        return values[sample];
    }

    @Override
    public TimeRange getSamplingTime() {
        return samplingTime;
    }

    @Override
    public IntStream values() {
        return Arrays.stream(values);
    }

    @Override
    public int getLength() {
        return values.length;
    }

    @Override
    public Sound map(IntUnaryOperator valueMapper) {
        return new BufferSound(values().map(valueMapper).toArray(), samplingTime);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        BufferSound that = (BufferSound) o;

        return samplingTime.equals(that.samplingTime)
            && Arrays.equals(values, that.values);

    }

    @Override
    public int hashCode() {
        int result = Arrays.hashCode(values);
        result = 31 * result + samplingTime.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "BufferSound{"
              + "samplingTime=" + samplingTime
              + ", length=" + getLength()
              + ", values=" + ArrayUtils.limitedToString(values, 10)
              + '}';
    }
}
