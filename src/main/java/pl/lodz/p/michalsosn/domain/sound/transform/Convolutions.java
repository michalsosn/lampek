package pl.lodz.p.michalsosn.domain.sound.transform;

import pl.lodz.p.michalsosn.domain.complex.Complex;
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

import static pl.lodz.p.michalsosn.domain.sound.transform.Extensions.*;

/**
 * @author Michał Sośnicki
 */
public final class Convolutions {

    private Convolutions() {
    }

    public static Function<Sound, Signal> convolveLinearTime(Filter filter) {
        return sound -> {
            if (!filter.getSamplingTime().equals(sound.getSamplingTime())) {
                throw new IllegalArgumentException("Sampling times of " + filter
                                                 + " and " + sound + " are different");
            }

            final int soundLength = sound.getLength();
            final int filterPositive = filter.getPositiveLength();
            final int filterNegative = filter.getNegativeLength();
            final int resultLength = soundLength + filterPositive - 1;

            final double[] values = new double[resultLength];
            for (int i = 0; i < resultLength; i++) {
                final int from = Math.max(i - filterPositive + 1, 0);
                final int to = Math.min(i + filterNegative + 1, soundLength);
                double result = 0;
                for (int j = from; j < to; j++) {
                    result += (double) sound.getValue(j) * filter.getValue(i - j);
                }
                values[i] = result;
            }

            return new BufferSignal(values, sound.getSamplingTime());
        };
    }

    public static Function<Sound, Signal> convolveLinearFrequency(Filter filter) {
        return sound -> {
            if (!filter.getSamplingTime().equals(sound.getSamplingTime())) {
                throw new IllegalArgumentException("Sampling times of " + filter
                                                 + " and " + sound + " are different");
            }

            final int soundLength = sound.getLength();
            final int filterLength = filter.getLength();
            final int minDftLength = soundLength + filterLength - 1;
            final int dftLength = MathUtils.isPowerOfTwo(minDftLength)
                    ? minDftLength : 1 << MathUtils.log2(minDftLength) + 1;
            final int filterPositive = filter.getPositiveLength();
            final int resultLength = soundLength + filterPositive - 1;

            final Filter paddedFilter = zeroPadFilter(dftLength).apply(filter);
            final Spectrum1d filterSpectrum = DitFastFourierTransform.transform(
                    Conversions.toSpectrum1d(paddedFilter)
            );

            final Sound paddedSound = zeroPadSound(dftLength).apply(sound);
            final Spectrum1d soundSpectrum = DitFastFourierTransform.transform(
                    Conversions.toSpectrum1d(paddedSound)
            );

            final Complex[] complexValues
                    = soundSpectrum.values().toArray(Complex[]::new);
            for (int i = 0; i < dftLength; ++i) {
                final Complex kernelValue = filterSpectrum.getValue(i);
                complexValues[i] = complexValues[i].multiply(kernelValue);
            }

            final Spectrum1d multipliedSpectrum
                    = new BufferSpectrum1d(complexValues,sound.getSamplingTime());
            final Signal result = DitFastFourierTransform.inverse(multipliedSpectrum);
            return shortenSignal(0, resultLength).apply(result);
        };
    }

    public static Function<Sound, Signal> convolveLinearOverlapAdd(
            UnaryOperator<Signal> windowFunction, int windowLength, int hopSize,
            Filter filter
    ) {
        return sound -> {
            if (!filter.getSamplingTime().equals(sound.getSamplingTime())) {
                throw new IllegalArgumentException("Sampling times of " + filter
                                                 + " and " + sound + " are different");
            }

            final Signal signal = Conversions.toSignal(sound);
            final int signalLength = signal.getLength();
            final int filterLength = filter.getLength();
            final int minDftLength = windowLength + filterLength - 1;
            final int dftLength = MathUtils.isPowerOfTwo(minDftLength)
                    ? minDftLength : 1 << MathUtils.log2(minDftLength) + 1;
            final int filterPositive = filter.getPositiveLength();
            final int resultLength = signalLength + filterPositive - 1;
            final int filterNegative = filter.getNegativeLength();

            final Filter paddedFilter = zeroPadFilter(dftLength).apply(filter);
            final Spectrum1d filterSpectrum = DitFastFourierTransform.transform(
                    Conversions.toSpectrum1d(paddedFilter)
            );
            final Complex[] filterSpectrumValues
                    = filterSpectrum.values().toArray(Complex[]::new);

            final Signal paddedSignal = zeroPadSignal(signalLength + 2 * windowLength - 2)
                    .compose(delaySignal(windowLength - 1)).apply(signal);

            return Windows.overlapAdd(windowLength, hopSize, window -> {
                final Signal paddedWindow = windowFunction
                        .andThen(zeroPadSignal(dftLength))
                        .andThen(rotateLeftSignal(dftLength - filterNegative))
                        .apply(window);
                final Spectrum1d windowSpectrum = DitFastFourierTransform.transform(
                        Conversions.toSpectrum1d(paddedWindow)
                );
                final Complex[] values = Arrays.copyOf(filterSpectrumValues, dftLength);
                for (int i = 0; i < dftLength; ++i) {
                    values[i] = values[i].multiply(windowSpectrum.getValue(i));
                }
                final Spectrum1d multipliedSpectrum
                        = new BufferSpectrum1d(values, window.getSamplingTime());
                return DitFastFourierTransform.inverse(multipliedSpectrum);
            }).andThen(shortenSignal(windowLength + filterNegative - 1, resultLength))
            .apply(paddedSignal);
        };
    }

    public static Function<Sound, Signal> convolveLinear(Filter kernel) {
        return sound -> {
            if (MathUtils.isPowerOfTwo(sound.getLength())) {
                return convolveLinearFrequency(kernel).apply(sound);
            } else {
                return convolveLinearTime(kernel).apply(sound);
            }
        };
    }

}
