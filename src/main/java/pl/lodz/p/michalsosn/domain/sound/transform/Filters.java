package pl.lodz.p.michalsosn.domain.sound.transform;

import pl.lodz.p.michalsosn.domain.sound.TimeRange;
import pl.lodz.p.michalsosn.domain.sound.filter.Filter;
import pl.lodz.p.michalsosn.domain.sound.filter.LazyFilter;
import pl.lodz.p.michalsosn.domain.util.MathUtils;

import java.util.function.IntToDoubleFunction;
import java.util.function.UnaryOperator;

import static pl.lodz.p.michalsosn.domain.Lift.lift;

/**
 * @author Michał Sośnicki
 */
public final class Filters {

    private Filters() {
    }

    private static IntToDoubleFunction sinc(
            TimeRange samplingTime, double cutoffFrequency, int length, boolean causal
    ) {
        final double coef = 2 * cutoffFrequency / samplingTime.getFrequency();
        final double offset = causal ? (length - 1.0) / 2 : 0;
        return i -> coef * MathUtils.sinc(coef * (i - offset));
    }

    public static Filter sincFilter(
            TimeRange samplingTime, double cutoffFrequency, int length, boolean causal
    ) {
        return new LazyFilter(
                sinc(samplingTime, cutoffFrequency, length, causal),
                length, causal ? 0 : length / 2, samplingTime
        );
    }

    public static UnaryOperator<Filter> modulate(double amplitude,
                                                 double modulationFrequency) {
        return filter -> {
            final TimeRange samplingTime = filter.getSamplingTime();

            final double coefficient
                    = 2 * Math.PI * modulationFrequency / samplingTime.getFrequency();

            IntToDoubleFunction sine = i -> amplitude * Math.cos(i * coefficient);

            return new LazyFilter(i -> sine.applyAsDouble(i) * filter.getValue(i),
                    filter.getLength(), filter.getNegativeLength(), samplingTime);
        };
    }

    public static UnaryOperator<Filter> amplify(double amplitude) {
        return lift(p -> p * amplitude);
    }

}
