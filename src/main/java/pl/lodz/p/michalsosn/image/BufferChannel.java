package pl.lodz.p.michalsosn.image;

import java.util.Arrays;
import java.util.function.IntUnaryOperator;

/**
 * @author Michał Sośnicki
 */
public class BufferChannel implements Channel<BufferChannel> {

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
            for (int x = 1; x < height; ++x) {
                if (values[x].length != width) {
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

    public int getValue(int x, int y) {
        return values[x][y];
    }

    /**
     * @return a copy of the 2d array of pixel values contained in this channel.
     */
    public int[][] getValues() {
        int[][] newValues = new int[getHeight()][];
        for (int x = 0; x < newValues.length; x++) {
            newValues[x] = Arrays.copyOf(values[x], values[x].length);
        }
        return newValues;
    }

    public void copyTo(int x, int[] buffer, int dest) {
        System.arraycopy(values[x], 0, buffer, dest, values[x].length);
    }

    @Override
    public BufferChannel map(IntUnaryOperator valueMapper) {
        int height = getHeight();
        int width = getWidth();

        int[][] newValues = new int[height][width];

        for (int x = 0; x < height; x++) {
            for (int y = 0; y < width; y++) {
                newValues[x][y] = valueMapper.applyAsInt(values[x][y]);
            }
        }

        return new BufferChannel(newValues);
    }

}

