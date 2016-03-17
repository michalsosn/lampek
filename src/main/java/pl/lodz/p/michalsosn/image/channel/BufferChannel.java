package pl.lodz.p.michalsosn.image.channel;

import java.util.Arrays;
import java.util.function.IntUnaryOperator;
import java.util.stream.IntStream;

/**
 * @author Michał Sośnicki
 */
public class BufferChannel implements Channel {

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
                    throw new IllegalArgumentException("Pixel rows must have equal lengths");
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
    public <T> T accept(ChannelVisitor<T> visitor) {
        return visitor.visit(this);
    }

    @Override
    public BufferChannel map(IntUnaryOperator valueMapper) {
        int height = getHeight();
        int width = getWidth();

        int[][] newValues = new int[height][width];

        forEach((y, x) -> newValues[y][x] = valueMapper.applyAsInt(values[y][x]));

        return new BufferChannel(newValues);
    }

    @Override
    public LazyChannel lazy() {
        return new LazyChannel(getHeight(), getWidth(), (y, x) -> values[y][x]);
    }

    @Override
    public BufferChannel strict() {
        return this;
    }
}

