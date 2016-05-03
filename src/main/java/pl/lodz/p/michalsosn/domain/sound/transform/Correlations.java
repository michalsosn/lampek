package pl.lodz.p.michalsosn.domain.sound.transform;

import pl.lodz.p.michalsosn.domain.complex.Complex;
import pl.lodz.p.michalsosn.domain.sound.signal.BufferSignal;
import pl.lodz.p.michalsosn.domain.sound.signal.Signal;
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

            long[] values = new long[soundLength];
            int safeLength = Math.max(0, soundLength - patternLength);
            for (int i = 0; i < safeLength; i++) {
                long result = 0;
                for (int j = 0; j < patternLength; j++) {
                     result += (long) pattern.getValue(j) * sound.getValue(i + j);
                }
                values[i] = result;
            }
            for (int i = safeLength; i < soundLength; i++) {
                long result = 0;
                for (int j = 0; j < patternLength; j++) {
                    result += (long) pattern.getValue(j)
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

            long[] values = new long[resultLength];
            for (int i = 0; i < resultLength; i++) {
                int shiftedI = i - patternLength + 1;
                int from = Math.max(-shiftedI, 0);
                int to = Math.min(resultLength - i, patternLength);
                long result = 0;
                for (int j = from; j < to; j++) {
                    result += pattern.getValue(j) * sound.getValue(shiftedI + j);
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

    /**
     * Uses Wiener–Khinchin algorithm
     * @return Autocorrelation of a sound
     */
    public static Function<Sound, Signal> autocorrelateCyclicWienerKhinchin() {
        return sound -> {
            Spectrum1d transform = DitFastFourierTransform.transform(sound);

            Complex[] powerValues = transform.values()
                    .map(complex -> complex.multiply(complex.conjugate()))
                    .toArray(Complex[]::new);

            BufferSpectrum1d powerSpectrum
                    = new BufferSpectrum1d(powerValues, sound.getSamplingTime());

            return DitFastFourierTransform.inverseSignal(powerSpectrum);
        };
    }

    public static Function<Sound, Signal> autocorrelateCyclic() {
        return sound -> {
            if (MathUtils.isPowerOfTwo(sound.getLength())) {
                return autocorrelateCyclicWienerKhinchin().apply(sound);
            } else {
                return autocorrelateCyclicNaive().apply(sound);
            }
        };
    }

    public static Function<Sound, Signal> autocorrelateLinear() {
        return sound -> correlateLinear(sound).apply(sound);
    }

}
