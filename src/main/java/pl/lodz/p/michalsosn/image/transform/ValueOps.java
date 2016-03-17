package pl.lodz.p.michalsosn.image.transform;

import java.util.function.IntUnaryOperator;
import java.util.stream.IntStream;

/**
 * @author Michał Sośnicki
 */
public final class ValueOps {

    private ValueOps() {
    }

    public static IntUnaryOperator negate() {
        return value -> 255 - value;
    }

    public static IntUnaryOperator changeBrightness(int change) {
        if (change > 0) {
            return value -> Math.min(value + change, 255);
        }
        else if (change < 0) {
            return value -> Math.max(value + change, 0);
        }
        else {
            return IntUnaryOperator.identity();
        }
    }

    /**
     * Modifies value of a pixel to change the contrast of an image around the average value 127.
     * The coefficient is limited to range [0.0, 128.0], because
     * at 128 the value 127 is mapped to 127, all values above 127 are mapped
     * to 255 and all values below 127 are mapped to 0. At 0 all values are
     * set to 127.
     * @param change A coefficient in range [-128.0, 128.0]
     * @return New value of a pixel.
     */
    public static IntUnaryOperator changeContrast(double change) {
        if (change < 0.0 || change > 128.0) {
            throw new IllegalArgumentException("change must be in [0.0, 128.0]");
        }

        if (change < 1.0) {
            return value -> (int) (value * change + 127 * (1 - change));
        }
        else if (change > 1.0) {
            return value -> {
                int newValue = (int) (value * change + 127 * (1 - change));
                return Math.min(255, Math.max(0, newValue));
            };
        }
        else {
            return IntUnaryOperator.identity();
        }
    }

    public static IntUnaryOperator clipBelow(int threshold) {
        return value -> value <= threshold ? 0 : value;
    }

    public static IntUnaryOperator precalculate(IntUnaryOperator operator) {
        int[] precalculated = IntStream.range(0, 256).map(operator).toArray();
        return value -> precalculated[value];
    }

}
