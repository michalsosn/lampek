package pl.lodz.p.michalsosn.domain.sound;

import java.util.Arrays;
import java.util.stream.IntStream;

/**
 * @author Michał Sośnicki
 */
public interface Size1d {

    int getLength();

    default boolean inside(int p) {
        return 0 <= p && p < getLength();
    }

    default boolean outside(int p) {
        return p < 0 || p >= getLength();
    }

    default IntStream stream() {
        return IntStream.range(0, getLength());
    }

    default boolean isEqualLength(Size1d other) {
        return getLength() == other.getLength();
    }

    static boolean allSameLength(Size1d... sized) {
        return allSameLength(Arrays.asList(sized));
    }

    static boolean allSameLength(Iterable<? extends Size1d> sizedCol) {
        Size1d last = null;
        for (Size1d sized : sizedCol) {
            if (last != null && !last.isEqualLength(sized)) {
                return false;
            }
            last = sized;
        }
        return true;
    }

}
