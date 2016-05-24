package pl.lodz.p.michalsosn.domain.sound.transform;

import pl.lodz.p.michalsosn.domain.complex.Complex;
import pl.lodz.p.michalsosn.domain.sound.Size1d;
import pl.lodz.p.michalsosn.domain.sound.signal.BufferSignal;
import pl.lodz.p.michalsosn.domain.sound.signal.Signal;
import pl.lodz.p.michalsosn.domain.sound.sound.BufferSound;
import pl.lodz.p.michalsosn.domain.sound.sound.Sound;
import pl.lodz.p.michalsosn.domain.sound.spectrum.BufferSpectrum1d;
import pl.lodz.p.michalsosn.domain.sound.spectrum.Spectrum1d;
import pl.lodz.p.michalsosn.domain.util.MathUtils;

import java.util.Arrays;

import static pl.lodz.p.michalsosn.domain.complex.Fourier.*;
import static pl.lodz.p.michalsosn.domain.sound.sound.Sound.*;


/**
 * @author Michał Sośnicki
 */
public final class DitFastFourierTransform {

    private DitFastFourierTransform() {
    }

    public static Spectrum1d transform(Sound sound) {
        checkSizePowerOfTwo(sound);

        Complex[] complexValues = sound.values()
                .map(v -> v - MID_VALUE)
                .mapToObj(Complex::ofRe)
                .toArray(Complex[]::new);

        int length = sound.getLength();
        Complex[] kernel = fourierBasis(length, length / 2);
        fft(complexValues, kernel, false);

        return new BufferSpectrum1d(complexValues, sound.getSamplingTime());
    }

    public static Spectrum1d transform(Signal signal) {
        checkSizePowerOfTwo(signal);

        Complex[] complexValues = signal.values()
                .mapToObj(Complex::ofRe)
                .toArray(Complex[]::new);

        int length = signal.getLength();
        Complex[] kernel = fourierBasis(length, length / 2);
        fft(complexValues, kernel, false);

        return new BufferSpectrum1d(complexValues, signal.getSamplingTime());
    }

    public static Spectrum1d transform(Spectrum1d spectrum) {
        checkSizePowerOfTwo(spectrum);

        Complex[] complexValues = spectrum.values().toArray(Complex[]::new);
        int length = spectrum.getLength();
        Complex[] kernel = fourierBasis(length, length / 2);
        fft(complexValues, kernel, false);

        return new BufferSpectrum1d(complexValues, spectrum.getBasicTime());
    }

    public static Sound inverse(Spectrum1d spectrum) {
        checkSizePowerOfTwo(spectrum);

        Complex[] values = spectrum.values().toArray(Complex[]::new);

        int length = spectrum.getLength();
        Complex[] kernel = inverseBasis(length, length / 2);
        fft(values, kernel, true);

        int[] integerValues = Arrays.stream(values)
                .mapToInt(v -> Math.max(MIN_VALUE, Math.min(MAX_VALUE,
                        (int) Math.round(v.getRe() + MID_VALUE)
                ))).toArray();

        return new BufferSound(integerValues, spectrum.getBasicTime());
    }

    public static Spectrum1d inverseSpectrum(Spectrum1d spectrum) {
        checkSizePowerOfTwo(spectrum);

        Complex[] values = spectrum.values().toArray(Complex[]::new);

        int length = spectrum.getLength();
        Complex[] kernel = inverseBasis(length, length / 2);
        fft(values, kernel, true);

        return new BufferSpectrum1d(values, spectrum.getBasicTime());
    }

    public static Signal inverseSignal(Spectrum1d spectrum) {
        checkSizePowerOfTwo(spectrum);

        Complex[] values = spectrum.values().toArray(Complex[]::new);

        int length = spectrum.getLength();
        Complex[] kernel = inverseBasis(length, length / 2);
        fft(values, kernel, true);

        double[] doubleValues = Arrays.stream(values)
                .mapToDouble(Complex::getRe)
                .toArray();

        return new BufferSignal(doubleValues, spectrum.getBasicTime());
    }

    private static void checkSizePowerOfTwo(Size1d size1d) {
        int length = size1d.getLength();
        if (!MathUtils.isPowerOfTwo(length)) {
            throw new IllegalArgumentException(
                    "Length not a power of two but " + length
            );
        }
    }

}
