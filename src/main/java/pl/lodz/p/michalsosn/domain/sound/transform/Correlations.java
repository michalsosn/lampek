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

    private Correlations() {
    }

    public static Function<Sound, Signal> correlateCyclic(Sound pattern) {
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

    public static Function<Sound, Signal> correlateLinear(Sound pattern) {
        return sound -> {
            if (!pattern.getSamplingTime().equals(sound.getSamplingTime())) {
                throw new IllegalArgumentException("Sampling times of " + pattern
                                                 + " and " + sound + " are different");
            }

            int soundLength = sound.getLength();
            int patternLength = pattern.getLength();
            int resultLength = soundLength + patternLength - 1;

            double[] values = new double[resultLength];
            for (int i = 0; i < resultLength; i++) {
                int shiftedI = i - patternLength + 1;
                int from = Math.max(-shiftedI, 0);
                int to = Math.min(resultLength - i, patternLength);
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
    public static Function<Sound, Signal> autocorrelateCyclicNaive() {
        return sound -> correlateCyclic(sound).apply(sound);
    }

    public static Function<Sound, Signal> autocorrelateLinearNaive() {
        return sound -> {
            Signal result = correlateLinear(sound).apply(sound);
            int halfLength = result.getLength() / 2;
            return new LazySignal(p -> result.getValue(p + halfLength),
                                  halfLength + 1, result.getSamplingTime());
        };
    }

    /**
     * Uses Wiener–Khinchin algorithm
     * @return Autocorrelation of a sound
     */
    public static Function<Sound, Signal> autocorrelateCyclicWienerKhinchin(
            boolean centerZero
    ) {
        return sound -> {
            Spectrum1d transform = DitFastFourierTransform.transform(sound);

            Complex[] powerValues = transform.values()
                    .map(value -> Complex.ofRe(value.getAbsSquare()))
                    .toArray(Complex[]::new);
            if (centerZero && powerValues.length > 0) {
                powerValues[0] = Complex.ZERO;
            }

            Spectrum1d powerSpectrum
                    = new BufferSpectrum1d(powerValues, sound.getSamplingTime());

            return DitFastFourierTransform.inverseSignal(powerSpectrum);
        };
    }

    public static Function<Sound, Signal> autocorrelateLinearWienerKhinchin(
            boolean centerZero
    ) {
        return sound -> {
            final int length = sound.getLength();
            Sound padded = new LazySound(p -> p < length ? sound.getValue(p) : 0,
                                         2 * length, sound.getSamplingTime());
            Signal result = autocorrelateCyclicWienerKhinchin(centerZero).apply(padded);
            return new LazySignal(result::getValue, length, sound.getSamplingTime());
        };
    }

    public static Function<Sound, Signal> autocorrelateCyclic(boolean centerZero) {
        return sound -> {
            if (MathUtils.isPowerOfTwo(sound.getLength())) {
                return autocorrelateCyclicWienerKhinchin(centerZero).apply(sound);
            } else {
                Signal result = autocorrelateCyclicNaive().apply(sound);
                if (centerZero && result.getLength() > 0) {
                    double mean = result.values().average().getAsDouble();
                    return new LazySignal(p -> result.getValue(p) - mean,
                                          result.getLength(), result.getSamplingTime());
                } else {
                    return result;
                }
            }
        };
    }

    public static Function<Sound, Signal> autocorrelateLinear(boolean centerZero) {
        return sound -> {
            if (MathUtils.isPowerOfTwo(sound.getLength())) {
                return autocorrelateLinearWienerKhinchin(centerZero).apply(sound);
            } else {
                Signal result =  autocorrelateLinearNaive().apply(sound);
                if (centerZero && result.getLength() > 0) {
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
