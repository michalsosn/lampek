package pl.lodz.p.michalsosn.rest.support;

import pl.lodz.p.michalsosn.domain.complex.Complex;
import pl.lodz.p.michalsosn.domain.sound.spectrum.Spectrum1d;

import java.io.IOException;
import java.util.stream.IntStream;

import static pl.lodz.p.michalsosn.entities.ResultEntity.SoundSpectrumResultEntity;

/**
 * @author Michał Sośnicki
 */
public class SoundSpectrumChartPack {

    private static final int PLOT_SIZE = 600;

    private final double[] abs;
    private final double[] phase;
    private final double startFrequency;
    private final double endFrequency;

    public SoundSpectrumChartPack(SoundSpectrumResultEntity result) throws IOException {
        this(result, null, null);
    }

    public SoundSpectrumChartPack(
            SoundSpectrumResultEntity result, Double startFreq, Double endFreq
    ) throws IOException {
        Spectrum1d spectrum = result.getSpectrum();
        int length = spectrum.getLength();
        double fullFrequency = spectrum.getBasicTime().getFrequency();

        if (startFreq == null || startFreq < 0) {
            startFreq = 0.0;
        }
        if (endFreq == null || endFreq > fullFrequency) {
            endFreq = fullFrequency;
        }

        int sampleStart = (int) Math.ceil(startFreq / fullFrequency * length);
        int sampleEnd = (int) Math.floor(endFreq / fullFrequency * length);
        int spanLength = sampleEnd - sampleStart;

        int step = (int) Math.floor((double) spanLength / PLOT_SIZE);
        if (step < 0) {
            this.abs = new double[0];
            this.phase = new double[0];
        } else if (step <= 1) {
            this.abs = new double[spanLength];
            this.phase = new double[spanLength];
            IntStream.range(0, spanLength)
                    .forEach(i -> {
                        final Complex value = spectrum.getValue(i + sampleStart);
                        abs[i] = value.getAbs();
                        phase[i] = value.getPhase();
                    });
        } else {
            int resultLength = (int) Math.ceil((double) spanLength / step);
            this.abs = new double[resultLength];
            this.phase = new double[resultLength];
            IntStream.range(0, resultLength)
                    .forEach(i -> {
                        final Complex value = spectrum.getValue(
                                i * step + sampleStart
                        );
                        abs[i] = value.getAbs();
                        phase[i] = value.getPhase();
                    });
        }
        this.startFrequency = startFreq;
        this.endFrequency = endFreq;
    }

    public double[] getAbs() {
        return abs;
    }

    public double[] getPhase() {
        return phase;
    }

    public double getStartFrequency() {
        return startFrequency;
    }

    public double getEndFrequency() {
        return endFrequency;
    }
}

