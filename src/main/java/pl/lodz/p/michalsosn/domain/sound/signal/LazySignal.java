package pl.lodz.p.michalsosn.domain.sound.signal;

import pl.lodz.p.michalsosn.domain.sound.TimeRange;

import java.util.function.IntToLongFunction;
import java.util.function.LongUnaryOperator;
import java.util.stream.LongStream;

/**
 * @author Michał Sośnicki
 */
public final class LazySignal implements Signal {

    private final IntToLongFunction valueFunction;
    private final int length;
    private final TimeRange samplingTime;

    public LazySignal(IntToLongFunction valueFunction, int length,
                     TimeRange samplingTime) {
        if (valueFunction == null || samplingTime == null) {
            throw new NullPointerException("arguments can't be null");
        }
        this.valueFunction = valueFunction;
        this.length = length;
        this.samplingTime = samplingTime;
    }

    @Override
    public long getValue(int sample) {
        return valueFunction.applyAsLong(sample);
    }

    @Override
    public TimeRange getSamplingTime() {
        return samplingTime;
    }

    @Override
    public LongStream values() {
        return stream().mapToLong(valueFunction);
    }

    @Override
    public int getLength() {
        return length;
    }

    @Override
    public Signal map(LongUnaryOperator valueMapper) {
        return new LazySignal(i -> valueMapper.applyAsLong(valueFunction.applyAsLong(i)),
                              length, samplingTime);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        LazySignal that = (LazySignal) o;

        return length == that.length
             && valueFunction.equals(that.valueFunction)
             && samplingTime.equals(that.samplingTime);
    }

    @Override
    public int hashCode() {
        int result = valueFunction.hashCode();
        result = 31 * result + length;
        result = 31 * result + samplingTime.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "LazySignal{"
                + "length=" + length
                + ", samplingTime=" + samplingTime
                + ", valueFunction=" + valueFunction
                + '}';
    }
}
