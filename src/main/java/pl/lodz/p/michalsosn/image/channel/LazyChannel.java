package pl.lodz.p.michalsosn.image.channel;

import java.util.function.IntBinaryOperator;
import java.util.function.IntUnaryOperator;

/**
 * @author Michał Sośnicki
 */
public class LazyChannel implements Channel {

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
    public <T> T accept(ChannelVisitor<T> visitor) {
        return visitor.visit(this);
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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final LazyChannel that = (LazyChannel) o;

        if (height != that.height) return false;
        if (width != that.width) return false;
        return valueFunction.equals(that.valueFunction);

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
        return "LazyChannel{" +
                "height=" + height +
                ", width=" + width +
                ", valueFunction=" + valueFunction +
                '}';
    }
}
