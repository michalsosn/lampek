package pl.lodz.p.michalsosn.domain.sound.transform;

import pl.lodz.p.michalsosn.domain.complex.Complex;
import pl.lodz.p.michalsosn.domain.sound.TimeRange;
import pl.lodz.p.michalsosn.domain.sound.filter.Filter;
import pl.lodz.p.michalsosn.domain.sound.signal.Signal;
import pl.lodz.p.michalsosn.domain.sound.sound.Sound;
import pl.lodz.p.michalsosn.domain.sound.spectrum.BufferSpectrum1d;
import pl.lodz.p.michalsosn.domain.sound.spectrum.Spectrum1d;
import pl.lodz.p.michalsosn.domain.util.MathUtils;

import java.util.function.Function;
import java.util.function.IntFunction;

import static pl.lodz.p.michalsosn.domain.sound.transform.Extensions.*;

/**
 * @author Michał Sośnicki
 */
public final class WahWahs {

    private WahWahs() {
    }

    public static IntFunction<Spectrum1d> oscillatingFilter(
            double oscillatorFrequency, double oscillationStart, double oscillationEnd,
            TimeRange samplingTime, double bandWidth, double amplification,
            int filterLength, Windows.Window filterWindow, int windowLength
    ) {
        final Filter filter = filterWindow.getFilterFunction().apply(
                Filters.sincFilter(samplingTime, bandWidth / 2, filterLength, false)
        );

        final double basicFreq = samplingTime.getFrequency();
        final double amplitude = MathUtils.fromDb(amplification);
        final double freqRatio = 2.0 * Math.PI * oscillatorFrequency / basicFreq;
        final double oscillationStartSafe = Math.min((basicFreq - bandWidth) / 2,
                Math.max(oscillationStart, bandWidth / 2)
        );
        final double oscillationEndSafe = Math.min((basicFreq - bandWidth) / 2,
                Math.max(oscillationEnd, bandWidth / 2)
        );
        final double oscillationRange = 0.5 * (oscillationStartSafe - oscillationEndSafe);
        final double oscillationMid = 0.5 * (oscillationStartSafe + oscillationEndSafe);

        final Complex one = Complex.ofRe(1);
        return sample -> {
            final double oscillationPosition
                    = Math.cos(sample * freqRatio) * oscillationRange + oscillationMid;
            final Spectrum1d filterSpectrum = Filters.modulate(
                    2.0 * amplitude, oscillationPosition)
                    .andThen(Convolutions.prepareFilter(windowLength))
                    .apply(filter);
            final Complex[] values = filterSpectrum.values().toArray(Complex[]::new);
            final int lowerRange = (int) Math.floor(
                    values.length * (oscillationPosition - bandWidth / 2) / basicFreq
            );
            final int upperRange = (int) Math.ceil(
                    values.length * (oscillationPosition + bandWidth / 2) / basicFreq
            );
            for (int i = 0; i <= lowerRange; ++i) {
                values[i] = one;
            }
            for (int i = upperRange; i < values.length - upperRange; ++i) {
                values[i] = one;
            }
            for (int i = values.length - lowerRange; i < values.length; ++i) {
                values[i] = one;
            }
            return new BufferSpectrum1d(values, samplingTime);
        };
    }

    public static Function<Sound, Signal> convolveChangingFilter(
            Windows.Window windowFunction, int windowLength, int hopSize,
            IntFunction<Spectrum1d> filterMaker, int filterPositive, int filterNegative
    ) {
        return sound -> {
            final Spectrum1d aFilter = filterMaker.apply(0);
            if (!aFilter.getBasicTime().equals(sound.getSamplingTime())) {
                throw new IllegalArgumentException("Sampling times of " + aFilter
                                                 + " and " + sound + " are different");
            }

            final Signal signal = Conversions.toSignal(sound);
            final int signalLength = signal.getLength();
            final int resultLength = signalLength + filterPositive - 1;

            final Signal paddedSignal = zeroPadSignal(signalLength + 2 * windowLength - 2)
                    .compose(delaySignal(windowLength - 1)).apply(signal);

            final int[] counter = new int[1];
            return Windows.overlapAdd(windowLength, hopSize, window -> {
                final Spectrum1d filterSpectrum = filterMaker.apply(counter[0]);
                final int dftLength = filterSpectrum.getLength();
                final Signal paddedWindow = windowFunction.getSignalFunction()
                        .andThen(zeroPadSignal(dftLength))
                        .andThen(rotateLeftSignal(dftLength - filterNegative))
                        .apply(window);
                final Spectrum1d windowSpectrum = DitFastFourierTransform.transform(
                        Conversions.toSpectrum1d(paddedWindow)
                );
                final Complex[] values = filterSpectrum.values().toArray(Complex[]::new);
                for (int i = 0; i < dftLength; ++i) {
                    values[i] = values[i].multiply(windowSpectrum.getValue(i));
                }
                final Spectrum1d multipliedSpectrum
                        = new BufferSpectrum1d(values, window.getSamplingTime());
                counter[0] += hopSize;
                return DitFastFourierTransform.inverse(multipliedSpectrum);
            }).andThen(shortenSignal(windowLength + filterNegative - 1, resultLength))
                    .apply(paddedSignal);
        };
    }

}
