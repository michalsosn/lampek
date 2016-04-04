package pl.lodz.p.michalsosn.domain.image.transform;

import java.util.function.IntUnaryOperator;
import java.util.stream.IntStream;

import static pl.lodz.p.michalsosn.domain.image.image.Image.MAX_VALUE;
import static pl.lodz.p.michalsosn.domain.image.image.Image.MIN_VALUE;

/**
 * @author Michał Sośnicki
 */
public final class ValueOps {

    private ValueOps() {
    }

    public static IntUnaryOperator negate() {
        return value -> MAX_VALUE - value;
    }

    public static IntUnaryOperator changeBrightness(int change) {
        if (change > 0) {
            return value -> Math.min(value + change, MAX_VALUE);
        } else if (change < 0) {
            return value -> Math.max(value + change, MIN_VALUE);
        } else {
            return IntUnaryOperator.identity();
        }
    }

    /**
     * Modifies value of a pixel to change the contrast of an image around
     * the average value 127.
     * The coefficient is limited to range [0.0, 128.0], because
     * at 128 the value 127 is mapped to 127, all values above 127 are mapped
     * to 255 and all values below 127 are mapped to 0. At 0 all values are
     * set to 127.
     * @param change A coefficient in range [0.0, 128.0]
     * @return New value of a pixel.
     */
    public static IntUnaryOperator changeContrast(double change) {
        if (change < 0.0 || change > 128.0) {
            throw new IllegalArgumentException(
                    "change must be in [0.0, 128.0]"
            );
        }

        if (change < 1.0) {
            return value -> (int) Math.round(
                    value * change + 127 * (1 - change)
            );
        } else if (change > 1.0) {
            return value -> {
                int newValue = (int) Math.round(
                        value * change + 127 * (1 - change)
                );
                return Math.min(MAX_VALUE, Math.max(MIN_VALUE, newValue));
            };
        } else {
            return IntUnaryOperator.identity();
        }
    }

    public static IntUnaryOperator clipBelow(int threshold) {
        return value -> value <= threshold ? 0 : value;
    }

    public static IntUnaryOperator precalculating(IntUnaryOperator operator) {
        int[] precalculated = IntStream
                .rangeClosed(MIN_VALUE, MAX_VALUE)
                .map(operator).toArray();
        return value -> precalculated[value];
    }

}
