package pl.lodz.p.michalsosn.domain.image.transform;

import pl.lodz.p.michalsosn.domain.image.Size2d;
import pl.lodz.p.michalsosn.domain.image.channel.BufferChannel;
import pl.lodz.p.michalsosn.domain.image.channel.Channel;
import pl.lodz.p.michalsosn.domain.image.channel.GrayImage;
import pl.lodz.p.michalsosn.domain.image.channel.Image;
import pl.lodz.p.michalsosn.domain.image.spectrum.Complex;
import pl.lodz.p.michalsosn.domain.image.spectrum.ImageSpectrum;
import pl.lodz.p.michalsosn.domain.image.spectrum.Spectrum;
import pl.lodz.p.michalsosn.domain.util.MathUtils;

import java.util.Arrays;
import java.util.Collection;
import java.util.DoubleSummaryStatistics;
import java.util.function.ToDoubleFunction;

import static java.lang.Math.*;
import static java.util.Arrays.stream;
import static pl.lodz.p.michalsosn.domain.image.channel.Image.MAX_VALUE;
import static pl.lodz.p.michalsosn.domain.image.channel.Image.MIN_VALUE;

/**
 * @author Michał Sośnicki
 */
public final class SpectrumConversions {

    private SpectrumConversions() {
    }

    public static Channel spectrumToRe(Spectrum spectrum) {
        return mapComplexSymmetricLog10(spectrum.copyValues(), Complex::getRe);
    }

    public static Channel spectrumToIm(Spectrum spectrum) {
        return mapComplexSymmetricLog10(spectrum.copyValues(), Complex::getIm);
    }

    public static Channel spectrumToAbs(Spectrum spectrum) {
        Complex[][] values = spectrum.copyValues();
        double[][] absValues = stream(values).map(row ->
                stream(row).mapToDouble(Complex::getAbs).toArray()
        ).toArray(double[][]::new);
        return mapDoubleWithLog10(absValues);
    }

    public static Channel spectrumToPhase(Spectrum spectrum) {
        double scale = 255 / 2 * PI;
        int[][] absValues = stream(spectrum.copyValues()).map(row ->
                stream(row).mapToDouble(Complex::getPhase)
                           .mapToInt(v -> (int) round(
                                   MathUtils.mod(v + PI, 2 * PI) * scale
                           )).toArray()
        ).toArray(int[][]::new);
        return new BufferChannel(absValues);
    }

    public static Channel spectraToAbs(Collection<Spectrum> spectra) {
        if (!Size2d.allSameSize(spectra)) {
            throw new IllegalArgumentException("Spectra differ in size");
        }
        Spectrum example = spectra.stream().findAny().orElseThrow(() ->
            new IllegalArgumentException("Can't average empty set")
        );

        int height = example.getHeight();
        int width = example.getWidth();
        double[][] absValues = new double[height][width];

        for (Spectrum spectrum : spectra) {
            spectrum.forEach((y, x) ->
                    absValues[y][x] += spectrum.getValue(y, x).getAbs()
            );
        }

        int spectraCount = spectra.size();
        example.forEach((y, x) ->
                absValues[y][x] = absValues[y][x] / spectraCount
        );

        return mapDoubleWithLog10(absValues);
    }

    public static Image presentSpectrum(ImageSpectrum imageSpectrum) {
        Collection<Spectrum> spectra = imageSpectrum.getSpectra().values();
        Channel absChannel = SpectrumConversions.spectraToAbs(spectra);
        return new GrayImage(absChannel);
    }

    private static Channel mapDoubleWithLog10(double[][] values) {
        DoubleSummaryStatistics summaryStatistics = Arrays.stream(values)
                .flatMapToDouble(Arrays::stream).summaryStatistics();
        double max = summaryStatistics.getMax();
        double min = summaryStatistics.getMin();
        double factor = MAX_VALUE / log10(max - min + 1);

        int[][] normalized = stream(values).map(row ->
            stream(row).mapToInt(value -> (int) round(
                        log10(value - min + 1) * factor + MIN_VALUE
            )).toArray()
        ).toArray(int[][]::new);

        return new BufferChannel(normalized);
    }

    private static Channel mapComplexSymmetricLog10(
            Complex[][] values, ToDoubleFunction<Complex> mapper
    ) {
        double[][] absValues = stream(values).map(row ->
                stream(row).mapToDouble(mapper).toArray()
        ).toArray(double[][]::new);
        return mapDoubleSymmetricLog10(absValues);
    }

    private static Channel mapDoubleSymmetricLog10(double[][] values) {
        DoubleSummaryStatistics summaryStatistics = Arrays.stream(values)
                .flatMapToDouble(Arrays::stream).summaryStatistics();
        double max = summaryStatistics.getMax();
        double min = summaryStatistics.getMin();
        double absMax = max(max, -min);

        double factor = (MAX_VALUE / 2) / log10(absMax + 1);
        double midValue = MAX_VALUE / 2 + MIN_VALUE;

        int[][] normalized = stream(values).map(row ->
                stream(row).mapToInt(value -> (int) round(
                    signum(value) * log10(abs(value) + 1) * factor + midValue
                )).toArray()
        ).toArray(int[][]::new);

        return new BufferChannel(normalized);
    }

}
