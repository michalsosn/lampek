package pl.lodz.p.michalsosn.domain.image.spectrum;

import pl.lodz.p.michalsosn.domain.Lift;
import pl.lodz.p.michalsosn.domain.image.Size2d;

import java.util.HashMap;
import java.util.Map;
import java.util.function.UnaryOperator;

/**
 * @author Michał Sośnicki
 */
public final class ImageSpectrum implements Size2d, Lift<UnaryOperator<Spectrum2d>, ImageSpectrum> {

    private final Map<String, ? extends Spectrum2d> spectra;

    public ImageSpectrum(Map<String, ? extends Spectrum2d> spectra) {
        if (!Size2d.allSameSize(spectra.values())) {
            throw new IllegalArgumentException("Spectra differ in size");
        }
        this.spectra = spectra;
    }

    public Map<String, Spectrum2d> getSpectra() {
        return new HashMap<>(spectra);
    }

    @Override
    public int getHeight() {
        for (Spectrum2d spectrum : spectra.values()) {
            return spectrum.getHeight();
        }
        return 0;
    }

    @Override
    public int getWidth() {
        for (Spectrum2d spectrum : spectra.values()) {
            return spectrum.getWidth();
        }
        return 0;
    }

    @Override
    public ImageSpectrum map(UnaryOperator<Spectrum2d> spectrumMapper) {
        Map<String, Spectrum2d> newSpectra = new HashMap<>();
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
