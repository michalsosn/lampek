package pl.lodz.p.michalsosn.domain.image.spectrum;

import pl.lodz.p.michalsosn.domain.Lift;
import pl.lodz.p.michalsosn.domain.image.Size2d;

import java.util.HashMap;
import java.util.Map;
import java.util.function.UnaryOperator;

/**
 * @author Michał Sośnicki
 */
public final class ImageSpectrum implements Size2d, Lift<UnaryOperator<Spectrum>, ImageSpectrum> {

    private final Map<String, ? extends Spectrum> spectra;

    public ImageSpectrum(Map<String, ? extends Spectrum> spectra) {
        Spectrum last = null;
        for (Spectrum spectrum : spectra.values()) {
            if (last != null && !last.isEqualSize(spectrum)) {
                throw new IllegalArgumentException(
                        "Spectra " + last + " and " + spectrum
                                + " have different sizes"
                );
            }
            last = spectrum;
        }
        this.spectra = spectra;
    }

    public Map<String, Spectrum> getSpectra() {
        return new HashMap<>(spectra);
    }

    @Override
    public int getHeight() {
        for (Spectrum spectrum : spectra.values()) {
            return spectrum.getHeight();
        }
        return 0;
    }

    @Override
    public int getWidth() {
        for (Spectrum spectrum : spectra.values()) {
            return spectrum.getWidth();
        }
        return 0;
    }

    @Override
    public ImageSpectrum map(UnaryOperator<Spectrum> spectrumMapper) {
        Map<String, Spectrum> newSpectra = new HashMap<>();
        spectra.forEach((name, spectrum) ->
                newSpectra.put(name, spectrumMapper.apply(spectrum))
        );
        return new ImageSpectrum(newSpectra);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ImageSpectrum that = (ImageSpectrum) o;

        return isEqualSize(that) && spectra.equals(that.spectra);

    }

    @Override
    public int hashCode() {
        return spectra.hashCode();
    }

    @Override
    public String toString() {
        return "ImageSpectrum{"
             + "spectra=" + spectra
             + '}';
    }
}
