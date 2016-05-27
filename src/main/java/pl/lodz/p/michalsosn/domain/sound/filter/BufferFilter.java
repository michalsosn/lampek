package pl.lodz.p.michalsosn.domain.sound.filter;

import pl.lodz.p.michalsosn.domain.sound.TimeRange;
import pl.lodz.p.michalsosn.domain.util.ArrayUtils;

import java.util.Arrays;
import java.util.function.DoubleUnaryOperator;
import java.util.stream.DoubleStream;

/**
 * @author Michał Sośnicki
 */
public final class BufferFilter implements Filter {

    private final double[] values;
    private final int negativeLength;
    private final TimeRange samplingTime;

    public BufferFilter(double[] values, int negativeLength, TimeRange samplingTime) {
        if (values == null || samplingTime == null) {
            throw new NullPointerException("arguments can't be null");
        }
        if (negativeLength < 0 || negativeLength > values.length) {
            throw new IllegalArgumentException("negative or positive length negative");
        }
        this.values = values;
        this.negativeLength = negativeLength;
        this.samplingTime = samplingTime;
    }

    @Override
    public double getValue(int sample) {
        return values[sample + negativeLength];
    }

    @Override
    public int getNegativeLength() {
        return negativeLength;
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
    public Filter map(DoubleUnaryOperator valueMapper) {
        return new BufferFilter(values().map(valueMapper).toArray(),
                negativeLength, samplingTime);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        BufferFilter that = (BufferFilter) o;

        return negativeLength == that.negativeLength
            && samplingTime.equals(that.samplingTime)
            && Arrays.equals(values, that.values);

    }

    @Override
    public int hashCode() {
        int result = Arrays.hashCode(values);
        result = 31 * result + negativeLength;
        result = 31 * result + samplingTime.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "BufferFilter{"
                + "negativeLength=" + negativeLength
                + ", samplingTime=" + samplingTime
                + ", length=" + getLength()
                + ", values=" + ArrayUtils.limitedToString(values, 10)
                + '}';
    }

}
