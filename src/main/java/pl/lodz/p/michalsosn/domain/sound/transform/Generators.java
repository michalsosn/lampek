package pl.lodz.p.michalsosn.domain.sound.transform;

import pl.lodz.p.michalsosn.domain.sound.TimeRange;
import pl.lodz.p.michalsosn.domain.sound.sound.LazySound;
import pl.lodz.p.michalsosn.domain.sound.sound.Sound;

import java.util.function.IntUnaryOperator;

import static pl.lodz.p.michalsosn.domain.sound.sound.Sound.*;

/**
 * @author Michał Sośnicki
 */
public final class Generators {
    private Generators() {
    }

    public static IntUnaryOperator sine(
            int amplitude, double basicFrequency, double startPhase,
            TimeRange samplingTime
    ) {
        double startPhaseRads = Math.toRadians(startPhase);
        double coefficient = 2 * Math.PI * basicFrequency / samplingTime.getFrequency();

        return i -> Math.max(MIN_VALUE, Math.min(MAX_VALUE, (int) Math.round(
                amplitude * Math.sin(i * coefficient + startPhaseRads) + MID_VALUE
        )));
    }

    public static IntUnaryOperator sine(
            int amplitude, double basicFrequency, TimeRange samplingTime
    ) {
        return sine(amplitude, basicFrequency, 0.0, samplingTime);
    }

    public static IntUnaryOperator cosine(
            int amplitude, double basicFrequency, double startPhase,
            TimeRange samplingTime
    ) {
        return sine(amplitude, basicFrequency, startPhase + 90, samplingTime);
    }

    public static IntUnaryOperator cosine(
            int amplitude, double basicFrequency, TimeRange samplingTime
    ) {
        return sine(amplitude, basicFrequency, 90.0, samplingTime);
    }

    public static Sound sine(
            int amplitude, double basicFrequency, double startPhase,
            int length, TimeRange samplingTime
    ) {
        return new LazySound(
                sine(amplitude, basicFrequency, startPhase, samplingTime),
                length, samplingTime
        );
    }

    public static Sound sine(
            int amplitude, double basicFrequency, int length, TimeRange samplingTime
    ) {
        return sine(amplitude, basicFrequency, 0, length, samplingTime);
    }

    public static Sound cosine(
            int amplitude, double basicFrequency, double startPhase,
            int length, TimeRange samplingTime
    ) {
        return sine(amplitude, basicFrequency, startPhase + 90, length, samplingTime);
    }

    public static Sound cosine(
            int amplitude, double basicFrequency, int length, TimeRange samplingTime
    ) {
        return sine(amplitude, basicFrequency, 90, length, samplingTime);
    }

    public static IntUnaryOperator constant(int amplitude) {
        final int realAmplitude = Math.max(MIN_VALUE, Math.min(MAX_VALUE, amplitude));
        return i -> realAmplitude;
    }

    public static Sound constant(
            int amplitude, int length, TimeRange samplingTime
    ) {
        return new LazySound(constant(amplitude), length, samplingTime);
    }

    public static IntUnaryOperator linear(int amplitudeStart, int amplitudeEnd,
                                          int length) {
        final int realAmplitudeStart
                = Math.max(MIN_VALUE, Math.min(MAX_VALUE, amplitudeStart));
        final int realAmplitudeEnd
                = Math.max(MIN_VALUE, Math.min(MAX_VALUE, amplitudeEnd));
        final double liquidLength = length;
        return i -> {
            final double progress = i / liquidLength;
            return (int) Math.round(
                    progress * realAmplitudeStart + (1 - progress) * realAmplitudeEnd
            );
        };
    }

    public static Sound linear(
            int amplitudeStart, int amplitudeEnd, int length, TimeRange samplingTime
    ) {
        return new LazySound(
                linear(amplitudeStart, amplitudeEnd, length), length, samplingTime
        );
    }

}
