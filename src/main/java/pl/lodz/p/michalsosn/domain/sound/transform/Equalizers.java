package pl.lodz.p.michalsosn.domain.sound.transform;

import pl.lodz.p.michalsosn.domain.sound.TimeRange;
import pl.lodz.p.michalsosn.domain.sound.filter.Filter;
import pl.lodz.p.michalsosn.domain.sound.signal.BufferSignal;
import pl.lodz.p.michalsosn.domain.sound.signal.Signal;
import pl.lodz.p.michalsosn.domain.sound.sound.Sound;
import pl.lodz.p.michalsosn.domain.util.MathUtils;

import java.util.Arrays;
import java.util.function.Function;
import java.util.function.UnaryOperator;

import static pl.lodz.p.michalsosn.domain.sound.transform.Conversions.toSound;

/**
 * @author Michał Sośnicki
 */
public final class Equalizers {

    private Equalizers() {
    }

    public static Filter[] filterBase(
            TimeRange samplingTime, int length, UnaryOperator<Filter> windowFunction,
            double startFrequency, int semitones, double[] amplification
    ) {
        final int bandCount = amplification.length;
        final Filter[] filterBase = new Filter[bandCount];

        final double frequencyChange = Math.pow(2.0, semitones / 12.0);
        double bandStart = startFrequency;
        double bandEnd = startFrequency * frequencyChange;

        for (int i = 0; i < bandCount; ++i) {
            final double bandWidth = (bandEnd - bandStart) / 2.0;
            final double bandCenter = (bandStart + bandEnd) / 2.0;
            final double amplitude = MathUtils.fromDb(amplification[i]);

            final Filter filter =
                    Filters.modulate(2.0 * amplitude, bandCenter)
                    .compose(windowFunction)
                    .apply(Filters.sincFilter(samplingTime, bandWidth, length, false));

            filterBase[i] = filter;

            bandStart = bandEnd;
            bandEnd = bandEnd * frequencyChange;
        }

        return filterBase;
    }

    public static UnaryOperator<Sound> equalize(
            Filter[] filterBase, Function<Filter, Function<Sound, Signal>> convolveMaker
    ) {
        return sound -> {

            final int resultLength = sound.getLength() + filterBase[0].getLength() - 1;
            final double[] doubleValues = new double[resultLength];

            Arrays.stream(filterBase)
                    .map(convolveMaker)
                    .map(convolve -> convolve.apply(sound))
                    .forEach(signal -> {
                        final int length = signal.getLength();
                        for (int i = 0; i < length; ++i) {
                            doubleValues[i] += signal.getValue(i);
                        }
                    });

//            for (Signal filter : filterBase) {
//                final Signal convolution = convolve.apply(filter, sound);
//                result = BufferSound.of(convolution);
//            }

            return toSound(new BufferSignal(doubleValues, sound.getSamplingTime()));
        };
    }

}
