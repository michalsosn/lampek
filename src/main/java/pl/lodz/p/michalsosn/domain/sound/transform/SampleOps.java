package pl.lodz.p.michalsosn.domain.sound.transform;

import java.util.function.IntUnaryOperator;

import static pl.lodz.p.michalsosn.domain.sound.sound.Sound.*;

/**
 * @author Michał Sośnicki
 */
public final class SampleOps {

    private SampleOps() {
    }

    public static IntUnaryOperator scaleValue(double change) {
        if (change > 1.0) {
            return value -> Math.max(MIN_VALUE, Math.min(MAX_VALUE,
                    (int) Math.round((value - MID_VALUE) * change + MID_VALUE)
            ));
        } else if (change < 1.0) {
            return value -> (int) Math.round((value - MID_VALUE) * change + MID_VALUE);
        } else {
            return IntUnaryOperator.identity();
        }
    }

    public static IntUnaryOperator clipAbove(int threshold) {
        return value -> {
            if (value > MID_VALUE + threshold) {
                return MID_VALUE + threshold;
            } else if (value < MID_VALUE - threshold) {
                return MID_VALUE - threshold;
            } else {
                return value;
            }
        };
    }

}
