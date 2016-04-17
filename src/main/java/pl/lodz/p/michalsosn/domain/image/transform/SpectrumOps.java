package pl.lodz.p.michalsosn.domain.image.transform;

import pl.lodz.p.michalsosn.domain.image.spectrum.BufferSpectrum;
import pl.lodz.p.michalsosn.domain.image.spectrum.Complex;
import pl.lodz.p.michalsosn.domain.image.spectrum.Spectrum;

import java.util.function.UnaryOperator;

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

            Complex[][] values = spectrum.copyValues();
            double yMod = - k * 2 * Math.PI / height;
            double xMod = - l * 2 * Math.PI / width;
            double cMod = (k + l) * Math.PI;
            for (int y = 0; y < height; ++y) {
                for (int x = 0; x < width; ++x) {
                    values[y][x] = values[y][x]
                            .multiply(Complex.ofPolar(1, yMod * y + xMod * x + cMod));
                }
            }

            return new BufferSpectrum(values);
        };
    }
}
