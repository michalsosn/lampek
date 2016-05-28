package pl.lodz.p.michalsosn.domain.sound.transform;

import pl.lodz.p.michalsosn.domain.complex.Complex;
import pl.lodz.p.michalsosn.domain.sound.sound.Sound;
import pl.lodz.p.michalsosn.domain.util.ArrayUtils;

/**
 * @author Michał Sośnicki
 */
public final class Spectrograms {

    private Spectrograms() {
    }

    public static Complex[][] spectrogram(int windowLength, Sound sound) {
        final Complex[][] matrix = Windows.sliding(windowLength).apply(sound)
                .map(Extensions.zeroPadSound(windowLength))
                .map(Conversions::toSpectrum1d)
                .map(DitFastFourierTransform::transform)
                .map(spectrum -> spectrum.values()
                        .skip((windowLength - 1) / 2)
                        .toArray(Complex[]::new))
                .toArray(Complex[][]::new);
        return ArrayUtils.transposeMatrix(matrix, (h, w) -> new Complex[h][w]);
    }

}
