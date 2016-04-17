package pl.lodz.p.michalsosn.domain.image.transform.segmentation;

import java.util.function.Function;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;

/**
 * @author Michał Sośnicki
 */
public final class DoubleArrayRegion implements SplitRegion<Double, DoubleStream> {
    private final double[][] values;
    private final int y;
    private final int x;
    private final int height;
    private final int width;

    public DoubleArrayRegion(double[][] values, int y, int x,
                             int height, int width) {
        this.values = values;
        this.y = y;
        this.x = x;
        this.height = height;
        this.width = width;
    }

    @Override
    public DoubleStream values() {
        return IntStream.range(y, y + height).mapToObj(curY ->
                IntStream.range(x, x + width).mapToDouble(curX -> values[curY][curX])
        ).flatMapToDouble(Function.identity());
    }

    @Override
    public SplitRegion<Double, DoubleStream> subRegion(SubRegion subRegion) {
        int midWidth = width / 2;
        int midHeight = height / 2;
        switch (subRegion) {
            case BOTTOM_LEFT:
                return new DoubleArrayRegion(
                        values, y, x, midHeight, midWidth
                );
            case BOTTOM_RIGHT:
                return new DoubleArrayRegion(
                        values, y, x + midWidth, midHeight, width - midWidth
                );
            case TOP_LEFT:
                return new DoubleArrayRegion(
                        values, y + midHeight, x, height - midHeight, midWidth
                );
            case TOP_RIGHT:
                return new DoubleArrayRegion(
                        values, y + midHeight, x + midWidth,
                        height - midHeight, width - midWidth
                );
            default:
                throw new IllegalArgumentException("Unknown sub region " + subRegion);
        }
    }

    @Override
    public int getY() {
        return y;
    }

    @Override
    public int getX() {
        return x;
    }

    @Override
    public int getHeight() {
        return height;
    }

    @Override
    public int getWidth() {
        return width;
    }
}


