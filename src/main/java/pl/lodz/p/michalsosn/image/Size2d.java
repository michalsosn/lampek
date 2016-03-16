package pl.lodz.p.michalsosn.image;

import pl.lodz.p.michalsosn.util.IntBiConsumer;

import java.util.Comparator;

/**
 * @author Michał Sośnicki
 */
public interface Size2d {

    Comparator<Size2d> COMPARE_XY = Comparator.comparingInt(Size2d::getHeight)
                                              .thenComparingInt(Size2d::getWidth);

    int getHeight();

    int getWidth();

    default void forEach(IntBiConsumer consumer) {
        int height = getHeight();
        int width = getWidth();

        for (int x = 0; x < height; x++) {
            for (int y = 0; y < width; y++) {
                consumer.accept(x, y);
            }
        }
    }

}
