package pl.lodz.p.michalsosn.domain.sound.transform;

import pl.lodz.p.michalsosn.domain.complex.Complex;
import pl.lodz.p.michalsosn.domain.sound.TimeRange;
import pl.lodz.p.michalsosn.domain.sound.signal.BufferSignal;
import pl.lodz.p.michalsosn.domain.sound.signal.Signal;
import pl.lodz.p.michalsosn.domain.sound.sound.BufferSound;
import pl.lodz.p.michalsosn.domain.sound.sound.LazySound;
import pl.lodz.p.michalsosn.domain.sound.sound.Sound;
import pl.lodz.p.michalsosn.domain.sound.spectrum.BufferSpectrum1d;
import pl.lodz.p.michalsosn.domain.sound.spectrum.Spectrum1d;
import pl.lodz.p.michalsosn.domain.util.MathUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.OptionalInt;
import java.util.function.Function;
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

    public static List<Note> joinNotes(Iterable<Note> notes) {
        List<Note> result = new ArrayList<>();
        Note last = null;
        for (Note note : notes) {
            if (last == null) {
                last = note;
            } else if (last.close(note)) {
                last = last.join(note);
            } else {
                result.add(last);
                last = note;
            }
        }
        if (last != null) {
            result.add(last);
        }
        return result;
    }

    public static Sound approximateSine(List<Note> notes) {
        int[] values = notes.stream()
                .map(Note::toSine)
                .flatMapToInt(Sound::values)
                .toArray();
        return new BufferSound(values, notes.get(0).getSamplingTime());
    }

    public static Function<Sound, Signal> cepstrum() {
        return sound -> {
            final int length = sound.getLength();
            Spectrum1d transform = DitFastFourierTransform.transform(sound);

            double[] powerValues = transform.values()
                    .mapToDouble(Complex::getAbsSquare)
                    .map(Math::log)
//                    .map(Math::log1p)
                    .toArray();
            powerValues[0] = 0;

            final double smallCutoff = 0.9;
            double largest = Arrays.stream(powerValues).max().orElse(0);
            for (int i = 0; i < length; i++) {
                if (powerValues[i] < smallCutoff * largest) {
                    powerValues[i] = 0;
                }
            }

//            final double localCutoff = 0.8;
//            double last = powerValues[0];
//            for (int i = 1; i < length - 1; ++i) {
//                final double current = powerValues[i];
//                if (current < localCutoff * last
//                 || current < localCutoff * powerValues[i + 1]) {
//                    powerValues[i] = 0;
//                }
//                last = current;
//            }

            Complex[] powerComplexes = Arrays.stream(powerValues)
                    .mapToObj(Complex::ofRe)
                    .toArray(Complex[]::new);

            Spectrum1d powerSpectrum
                    = new BufferSpectrum1d(powerComplexes, sound.getSamplingTime());

            Spectrum1d cepstrum = DitFastFourierTransform.transform(powerSpectrum);

            double[] values = cepstrum.values()
                    .limit(cepstrum.getLength() / 2)
                    .mapToDouble(Complex::getRe)
                    .toArray();
            return new BufferSignal(values, cepstrum.getBasicTime());
        };
    }

    public static Function<Sound, Note> findByCepstrum(double threshold) {
        return sound -> {
            final int length = sound.getLength();
            if (length == 0 || !MathUtils.isPowerOfTwo(length)) {
                return Note.unknown(sound.getLength(), sound.getSamplingTime());
            }

            Signal cepstrum = cepstrum().apply(sound);

            final OptionalInt optionalI = searchMaximum(cepstrum, threshold);
            return sampleToNote(optionalI, sound);
        };
    }

    public static Function<Sound, Note> findByAutocorrelation(double threshold) {
        return sound -> {
            Signal correlation = Correlations.autocorrelateLinear(true).apply(sound);
            if (correlation.getLength() == 0) {
                return Note.unknown(sound.getLength(), sound.getSamplingTime());
            }

            OptionalInt optionalI = searchMaximum(correlation, threshold);
            return sampleToNote(optionalI, sound);
        };
    }

    private static Note sampleToNote(OptionalInt optionalI, Sound sound) {
        final int length = sound.getLength();
        final TimeRange time = sound.getSamplingTime();
        if (optionalI.isPresent()) {
            final double frequency = sound.getSamplingTime().getFrequency();
            double pitch = frequency / optionalI.getAsInt();
            return Note.of(pitch, maxAmplitude(sound), length, time);
        } else {
            return Note.unknown(length, time);
        }
    }

    private static int maxAmplitude(Sound sound) {
        return sound.values().map(Math::abs).max().orElse(0);
    }

    private static OptionalInt searchMaximum(Signal signal, double threshold) {
        final int length = signal.getLength();

        // find first and second zero crossing
        int zero = 0;
        while (zero < length) {
            if (signal.getValue(zero++) < 0) {
                break;
            }
        }
        while (zero < length) {
            if (signal.getValue(zero++) > 0) {
                break;
            }
        }
        if (zero == length) {
            return OptionalInt.empty();
        }

        // find maximum after the crossings
        double max = signal.getValue(zero);
        int maxI = zero;
        for (int i = zero + 1; i < length; ++i) {
            final double value = signal.getValue(i);
            if (value > max) {
                max = value;
                maxI = i;
            }
        }

        // find first peak larger than threshold * maximum
        double thresholdValue = threshold * max;
        for (int i = zero; i <= maxI; ++i) {
            final double value1 = signal.getValue(i);
            if (value1 > thresholdValue) {
                double peak = value1;
                int peakJ = i;
                for (int j = i; j <= maxI; ++j) {
                    final double value2 = signal.getValue(j);
                    if (value2 < thresholdValue) {
                        return OptionalInt.of(peakJ);
                    }
                    if (value2 > peak) {
                        peak = value2;
                        peakJ = j;
                    }
                }
            }
        }

        return OptionalInt.of(maxI);
    }

}
