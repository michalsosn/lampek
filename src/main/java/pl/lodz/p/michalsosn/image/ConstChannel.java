package pl.lodz.p.michalsosn.image;

import java.util.Arrays;
import java.util.function.IntUnaryOperator;

/**
 * @author Michał Sośnicki
 */
public class ConstChannel implements Channel<ConstChannel> {

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
    public int getValue(int x, int y) {
        return value;
    }

    @Override
    public int[][] getValues() {
        int[][] newValues = new int[getHeight()][];
        for (int[] valuesRow : newValues) {
            Arrays.fill(valuesRow, value);
        }
        return newValues;
    }

    @Override
    public void copyTo(int x, int[] buffer, int dest) {
        Arrays.fill(buffer, dest, dest + width, value);
    }

    @Override
    public ConstChannel map(IntUnaryOperator operator) {
        return new ConstChannel(height, width, operator.applyAsInt(value));
    }

}
