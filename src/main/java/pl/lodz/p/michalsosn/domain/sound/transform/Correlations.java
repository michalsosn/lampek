package pl.lodz.p.michalsosn.domain.sound.transform;

import pl.lodz.p.michalsosn.domain.complex.Complex;
import pl.lodz.p.michalsosn.domain.sound.signal.BufferSignal;
import pl.lodz.p.michalsosn.domain.sound.signal.LazySignal;
import pl.lodz.p.michalsosn.domain.sound.signal.Signal;
import pl.lodz.p.michalsosn.domain.sound.sound.LazySound;
import pl.lodz.p.michalsosn.domain.sound.sound.Sound;
import pl.lodz.p.michalsosn.domain.sound.spectrum.BufferSpectrum1d;
import pl.lodz.p.michalsosn.domain.sound.spectrum.Spectrum1d;
import pl.lodz.p.michalsosn.domain.util.MathUtils;

import java.util.function.Function;

/**
 * @author Michał Sośnicki
 */
public final class Correlations {

    public enum CorrelationType {
        CYCLIC(Correlations::correlateCyclicTime, autocorrelateCyclic()),
        LINEAR(Correlations::correlateLinearTime, autocorrelateLinear());

        private final Function<Sound, Function<Sound, Signal>> correlation;
        private final Function<Sound, Signal> autocorrelation;

        CorrelationType(Function<Sound, Function<Sound, Signal>> correlation,
                        Function<Sound, Signal> autocorrelation) {
            this.correlation = correlation;
            this.autocorrelation = autocorrelation;
        }

        public Function<Sound, Function<Sound, Signal>> getCorrelation() {
            return correlation;
        }

        public Function<Sound, Signal> getAutocorrelation() {
            return autocorrelation;
        }
    }

    private Correlations() {
    }

    public static Function<Sound, Signal> correlateCyclicTime(Sound pattern) {
        return sound -> {
            if (!pattern.getSamplingTime().equals(sound.getSamplingTime())) {
                throw new IllegalArgumentException("Sampling times of " + pattern
                                                 + " and " + sound + " are different");
            }

            int soundLength = sound.getLength();
            int patternLength = pattern.getLength();

            double[] values = new double[soundLength];
            int safeLength = Math.max(0, soundLength - patternLength);
            for (int i = 0; i < safeLength; i++) {
                double result = 0;
                for (int j = 0; j < patternLength; j++) {
                     result += (double) pattern.getValue(j) * sound.getValue(i + j);
                }
                values[i] = result;
            }
            for (int i = safeLength; i < soundLength; i++) {
                double result = 0;
                for (int j = 0; j < patternLength; j++) {
                    result += (double) pattern.getValue(j)
                            * sound.getValue((i + j) % soundLength);
                }
                values[i] = result;
            }

            return new BufferSignal(values, sound.getSamplingTime());
        };
    }

    public static Function<Sound, Signal> correlateLinearTime(Sound pattern) {
        return sound -> {
            if (!pattern.getSamplingTime().equals(sound.getSamplingTime())) {
                throw new IllegalArgumentException("Sampling times of " + pattern
                                                 + " and " + sound + " are different");
            }

            final int soundLength = sound.getLength();
            final int patternLength = pattern.getLength();
            final int resultLength = soundLength + patternLength - 1;

            final double[] values = new double[resultLength];
            for (int i = 0; i < resultLength; i++) {
                final int shiftedI = i - patternLength + 1;
                final int from = Math.max(-shiftedI, 0);
                final int to = Math.min(resultLength - i, patternLength);
                double result = 0;
                for (int j = from; j < to; j++) {
                    result += (double) pattern.getValue(j) * sound.getValue(shiftedI + j);
                }
                values[i] = result;
            }

            return new BufferSignal(values, sound.getSamplingTime());
        };
    }

    /**
     * Uses naive method of cross multiplying all values
     * @return Autocorrelation of a sound
     */
    public static Function<Sound, Signal> autocorrelateCyclicTime() {
        return sound -> correlateCyclicTime(sound).apply(sound);
    }

    public static Function<Sound, Signal> autocorrelateLinearTime() {
        return sound -> {
            Signal result = correlateLinearTime(sound).apply(sound);
            int halfLength = result.getLength() / 2;
            return new LazySignal(p -> result.getValue(p + halfLength),
                                  halfLength + 1, result.getSamplingTime());
        };
    }

    /**
     * Uses Wiener–Khinchin algorithm
     * @param zeroMean Set mean value to zero (easy in frequency domain)
     * @return Autocorrelation of a sound
     */
    public static Function<Sound, Signal> autocorrelateCyclicFrequency(
            boolean zeroMean
    ) {
        return sound -> {
            Spectrum1d transform = DitFastFourierTransform.transform(sound);

            Complex[] powerValues = transform.values()
                    .map(value -> Complex.ofRe(value.getAbsSquare()))
                    .toArray(Complex[]::new);
            if (zeroMean && powerValues.length > 0) {
                powerValues[0] = Complex.ZERO;
            }

            Spectrum1d powerSpectrum
                    = new BufferSpectrum1d(powerValues, sound.getSamplingTime());

            return DitFastFourierTransform.inverseSignal(powerSpectrum);
        };
    }

    public static Function<Sound, Signal> autocorrelateLinearFrequency(
            boolean zeroMean
    ) {
        return sound -> {
            final int length = sound.getLength();
            Sound padded = new LazySound(p -> p < length ? sound.getValue(p) : 0,
                                         2 * length, sound.getSamplingTime());
            Signal result = autocorrelateCyclicFrequency(zeroMean).apply(padded);
            return new LazySignal(result::getValue, length, sound.getSamplingTime());
        };
    }

    public static Function<Sound, Signal> autocorrelateCyclic(boolean zeroMean) {
        return sound -> {
            if (MathUtils.isPowerOfTwo(sound.getLength())) {
                return autocorrelateCyclicFrequency(zeroMean).apply(sound);
            } else {
                Signal result = autocorrelateCyclicTime().apply(sound);
                if (zeroMean && result.getLength() > 0) {
                    double mean = result.values().average().getAsDouble();
                    return new LazySignal(p -> result.getValue(p) - mean,
                                          result.getLength(), result.getSamplingTime());
                } else {
                    return result;
                }
            }
        };
    }

    public static Function<Sound, Signal> autocorrelateLinear(boolean zeroMean) {
        return sound -> {
            if (MathUtils.isPowerOfTwo(sound.getLength())) {
                return autocorrelateLinearFrequency(zeroMean).apply(sound);
            } else {
                Signal result =  autocorrelateLinearTime().apply(sound);
                if (zeroMean && result.getLength() > 0) {
                    double mean = result.values().average().getAsDouble();
                    return new LazySignal(p -> result.getValue(p) - mean,
                            result.getLength(), result.getSamplingTime());
                } else {
                    return result;
                }
            }
        };
    }

    public static Function<Sound, Signal> autocorrelateCyclic() {
        return autocorrelateCyclic(false);
    }

    public static Function<Sound, Signal> autocorrelateLinear() {
        return autocorrelateLinear(false);
    }

}
