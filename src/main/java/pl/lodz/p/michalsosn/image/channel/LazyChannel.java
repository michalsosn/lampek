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
    public LazyChannel lazy() {
        return this;
    }

    @Override
    public BufferChannel strict() {
        return new BufferChannel(copyValues());
    }
}
