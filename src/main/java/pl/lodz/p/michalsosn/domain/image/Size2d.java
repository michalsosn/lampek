package pl.lodz.p.michalsosn.domain.image;

import pl.lodz.p.michalsosn.domain.util.IntBiConsumer;

import java.util.Arrays;
import java.util.Comparator;

/**
 * @author Michał Sośnicki
 */
public interface Size2d {

    Comparator<Size2d> COMPARE_HW =
            Comparator.comparingInt(Size2d::getHeight)
                      .thenComparingInt(Size2d::getWidth);

    int getHeight();

    int getWidth();

    default boolean inside(int y, int x) {
        return 0 <= y && y < getHeight()
            && 0 <= x && x < getWidth();
    }

    default boolean outside(int y, int x) {
        return y < 0 || y >= getHeight()
            || x < 0 || x >= getWidth();
    }

    default void forEach(IntBiConsumer consumer) {
        int height = getHeight();
        int width = getWidth();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                consumer.accept(y, x);
            }
        }
    }

    default int getSize() {
        return getHeight() * getWidth();
    }

    default boolean isEqualSize(Size2d other) {
        return getHeight() == other.getHeight()
            && getWidth() == other.getWidth();
    }

    static boolean allSameSize(Size2d... sized) {
        return allSameSize(Arrays.asList(sized));
    }

    static boolean allSameSize(Iterable<? extends Size2d> sizedCol) {
        Size2d last = null;
        for (Size2d sized : sizedCol) {
            if (last != null && !last.isEqualSize(sized)) {
                return false;
            }
            last = sized;
        }
        return true;
    }

}
