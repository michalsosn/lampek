package pl.lodz.p.michalsosn.domain.sound.signal;

import pl.lodz.p.michalsosn.domain.sound.TimeRange;

import java.util.function.DoubleUnaryOperator;
import java.util.function.IntToDoubleFunction;
import java.util.stream.DoubleStream;

/**
 * @author Michał Sośnicki
 */
public final class LazySignal implements Signal {

    private final IntToDoubleFunction valueFunction;
    private final int length;
    private final TimeRange samplingTime;

    public LazySignal(IntToDoubleFunction valueFunction, int length,
                     TimeRange samplingTime) {
        if (valueFunction == null || samplingTime == null) {
            throw new NullPointerException("arguments can't be null");
        }
        this.valueFunction = valueFunction;
        this.length = length;
        this.samplingTime = samplingTime;
    }

    @Override
    public double getValue(int sample) {
        return valueFunction.applyAsDouble(sample);
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
    public Signal map(DoubleUnaryOperator valueMapper) {
        return new LazySignal(i ->
                valueMapper.applyAsDouble(valueFunction.applyAsDouble(i)),
                length, samplingTime
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

        LazySignal that = (LazySignal) o;

        return length == that.length
             && stream().allMatch(p -> valueFunction.applyAsDouble(p)
                                    == that.valueFunction.applyAsDouble(p))
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
