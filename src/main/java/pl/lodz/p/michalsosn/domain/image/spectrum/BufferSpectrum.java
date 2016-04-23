package pl.lodz.p.michalsosn.domain.image.spectrum;

import pl.lodz.p.michalsosn.domain.util.IntBiFunction;

import java.util.Arrays;
import java.util.function.UnaryOperator;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * @author Michał Sośnicki
 */
public final class BufferSpectrum implements Spectrum {

    private final Complex[][] values;

    public BufferSpectrum(Complex[][] values) {
        int height = values.length;
        if (height != 0) {
            int width = values[0].length;
            for (int y = 1; y < height; y++) {
                if (values[y].length != width) {
                    throw new IllegalArgumentException(
                            "Spectrum rows must have equal lengths"
                    );
                }
            }
        }

        this.values = values;
    }

    public static BufferSpectrum construct(int height, int width,
                           IntBiFunction<Complex> generator) {
        Complex[][] values = new Complex[height][width];
        for (int y = 0; y < height; y++) {
            final int cY = y;
            Arrays.setAll(values[y], x -> generator.apply(cY, x));
        }
        return new BufferSpectrum(values);
    }

    @Override
    public int getHeight() {
        return values.length;
    }

    @Override
    public int getWidth() {
        if (values.length == 0) {
            return 0;
        }
        return values[0].length;
    }

    @Override
    public Complex getValue(int y, int x) {
        return values[y][x];
    }

    @Override
    public Stream<Complex> values() {
        return IntStream.range(0, getHeight()).boxed().flatMap(y ->
                Arrays.stream(values[y])
        );
    }

    @Override
    public Complex[][] copyValues() {
        int height = getHeight();
        Complex[][] newValues = new Complex[height][];
        for (int y = 0; y < height; y++) {
            newValues[y] = Arrays.copyOf(values[y], values[y].length);
        }
        return newValues;
    }

    @Override
    public void copyTo(int y, Complex[] buffer, int dest) {
        System.arraycopy(values[y], 0, buffer, dest, values[y].length);
    }

    @Override
    public Spectrum map(UnaryOperator<Complex> valueMapper) {
        int height = getHeight();
        int width = getWidth();

        Complex[][] newValues = new Complex[height][width];

        for (int y = 0; y < height; ++y) {
            final int cY = y;
            Arrays.setAll(newValues[y], x -> valueMapper.apply(values[cY][x]));
        }

        return new BufferSpectrum(newValues);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        BufferSpectrum that = (BufferSpectrum) o;

        return isEqualSize(that) && Arrays.deepEquals(values, that.values);
    }

    @Override
    public int hashCode() {
        return Arrays.deepHashCode(values);
    }

    @Override
    public String toString() {
        return "BufferSpectrum{"
                + "height=" + getHeight()
                + ", width=" + getWidth()
                + ", values=" + values
                + '}';
    }
}
