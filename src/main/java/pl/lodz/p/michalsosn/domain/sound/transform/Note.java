package pl.lodz.p.michalsosn.domain.sound.transform;

import pl.lodz.p.michalsosn.domain.sound.TimeRange;
import pl.lodz.p.michalsosn.domain.sound.sound.LazySound;
import pl.lodz.p.michalsosn.domain.sound.sound.Sound;

import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.function.IntUnaryOperator;

import static pl.lodz.p.michalsosn.domain.sound.sound.Sound.MID_VALUE;

/**
 * @author Michał Sośnicki
 */
public final class Note {
    public static final double DEFAULT_CLOSENESS_THRESHOLD = 0.03;

    private final OptionalDouble pitch;
    private final int amplitudeStart;
    private final int amplitudeEnd;
    private final int length;
    private final TimeRange samplingTime;

    private Note(OptionalDouble pitch, int amplitudeStart, int amplitudeEnd,
                 int length, TimeRange samplingTime) {
        if (pitch == null || samplingTime == null) {
            throw new IllegalArgumentException("arguments can't be null");
        }
        this.pitch = pitch;
        this.amplitudeStart = amplitudeStart;
        this.amplitudeEnd = amplitudeEnd;
        this.length = length;
        this.samplingTime = samplingTime;
    }

    public static Note of(
            double pitch, int amplitudeStart, int amplitudeEnd,
            int length, TimeRange samplingTime
    ) {
        return new Note(OptionalDouble.of(pitch), amplitudeStart, amplitudeEnd,
                length, samplingTime);
    }

    public static Note of(
            double pitch, int amplitude, int length, TimeRange samplingTime
    ) {
        return Note.of(pitch, amplitude, amplitude, length, samplingTime);
    }

    public static Note unknown(int length, TimeRange samplingTime) {
        return new Note(OptionalDouble.empty(), 0, 0, length, samplingTime);
    }

    public OptionalDouble getPitch() {
        return pitch;
    }

    public OptionalInt getAmplitudeStart() {
        if (getPitch().isPresent()) {
            return OptionalInt.of(amplitudeStart);
        } else {
            return OptionalInt.empty();
        }
    }

    public OptionalInt getAmplitudeEnd() {
        if (getPitch().isPresent()) {
            return OptionalInt.of(amplitudeEnd);
        } else {
            return OptionalInt.empty();
        }
    }

    public int getLength() {
        return length;
    }

    public TimeRange getSamplingTime() {
        return samplingTime;
    }

    public double getDuration() {
        return length * samplingTime.getDuration();
    }

    public boolean close(Note next, double closeThreshold) {
        if (!samplingTime.equals(next.samplingTime)) {
            return false;
        }
        return !pitch.isPresent()
            || !next.pitch.isPresent()
            || Math.abs(pitch.getAsDouble() - next.pitch.getAsDouble())
            < closeThreshold * Math.min(pitch.getAsDouble(), next.pitch.getAsDouble());
    }

    public boolean close(Note next) {
        return close(next, DEFAULT_CLOSENESS_THRESHOLD);
    }

    public Note join(Note next) {
        final int joinLength = length + next.length;
        if (!pitch.isPresent()) {
            return new Note(next.pitch, next.amplitudeStart, next.amplitudeEnd,
                            joinLength, samplingTime);
        }
        if (!next.pitch.isPresent()) {
            return new Note(pitch, amplitudeStart,  amplitudeEnd,
                            joinLength, samplingTime);
        }
        return new Note(
                pitch, amplitudeStart, next.amplitudeEnd,
                joinLength, samplingTime
        );
    }

    public Sound toSine() {
        if (pitch.isPresent()) {
            final double liquidLength = length;
            final double coefficient = 2 * Math.PI * pitch.getAsDouble()
                                     / samplingTime.getFrequency();

            final IntUnaryOperator valueFunction = i -> {
                final double progress = i / liquidLength;
                final double amplitude = progress * amplitudeStart
                                       + (1 - progress) * amplitudeEnd;

                return (int) Math.round(
                        amplitude * Math.sin(i * coefficient) + MID_VALUE
                );
            };

            return new LazySound(valueFunction, length, samplingTime);
        } else {
            return Generators.constant(0, length, samplingTime);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Note note = (Note) o;

        return amplitudeStart == note.amplitudeStart
            && amplitudeEnd == note.amplitudeEnd
            && length == note.length
            && pitch.equals(note.pitch)
            && samplingTime.equals(note.samplingTime);
    }

    @Override
    public int hashCode() {
        int result = pitch.hashCode();
        result = 31 * result + amplitudeStart;
        result = 31 * result + amplitudeEnd;
        result = 31 * result + length;
        result = 31 * result + samplingTime.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "Note{"
             +  "pitch=" + pitch
             +  ", amplitudeStart=" + amplitudeStart
             +  ", amplitudeEnd=" + amplitudeEnd
             +  ", length=" + length
             +  ", samplingTime=" + samplingTime
             +  '}';
    }
}

