package pl.lodz.p.michalsosn.domain.sound.sound;

import pl.lodz.p.michalsosn.domain.sound.TimeRange;

import java.util.function.IntUnaryOperator;
import java.util.stream.IntStream;

/**
 * @author Michał Sośnicki
 */
public final class LazySound implements Sound {

    private final IntUnaryOperator valueFunction;
    private final int length;
    private final TimeRange samplingTime;

    public LazySound(IntUnaryOperator valueFunction, int length,
                     TimeRange samplingTime) {
        if (valueFunction == null || samplingTime == null) {
            throw new NullPointerException("arguments can't be null");
        }
        this.valueFunction = valueFunction;
        this.length = length;
        this.samplingTime = samplingTime;
    }

    @Override
    public int getValue(int sample) {
//        if (outside(sample)) {
//            return 0;
//        }
        return valueFunction.applyAsInt(sample);
    }

    @Override
    public IntStream values() {
        return stream().map(valueFunction);
    }

    @Override
    public TimeRange getSamplingTime() {
        return samplingTime;
    }

    @Override
    public int getLength() {
        return length;
    }

    @Override
    public Sound map(IntUnaryOperator valueMapper) {
        return new LazySound(valueMapper.compose(valueFunction), length, samplingTime);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        LazySound that = (LazySound) o;

        return length == that.length
            && stream().allMatch(p -> valueFunction.applyAsInt(p)
                                   == that.valueFunction.applyAsInt(p))
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
        return "LazySound{"
              + "length=" + length
              + ", samplingTime=" + samplingTime
              + ", valueFunction=" + valueFunction
              + '}';
    }
}
