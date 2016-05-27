package pl.lodz.p.michalsosn.domain.sound.transform;

import pl.lodz.p.michalsosn.domain.complex.Complex;
import pl.lodz.p.michalsosn.domain.sound.Size1d;
import pl.lodz.p.michalsosn.domain.sound.signal.BufferSignal;
import pl.lodz.p.michalsosn.domain.sound.signal.Signal;
import pl.lodz.p.michalsosn.domain.sound.spectrum.BufferSpectrum1d;
import pl.lodz.p.michalsosn.domain.sound.spectrum.Spectrum1d;
import pl.lodz.p.michalsosn.domain.util.MathUtils;

import java.util.Arrays;

import static pl.lodz.p.michalsosn.domain.complex.Fourier.*;


/**
 * @author Michał Sośnicki
 */
public final class DitFastFourierTransform {

    private DitFastFourierTransform() {
    }

    public static Spectrum1d transform(Spectrum1d spectrum) {
        checkSizePowerOfTwo(spectrum);

        Complex[] complexValues = spectrum.values().toArray(Complex[]::new);
        int length = spectrum.getLength();
        Complex[] kernel = fourierBasis(length, length / 2);
        fft(complexValues, kernel, false);

        return new BufferSpectrum1d(complexValues, spectrum.getBasicTime());
    }

    public static Signal inverse(Spectrum1d spectrum) {
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
