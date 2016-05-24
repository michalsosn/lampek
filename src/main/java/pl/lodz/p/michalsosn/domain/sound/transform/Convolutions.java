package pl.lodz.p.michalsosn.domain.sound.transform;

import pl.lodz.p.michalsosn.domain.complex.Complex;
import pl.lodz.p.michalsosn.domain.sound.signal.BufferSignal;
import pl.lodz.p.michalsosn.domain.sound.signal.LazySignal;
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
public class Convolutions {

    public static Function<Sound, Signal> convolveLinearTime(Signal kernel) {
        return sound -> {
            if (!kernel.getSamplingTime().equals(sound.getSamplingTime())) {
                throw new IllegalArgumentException("Sampling times of " + kernel
                        + " and " + sound + " are different");
            }

            final int soundLength = sound.getLength();
            final int kernelLength = kernel.getLength();
            final int resultLength = soundLength + kernelLength - 1;

            final double[] values = new double[resultLength];
            for (int i = 0; i < resultLength; i++) {
                final int from = Math.max(i - kernelLength + 1, 0);
                final int to = Math.min(i + 1, soundLength);
                double result = 0;
                for (int j = from; j < to; j++) {
                    result += (double) sound.getValue(j) * kernel.getValue(i - j);
                }
                values[i] = result;
            }

            return new BufferSignal(values, sound.getSamplingTime());
        };
    }

    public static Function<Sound, Signal> convolveLinearFrequency(Signal kernel) {
        return sound -> {
            if (!kernel.getSamplingTime().equals(sound.getSamplingTime())) {
                throw new IllegalArgumentException("Sampling times of " + kernel
                                                 + " and " + sound + " are different");
            }

            final int soundLength = sound.getLength();
            final int kernelLength = kernel.getLength();
            final int resultLength = soundLength + kernelLength - 1;

            final Signal paddedKernel = zeroPadSignal(resultLength).apply(kernel);
            final Spectrum1d kernelSpectrum
                    = DitFastFourierTransform.transform(paddedKernel);

            return convolveLinearFrequency(kernelSpectrum).apply(sound);
        };
    }

    public static Function<Sound, Signal> convolveLinearFrequency(
            Spectrum1d kernelSpectrum
    ) {
        return sound -> {
            if (!kernelSpectrum.getBasicTime().equals(sound.getSamplingTime())) {
                throw new IllegalArgumentException("Sampling times of " + kernelSpectrum
                                                 + " and " + sound + " are different");
            }

            final int resultLength = kernelSpectrum.getLength();

            final Sound paddedSound = zeroPadSound(resultLength).apply(sound);
            final Spectrum1d soundSpectrum
                    = DitFastFourierTransform.transform(paddedSound);

            final Complex[] complexValues
                    = soundSpectrum.values().toArray(Complex[]::new);
            for (int i = 0; i < resultLength; ++i) {
                final Complex kernelValue = kernelSpectrum.getValue(i);
                complexValues[i] = complexValues[i].multiply(kernelValue);
            }

            final Spectrum1d multipliedSpectrum
                    = new BufferSpectrum1d(complexValues,sound.getSamplingTime());
            return DitFastFourierTransform.inverseSignal(multipliedSpectrum);
        };
    }

    public static Function<Sound, Signal> convolveLinearOverlapAdd(
            UnaryOperator<Signal> windowFunction, int windowLength, int hopSize,
            Signal kernel
    ) {
        return sound -> {
            if (!kernel.getSamplingTime().equals(sound.getSamplingTime())) {
                throw new IllegalArgumentException("Sampling times of " + kernel
                                                 + " and " + sound + " are different");
            }

            final Signal signal = LazySignal.of(sound);
            final int signalLength = signal.getLength();
            final int kernelLength = kernel.getLength();
            final int resultLength = signalLength + kernelLength - 1;
            final int minDftLength = windowLength + kernelLength - 1;
            final int dftLength = MathUtils.isPowerOfTwo(minDftLength)
                    ? minDftLength : 1 << MathUtils.log2(minDftLength) + 1;

            final Signal paddedKernel = zeroPadSignal(dftLength).apply(kernel);
            final Spectrum1d kernelDft = DitFastFourierTransform.transform(paddedKernel);
            final Complex[] kernelCopy = kernelDft.values().toArray(Complex[]::new);

            final Signal paddedSignal = zeroPadSignal(
                    signalLength + 2 * windowLength - 2, windowLength - 1
            ).apply(signal);

            return Windows.overlapAdd(windowLength, hopSize, window -> {
                final Signal paddedWindow = windowFunction
                        .andThen(zeroPadSignal(dftLength))
                        .apply(window);
                final Spectrum1d windowDft
                        = DitFastFourierTransform.transform(paddedWindow);
                final Complex[] values = Arrays.copyOf(kernelCopy, dftLength);
                for (int i = 0; i < dftLength; i++) {
                    values[i] = windowDft.getValue(i).multiply(values[i]);
                }
                final Spectrum1d multipliedSpectrum
                        = new BufferSpectrum1d(values, window.getSamplingTime());
                return DitFastFourierTransform.inverseSignal(multipliedSpectrum);
            }).andThen(shortenSignal(windowLength - 1, resultLength)).apply(paddedSignal);
        };
    }

    public static Function<Sound, Signal> convolveLinear(Signal kernel) {
        return sound -> {
            if (MathUtils.isPowerOfTwo(sound.getLength())) {
                return convolveLinearFrequency(kernel).apply(sound);
            } else {
                return convolveLinearTime(kernel).apply(sound);
            }
        };
    }

}
