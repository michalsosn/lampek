package pl.lodz.p.michalsosn.domain.image.channel;

import java.util.Arrays;
import java.util.function.IntBinaryOperator;
import java.util.function.IntUnaryOperator;
import java.util.stream.IntStream;

/**
 * An color channel filled with a single value.
 * The representation uses a constant amount of memory regardless of the size.
 * Objects of this class are immutable.
 * @author Michał Sośnicki
 */
public final class ConstChannel implements Channel {

    private final int height;
    private final int width;
    private final int value;

    public ConstChannel(int height, int width, int value) {
        this.height = height;
        this.width = width;
        this.value = value;
    }

    @Override
    public int getHeight() {
        return height;
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public int getValue(int y, int x) {
        return value;
    }

    @Override
    public IntStream values() {
        return IntStream.generate(() -> value).limit(getSize());
    }

    @Override
    public int[][] copyValues() {
        int[][] newValues = new int[getHeight()][];
        for (int[] valuesRow : newValues) {
            Arrays.fill(valuesRow, value);
        }
        return newValues;
    }

    @Override
    public void copyTo(int y, int[] buffer, int dest) {
        Arrays.fill(buffer, dest, dest + width, value);
    }

    @Override
    public ConstChannel map(IntUnaryOperator operator) {
        return new ConstChannel(height, width, operator.applyAsInt(value));
    }

    @Override
    public LazyChannel toLazy() {
        return new LazyChannel(height, width, (y, x) -> value);
    }

    @Override
    public ConstChannel toStrict() {
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

        final ConstChannel that = (ConstChannel) o;

        return height == that.height
            && width == that.width
            && value == that.value;
    }

    @Override
    public int hashCode() {
        int result = height;
        result = 31 * result + width;
        result = 31 * result + value;
        return result;
    }

    @Override
    public String toString() {
        return "ConstChannel{"
              + "height=" + height
              + ", width=" + width
              + ", value=" + value
              + '}';
    }
}
