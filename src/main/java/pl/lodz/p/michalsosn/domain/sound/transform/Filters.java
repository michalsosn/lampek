package pl.lodz.p.michalsosn.domain.sound.transform;

import pl.lodz.p.michalsosn.domain.sound.TimeRange;
import pl.lodz.p.michalsosn.domain.sound.signal.LazySignal;
import pl.lodz.p.michalsosn.domain.sound.signal.Signal;
import pl.lodz.p.michalsosn.domain.util.MathUtils;

import java.util.function.IntToDoubleFunction;

/**
 * @author Michał Sośnicki
 */
public class Filters {

    public static IntToDoubleFunction sinc(
            TimeRange samplingTime, TimeRange cutoffTime, int length
    ) {
        final double coef = 2 * cutoffTime.getFrequency() / samplingTime.getFrequency();
        double offset = (length - 1.0) / 2;
        return i -> coef * MathUtils.sinc(coef * (i - offset));
    }

    public static Signal sincResponse(
            TimeRange samplingTime, TimeRange cutoffTime, int length
    ) {
        return new LazySignal(
                sinc(samplingTime, cutoffTime, length), length, samplingTime
        );
    }

}
