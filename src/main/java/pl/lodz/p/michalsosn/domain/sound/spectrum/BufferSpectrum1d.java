package pl.lodz.p.michalsosn.domain.sound.spectrum;

import pl.lodz.p.michalsosn.domain.complex.Complex;
import pl.lodz.p.michalsosn.domain.sound.TimeRange;
import pl.lodz.p.michalsosn.domain.util.ArrayUtils;

import java.util.Arrays;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

/**
 * @author Michał Sośnicki
 */
public final class BufferSpectrum1d implements Spectrum1d {

    private final Complex[] values;
    private final TimeRange basicTime;

    public BufferSpectrum1d(Complex[] values, TimeRange basicTime) {
        if (values == null || basicTime == null) {
            throw new NullPointerException("arguments can't be null");
        }
        this.values = values;
        this.basicTime = basicTime;
    }

    @Override
    public Complex getValue(int sample) {
//        if (outside(sample)) {
//            return Complex.ZERO;
//        }
        return values[sample];
    }

    @Override
    public Stream<Complex> values() {
        return Arrays.stream(values);
    }

    @Override
    public TimeRange getBasicTime() {
        return basicTime;
    }

    @Override
    public int getLength() {
        return values.length;
    }

    @Override
    public Spectrum1d map(UnaryOperator<Complex> valueMapper) {
        return new BufferSpectrum1d(
                values().map(valueMapper).toArray(Complex[]::new),
                basicTime
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

        BufferSpectrum1d that = (BufferSpectrum1d) o;

        // Probably incorrect - comparing Object[] arrays with Arrays.equals
        return basicTime.equals(that.basicTime)
            && Arrays.equals(values, that.values);

    }

    @Override
    public int hashCode() {
        int result = Arrays.hashCode(values);
        result = 31 * result + basicTime.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "BufferSpectrum1d{"
             + "basicTime=" + basicTime
             + ", length=" + getLength()
             + ", values=" + ArrayUtils.limitedToString(values, 10)
             + '}';
    }
}
