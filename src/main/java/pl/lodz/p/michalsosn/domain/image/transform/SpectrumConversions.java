package pl.lodz.p.michalsosn.domain.image.transform;

import pl.lodz.p.michalsosn.domain.image.channel.BufferChannel;
import pl.lodz.p.michalsosn.domain.image.channel.Channel;
import pl.lodz.p.michalsosn.domain.image.spectrum.Complex;
import pl.lodz.p.michalsosn.domain.image.spectrum.Spectrum;

import java.util.Arrays;
import java.util.DoubleSummaryStatistics;
import java.util.function.ToDoubleFunction;

import static pl.lodz.p.michalsosn.domain.image.channel.Image.MAX_VALUE;
import static pl.lodz.p.michalsosn.domain.image.channel.Image.MIN_VALUE;

/**
 * @author Michał Sośnicki
 */
public final class SpectrumConversions {

    private SpectrumConversions() {
    }

    public static Channel spectrumToRe(Spectrum spectrum) {
        return mapComplexWithLog10(spectrum, Complex::getRe);
    }

    public static Channel spectrumToIm(Spectrum spectrum) {
        return mapComplexWithLog10(spectrum, Complex::getIm);
    }

    public static Channel spectrumToAbs(Spectrum spectrum) {
        return mapComplexWithLog10(spectrum, Complex::getAbs);
    }

    public static Channel spectrumToPhase(Spectrum spectrum) {
        return mapComplexWithLog10(spectrum, Complex::getPhase);
    }

    private static Channel mapComplexWithLog10(
            Spectrum spectrum, ToDoubleFunction<Complex> mapper
    ) {
        Complex[][] values = spectrum.copyValues();
        double[][] absValues = Arrays.stream(values).map(row ->
                Arrays.stream(row)
                        .mapToDouble(mapper)
                        .map(Math::log10)
                        .toArray()
        ).toArray(double[][]::new);
        return new BufferChannel(normalizeToChannel(absValues));
    }

    private static int[][] normalizeToChannel(double[][] values) {
        DoubleSummaryStatistics spectrumStatistics
                = Arrays.stream(values).flatMapToDouble(Arrays::stream)
                .summaryStatistics();
        double min = spectrumStatistics.getMin();
        double max = spectrumStatistics.getMax();
        double range = max - min;
        return Arrays.stream(values).map(row ->
                Arrays.stream(row).mapToInt(value -> (int) Math.round(
                        (value - min) / range * MAX_VALUE + MIN_VALUE
                )).toArray()
        ).toArray(int[][]::new);
    }


}
