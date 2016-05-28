package pl.lodz.p.michalsosn.domain.sound.transform;

import pl.lodz.p.michalsosn.domain.complex.Complex;
import pl.lodz.p.michalsosn.domain.sound.TimeRange;
import pl.lodz.p.michalsosn.domain.sound.filter.Filter;
import pl.lodz.p.michalsosn.domain.sound.signal.BufferSignal;
import pl.lodz.p.michalsosn.domain.sound.signal.Signal;
import pl.lodz.p.michalsosn.domain.sound.sound.Sound;
import pl.lodz.p.michalsosn.domain.sound.spectrum.BufferSpectrum1d;
import pl.lodz.p.michalsosn.domain.sound.spectrum.Spectrum1d;
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
            TimeRange samplingTime, int length, Windows.Window windowFunction,
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
                    .compose(windowFunction.getFilterFunction())
                    .apply(Filters.sincFilter(samplingTime, bandWidth, length, false));

            filterBase[i] = filter;

            bandStart = bandEnd;
            bandEnd = bandEnd * frequencyChange;
        }

        return filterBase;
    }

    public static Spectrum1d joinBase(Filter[] filterBase, int otherLength) {
        Spectrum1d[] spectra = Arrays.stream(filterBase)
                .map(Convolutions.prepareFilter(otherLength))
                .toArray(Spectrum1d[]::new);
        final Spectrum1d aSpectrum = spectra[0];
        final int dftLength = aSpectrum.getLength();
        final Complex[] result = aSpectrum.values().toArray(Complex[]::new);
        for (int i = 1; i < spectra.length; ++i) {
            final Spectrum1d spectrum = spectra[i];
            for (int j = 0; j < dftLength; ++j) {
                result[j] = result[j].add(spectrum.getValue(j));
            }
        }
        return new BufferSpectrum1d(result, aSpectrum.getBasicTime());
    }

    public static UnaryOperator<Sound> equalizeSequentially(// don't do it!
            Filter[] filterBase, Function<Filter, Function<Sound, Signal>> convolveMaker
    ) {
        return sound -> {
            final Signal[] partialResults = Arrays.stream(filterBase)
                    .map(convolveMaker)
                    .map(convolve -> convolve.apply(sound))
                    .toArray(Signal[]::new);

            final int partialLength = partialResults[0].getLength();
            final double[] result = partialResults[0].values().toArray();

            for (int i = 1; i < partialResults.length; ++i) {
                final Signal partialResult = partialResults[i];
                for (int j = 0; j < partialLength; ++j) {
                    result[j] = result[j] + partialResult.getValue(j);
                }
            }

            return toSound(new BufferSignal(result, sound.getSamplingTime()));
        };
    }

}
