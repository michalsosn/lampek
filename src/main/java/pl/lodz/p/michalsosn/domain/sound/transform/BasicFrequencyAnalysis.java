package pl.lodz.p.michalsosn.domain.sound.transform;

import pl.lodz.p.michalsosn.domain.complex.Complex;
import pl.lodz.p.michalsosn.domain.sound.TimeRange;
import pl.lodz.p.michalsosn.domain.sound.signal.LazySignal;
import pl.lodz.p.michalsosn.domain.sound.signal.Signal;
import pl.lodz.p.michalsosn.domain.sound.sound.LazySound;
import pl.lodz.p.michalsosn.domain.sound.sound.Sound;
import pl.lodz.p.michalsosn.domain.sound.spectrum.BufferSpectrum1d;
import pl.lodz.p.michalsosn.domain.sound.spectrum.Spectrum1d;

import java.util.ArrayList;
import java.util.List;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.function.Function;
import java.util.function.ToDoubleFunction;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

/**
 * @author Michał Sośnicki
 */
public final class BasicFrequencyAnalysis {

    private BasicFrequencyAnalysis() {
    }

    public static Function<Sound, Stream<Sound>> windowed(int windowLength) {
        return sound -> {
            final int length = sound.getLength();
            final List<Sound> windowList = new ArrayList<>();

            for (int off = 0; off < length; off += windowLength) {
                final int cutLength = Math.min(windowLength, length - off);
                final int cOff = off;
                LazySound windowSound = new LazySound(
                        p -> sound.getValue(p + cOff),
                        cutLength, sound.getSamplingTime()
                );
                windowList.add(windowSound);
            }

            return windowList.stream();
        };
    }

    public static UnaryOperator<Sound> approximateSine(
            ToDoubleFunction<Sound> frequencySearch
    ) {
        return sound -> {
            int amplitude = sound.values().map(Math::abs).max().getAsInt();
            double frequency = frequencySearch.applyAsDouble(sound);
            return Generators.sine(
                    amplitude, frequency, sound.getLength(), sound.getSamplingTime()
            );
        };
    }

    public static Function<Sound, Spectrum1d> cepstrum() {
        return sound -> {
            final int length = sound.getLength();
            Spectrum1d transform = DitFastFourierTransform.transform(sound);

//            final int halfLength = transform.getLength() / 2;
            Complex[] halfAbs = transform.values()//.limit(halfLength)
                    .map(value -> Complex.ofRe(Math.log(pow2(value.getAbs() / length))))
                    .toArray(Complex[]::new);
            BufferSpectrum1d halfAbsSpectrum
                    = new BufferSpectrum1d(halfAbs, sound.getSamplingTime());

            return DitFastFourierTransform.transform(halfAbsSpectrum);
        };
    }

    private static double pow2(double x) {
        return x * x;
    }

    public static Function<Sound, OptionalDouble> findByCepstrum() {
        return sound -> {
            if (sound.getLength() == 0) {
                return OptionalDouble.empty();
            }
            Spectrum1d cepstrum = cepstrum().apply(sound);

            Signal signal = new LazySignal(i -> (long) cepstrum.getValue(i).getAbs(),
                                           cepstrum.getLength(), cepstrum.getBasicTime());
            final OptionalInt optionalI = searchMaximum(signal, 0.2);
            return sampleToBasicFrequency(optionalI, sound.getSamplingTime());

//            final int halfLength2 = cepstrum.getLength() / 2;
//
//            double max = cepstrum.getValue(1).getAbs();
//            int maxI = 1;
//            for (int i = 2; i < halfLength2; ++i) {
//                final double value = cepstrum.getValue(i).getAbs();
//                if (value > max) {
//                    max = value;
//                    maxI = i;
//                }
//            }
//
//            return OptionalDouble.of(
//                    2 * sound.getSamplingTime().getFrequency() / maxI
//            );
        };
    }

    public static Function<Sound, OptionalDouble> findByAutocorrelation(
            double threshold
    ) {
        return sound -> {
            final Signal correlation = Correlations.autocorrelateCyclic().apply(sound);
            if (correlation.getLength() == 0) {
                return OptionalDouble.empty();
            }

            final OptionalInt optionalI = searchMaximum(correlation, threshold);
            return sampleToBasicFrequency(optionalI, sound.getSamplingTime());
        };
    }

    private static OptionalDouble sampleToBasicFrequency(OptionalInt optionalI,
                                                         TimeRange timeRange) {
        if (optionalI.isPresent()) {
            return OptionalDouble.of(timeRange.getFrequency() / optionalI.getAsInt());
        } else {
            return OptionalDouble.empty();
        }
    }

    private static OptionalInt searchMaximum(Signal signal, double threshold) {
        final int length = signal.getLength();

        final long max0 = signal.getValue(0);
        long min1 = max0;
        int i;
        for (i = 1; i < length; ++i) {
            final long value = signal.getValue(i);
            if (value < min1) {
                min1 = value;
            } else if (value - min1 > threshold * (max0 - min1)) {
                break;
            }
        }
        if (i == length) {
            return OptionalInt.empty();
        }

        long max2 = signal.getValue(i);
        int max2i = i;
        for (i = i + 1; i < length; ++i) {
            final long value = signal.getValue(i);
            if (value > max2) {
                max2 = value;
                max2i = i;
            } else if (max2 - value > threshold * (max2 - min1)) {
                break;
            }
        }

        return OptionalInt.of(max2i);
    }

}
