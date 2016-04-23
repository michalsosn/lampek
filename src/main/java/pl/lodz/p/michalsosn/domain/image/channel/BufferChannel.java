package pl.lodz.p.michalsosn.domain.image.channel;

import java.util.Arrays;
import java.util.function.IntBinaryOperator;
import java.util.function.IntUnaryOperator;
import java.util.stream.IntStream;

/**
 * An array made of one of primary colors of an image.
 * Objects of this class are immutable.
 * The array is rectangular, all rows are of equal length.
 * @author Michał Sośnicki
 */
public final class BufferChannel implements Channel {

    private final int[][] values;

    /**
     * Creates the Channel from an array of pixel values.
     * The array is not copied for efficiency, so do it if it's possible
     * that it changes afterwards.
     * @param values An array of values.
     */
    public BufferChannel(int[][] values) {
        int height = values.length;
        if (height != 0) {
            int width = values[0].length;
            for (int y = 1; y < height; y++) {
                if (values[y].length != width) {
                    throw new IllegalArgumentException(
                            "Pixel rows must have equal lengths"
                    );
                }
            }
        }

        this.values = values;
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
    public int getValue(int y, int x) {
        return values[y][x];
    }

    @Override
    public IntStream values() {
        return IntStream.range(0, getHeight()).flatMap(y ->
                Arrays.stream(values[y])
        );
    }

    @Override
    public int[][] copyValues() {
        int height = getHeight();
        int[][] newValues = new int[height][];
        for (int y = 0; y < height; y++) {
            newValues[y] = Arrays.copyOf(values[y], values[y].length);
        }
        return newValues;
    }

    @Override
    public void copyTo(int y, int[] buffer, int dest) {
        System.arraycopy(values[y], 0, buffer, dest, values[y].length);
    }

    @Override
    public BufferChannel map(IntUnaryOperator valueMapper) {
        int height = getHeight();
        int width = getWidth();

        int[][] newValues = new int[height][width];

        for (int y = 0; y < height; ++y) {
            final int cY = y;
            Arrays.setAll(newValues[y], x -> valueMapper.applyAsInt(values[cY][x]));
        }

        return new BufferChannel(newValues);
    }

    @Override
    public LazyChannel toLazy() {
        return new LazyChannel(getHeight(), getWidth(), (y, x) -> values[y][x]);
    }

    @Override
    public BufferChannel toStrict() {
        return this;
    }

    @Override
    public Channel constructSimilar(int height, int width,
                                    IntBinaryOperator valueFunction) {
        int[][] newValues = new int[height][width];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                newValues[y][x] = valueFunction.applyAsInt(y, x);
            }
        }
        return new BufferChannel(newValues);
    }

    @Override
    public Channel constructConst(int height, int width, int value) {
        return new ConstChannel(height, width, value);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final BufferChannel that = (BufferChannel) o;

        return isEqualSize(that) && Arrays.deepEquals(values, that.values);
    }

    @Override
    public int hashCode() {
        return Arrays.deepHashCode(values);
    }

    @Override
    public String toString() {
        return "BufferChannel{"
             + "height=" + getHeight()
             + ", width=" + getWidth()
             + ", values=" + values
             + '}';
    }
}

