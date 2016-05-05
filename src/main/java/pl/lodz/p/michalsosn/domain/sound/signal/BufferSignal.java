package pl.lodz.p.michalsosn.domain.sound.signal;

import pl.lodz.p.michalsosn.domain.sound.TimeRange;
import pl.lodz.p.michalsosn.domain.util.ArrayUtils;

import java.util.Arrays;
import java.util.function.DoubleUnaryOperator;
import java.util.stream.DoubleStream;

/**
 * @author Michał Sośnicki
 */
public final class BufferSignal implements Signal {

    private final double[] values;
    private final TimeRange samplingTime;

    public BufferSignal(double[] values, TimeRange samplingTime) {
        if (values == null || samplingTime == null) {
            throw new NullPointerException("arguments can't be null");
        }
        this.values = values;
        this.samplingTime = samplingTime;
    }

    @Override
    public double getValue(int sample) {
        return values[sample];
    }

    @Override
    public TimeRange getSamplingTime() {
        return samplingTime;
    }

    @Override
    public DoubleStream values() {
        return Arrays.stream(values);
    }

    @Override
    public int getLength() {
        return values.length;
    }

    @Override
    public Signal map(DoubleUnaryOperator valueMapper) {
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
