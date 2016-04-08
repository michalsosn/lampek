package pl.lodz.p.michalsosn.domain.image.transform;

import pl.lodz.p.michalsosn.domain.image.Size2d;
import pl.lodz.p.michalsosn.domain.image.channel.BufferChannel;
import pl.lodz.p.michalsosn.domain.image.channel.Channel;
import pl.lodz.p.michalsosn.domain.image.spectrum.Complex;
import pl.lodz.p.michalsosn.domain.image.spectrum.Spectrum;

import java.util.Arrays;
import java.util.Collection;
import java.util.DoubleSummaryStatistics;
import java.util.function.ToDoubleFunction;

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
        return mapComplexWithLog10(spectrum.copyValues(), Complex::getRe);
    }

    public static Channel spectrumToIm(Spectrum spectrum) {
        return mapComplexWithLog10(spectrum.copyValues(), Complex::getIm);
    }

    public static Channel spectrumToAbs(Spectrum spectrum) {
        return mapComplexWithLog10(spectrum.copyValues(), Complex::getAbs);
    }

    public static Channel spectrumToPhase(Spectrum spectrum) {
        return mapComplexWithLog10(spectrum.copyValues(), Complex::getPhase);
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

    private static Channel mapComplexWithLog10(
            Complex[][] values, ToDoubleFunction<Complex> mapper
    ) {
        double[][] absValues = stream(values).map(row ->
                stream(row).mapToDouble(mapper).toArray()
        ).toArray(double[][]::new);
        return mapDoubleWithLog10(absValues);
    }

    private static Channel mapDoubleWithLog10(double[][] values) {
        DoubleSummaryStatistics summaryStatistics = Arrays.stream(values)
                .flatMapToDouble(Arrays::stream).summaryStatistics();
        double min = summaryStatistics.getMin();
        double max = summaryStatistics.getMax();
        double logRange = Math.log10(max - min);

        int[][] normalized = stream(values).map(row ->
                stream(row).mapToInt(value -> (int) Math.round(
                    Math.log10(value - min) / logRange * MAX_VALUE + MIN_VALUE
                )).toArray()
        ).toArray(int[][]::new);

        return new BufferChannel(normalized);
    }


}
