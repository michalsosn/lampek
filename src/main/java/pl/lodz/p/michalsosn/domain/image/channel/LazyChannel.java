package pl.lodz.p.michalsosn.domain.image.channel;

import java.util.function.IntBinaryOperator;
import java.util.function.IntUnaryOperator;

/**
 * A color channel represented with a function.
 * It needs a constant amount of memory but will recompute the
 * values every time. The behavior is largely dependent on the
 * supplied function, which may allocate memory or cache values
 * in a closure.
 * Objects of this class are immutable.
 * @author Michał Sośnicki
 */
public final class LazyChannel implements Channel {

    private final int height;
    private final int width;
    private final IntBinaryOperator valueFunction;

    public LazyChannel(int height, int width, IntBinaryOperator valueFunction) {
        this.height = height;
        this.width = width;
        this.valueFunction = valueFunction;
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
        return valueFunction.applyAsInt(y, x);
    }

    @Override
    public int[][] copyValues() {
        int[][] values = new int[height][width];
        forEach((y, x) -> values[y][x] = valueFunction.applyAsInt(y, x));
        return values;
    }

    @Override
    public void copyTo(int y, int[] buffer, int dest) {
        for (int x = 0; x < width; x++) {
            buffer[dest + x] = valueFunction.applyAsInt(y, x);
        }
    }

    @Override
    public Channel map(IntUnaryOperator operator) {
        return new LazyChannel(height, width, (y, x) ->
                operator.applyAsInt(valueFunction.applyAsInt(y, x))
        );
    }

    @Override
    public LazyChannel toLazy() {
        return this;
    }

    @Override
    public BufferChannel toStrict() {
        return new BufferChannel(copyValues());
    }

    @Override
    public Channel constructSimilar(int height, int width,
                                    IntBinaryOperator valueFunction) {
        return new LazyChannel(height, width, valueFunction);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final LazyChannel that = (LazyChannel) o;

        if (height != that.height || width != that.width) {
            return false;
        }

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (valueFunction.applyAsInt(y, x)
                 != that.valueFunction.applyAsInt(y, x)) {
                    return false;
                }
            }
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = height;
        result = 31 * result + width;
        result = 31 * result + valueFunction.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "LazyChannel{"
              + "height=" + height
              + ", width=" + width
              + ", valueFunction=" + valueFunction
              + '}';
    }
}
