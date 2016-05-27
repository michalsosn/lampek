package pl.lodz.p.michalsosn.domain.sound.filter;

import pl.lodz.p.michalsosn.domain.sound.TimeRange;

import java.util.function.DoubleUnaryOperator;
import java.util.function.IntToDoubleFunction;
import java.util.stream.DoubleStream;

/**
 * @author Michał Sośnicki
 */
public final class LazyFilter implements Filter {

    private final IntToDoubleFunction valueFunction;
    private final int length;
    private final int negativeLength;
    private final TimeRange samplingTime;

    public LazyFilter(IntToDoubleFunction valueFunction, int length, int negativeLength,
                      TimeRange samplingTime) {
        if (valueFunction == null || samplingTime == null) {
            throw new NullPointerException("arguments can't be null");
        }
        if (negativeLength < 0 || negativeLength > length) {
            throw new IllegalArgumentException("negative or positive length < 0");
        }
        this.valueFunction = valueFunction;
        this.length = length;
        this.negativeLength = negativeLength;
        this.samplingTime = samplingTime;
    }

    public static LazyFilter causal(IntToDoubleFunction valueFunction, int length,
                                    TimeRange samplingTime) {
        return new LazyFilter(valueFunction, length, 0, samplingTime);
    }

    public static LazyFilter nonCausal(IntToDoubleFunction valueFunction, int length,
                                       int negativeLength, TimeRange samplingTime) {
        return new LazyFilter(valueFunction, length, negativeLength, samplingTime);
    }

    @Override
    public double getValue(int sample) {
        return valueFunction.applyAsDouble(sample);
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
        return stream().mapToDouble(valueFunction);
    }

    @Override
    public int getLength() {
        return length;
    }

    @Override
    public Filter map(DoubleUnaryOperator valueMapper) {
        return new LazyFilter(i ->
                valueMapper.applyAsDouble(valueFunction.applyAsDouble(i)),
                length, negativeLength, samplingTime
        );
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        LazyFilter that = (LazyFilter) o;

        return length == that.length
            && negativeLength == that.negativeLength
            && samplingTime.equals(that.samplingTime)
            && stream().allMatch(p -> valueFunction.applyAsDouble(p)
                                   == that.valueFunction.applyAsDouble(p));
    }

    @Override
    public int hashCode() {
        int result = valueFunction.hashCode();
        result = 31 * result + length;
        result = 31 * result + negativeLength;
        result = 31 * result + samplingTime.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "LazyFilter{"
                + "length=" + length
                + ", negativeLength=" + negativeLength
                + ", samplingTime=" + samplingTime
                + ", valueFunction=" + valueFunction
                + '}';
    }
}
