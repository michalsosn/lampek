package pl.lodz.p.michalsosn.domain.image.transform;

import pl.lodz.p.michalsosn.domain.image.Size2d;
import pl.lodz.p.michalsosn.domain.image.channel.Image;

import java.util.Arrays;

/**
 * @author Michał Sośnicki
 */
public final class Kernel implements Size2d {

    private final double[][] values;
    private final double shift;

    private Kernel(double[][] values, double shift) {
        this.values = values;
        this.shift = shift;
    }

    public static Kernel normalized(double[][] values) {
        int height = values.length;
        if (height == 0) {
            throw new IllegalArgumentException(
                    "Passed array has zero height."
            );
        }

        int width = values[0].length;
        if (width == 0) {
            throw new IllegalArgumentException(
                    "Passed array has zero width."
            );
        }

        for (int y = 1; y < height; y++) {
            if (values[y].length != width) {
                throw new IllegalArgumentException(
                        "Kernel rows must have equal lengths"
                );
            }
        }

        double positiveSum = 0;
        double negativeSum = 0;
        for (double[] valuesRow : values) {
            for (int x = 0; x < width; x++) {
                double value = valuesRow[x];
                if (value < 0) {
                    negativeSum += -value;
                } else {
                    positiveSum += value;
                }
            }
        }

        double totalRange = positiveSum + negativeSum;
        for (double[] valuesRow : values) {
            for (int x = 0; x < width; x++) {
                valuesRow[x] /= totalRange;
            }
        }

        double shift = negativeSum / totalRange * Image.MAX_VALUE;

        return new Kernel(values, shift);
    }

    public static Kernel unsafe(double[][] values) {
        return new Kernel(values, 0);
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

    public double getValue(int y, int x) {
        return values[y][x];
    }

    public double getShift() {
        return shift;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final Kernel kernel = (Kernel) o;

        return Double.compare(kernel.shift, shift) == 0
            && Arrays.deepEquals(values, kernel.values);
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = Arrays.deepHashCode(values);
        temp = Double.doubleToLongBits(shift);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    @Override
    public String toString() {
        return "Kernel{"
              + "values=" + Arrays.deepToString(values)
              + ", shift=" + shift
              + '}';
    }
}
