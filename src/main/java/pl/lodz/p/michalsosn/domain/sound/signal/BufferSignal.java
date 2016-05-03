package pl.lodz.p.michalsosn.domain.sound.signal;

import pl.lodz.p.michalsosn.domain.sound.TimeRange;
import pl.lodz.p.michalsosn.domain.util.ArrayUtils;

import java.util.Arrays;
import java.util.function.LongUnaryOperator;
import java.util.stream.LongStream;

/**
 * @author Michał Sośnicki
 */
public final class BufferSignal implements Signal {

    private final long[] values;
    private final TimeRange samplingTime;

    public BufferSignal(long[] values, TimeRange samplingTime) {
        if (values == null || samplingTime == null) {
            throw new NullPointerException("arguments can't be null");
        }
        this.values = values;
        this.samplingTime = samplingTime;
    }

    @Override
    public long getValue(int sample) {
        return values[sample];
    }

    @Override
    public TimeRange getSamplingTime() {
        return samplingTime;
    }

    @Override
    public LongStream values() {
        return Arrays.stream(values);
    }

    @Override
    public int getLength() {
        return values.length;
    }

    @Override
    public Signal map(LongUnaryOperator valueMapper) {
        return new BufferSignal(values().map(valueMapper).toArray(), samplingTime);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        BufferSignal that = (BufferSignal) o;

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
        return "BufferSignal{"
              + "samplingTime=" + samplingTime
              + ", length=" + getLength()
              + ", values=" + ArrayUtils.limitedToString(values, 10)
              + '}';
    }
}
