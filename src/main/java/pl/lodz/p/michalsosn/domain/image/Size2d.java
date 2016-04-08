package pl.lodz.p.michalsosn.domain.image;

import pl.lodz.p.michalsosn.util.IntBiConsumer;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;

/**
 * @author Michał Sośnicki
 */
public interface Size2d {

    Comparator<Size2d> COMPARE_XY =
            Comparator.comparingInt(Size2d::getHeight)
                      .thenComparingInt(Size2d::getWidth);

    int getHeight();

    int getWidth();

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
        return getHeight() * getHeight();
    }

    default boolean isEqualSize(Size2d other) {
        return getHeight() == other.getHeight()
            && getWidth() == other.getWidth();
    }

    static boolean allSameSize(Size2d... sized) {
        return allSameSize(Arrays.asList(sized));
    }

    static boolean allSameSize(Collection<? extends Size2d> sized) {
        Size2d last = null;
        for (Size2d spectrum : sized) {
            if (last != null && !last.isEqualSize(spectrum)) {
                return false;
            }
            last = spectrum;
        }
        return true;
    }

}
