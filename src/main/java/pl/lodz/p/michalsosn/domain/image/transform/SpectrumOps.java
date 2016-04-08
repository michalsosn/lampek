package pl.lodz.p.michalsosn.domain.image.transform;

import pl.lodz.p.michalsosn.domain.image.spectrum.BufferSpectrum;
import pl.lodz.p.michalsosn.domain.image.spectrum.Complex;
import pl.lodz.p.michalsosn.domain.image.spectrum.Spectrum;

import java.util.Arrays;
import java.util.function.UnaryOperator;

import static pl.lodz.p.michalsosn.domain.image.transform.DitFastFourierTransform.*;

/**
 * @author Michał Sośnicki
 */
public final class SpectrumOps {
    private SpectrumOps() {
    }

    public static UnaryOperator<Spectrum> shiftPhase(int k, int l) {
        return spectrum -> {
            int height = spectrum.getHeight();
            int width = spectrum.getWidth();

            Complex[] kernelHeight = fourierBasis(height);
            Complex[] kernelWidth = fourierBasis(width);
            Complex kernelPlus = inverseBasis(2)[(k + l) % 2];
            Complex[] kernelPrecalc = Arrays.stream(kernelWidth)
                    .map(kernelPlus::multiply)
                    .toArray(Complex[]::new);

            Complex[][] values = spectrum.copyValues();
            for (int y = 0; y < height; ++y) {
                for (int x = 0; x < width; ++x) {
                    values[y][x] = values[y][x]
                            .multiply(kernelHeight[y * k % height])
                            .multiply(kernelPrecalc[x * l % width]);
                }
            }

            return new BufferSpectrum(values);
        };
    }
}
