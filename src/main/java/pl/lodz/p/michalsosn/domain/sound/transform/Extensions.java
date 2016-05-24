package pl.lodz.p.michalsosn.domain.sound.transform;

import pl.lodz.p.michalsosn.domain.sound.TimeRange;
import pl.lodz.p.michalsosn.domain.sound.signal.BufferSignal;
import pl.lodz.p.michalsosn.domain.sound.signal.LazySignal;
import pl.lodz.p.michalsosn.domain.sound.signal.Signal;
import pl.lodz.p.michalsosn.domain.sound.sound.BufferSound;
import pl.lodz.p.michalsosn.domain.sound.sound.LazySound;
import pl.lodz.p.michalsosn.domain.sound.sound.Sound;
import pl.lodz.p.michalsosn.domain.util.MathUtils;

import java.util.function.UnaryOperator;

/**
 * @author Michał Sośnicki
 */
public final class Extensions {

    private Extensions() {
    }

    public static UnaryOperator<Sound> zeroPadSound(int newLength) {
        return sound -> {
            final int length = sound.getLength();
            final TimeRange samplingTime = sound.getSamplingTime();
            if (newLength <= length) {
                return new LazySound(sound::getValue, newLength, samplingTime);
            } else {
                return new LazySound(
                        p -> p < length ? sound.getValue(p) : 0, newLength, samplingTime
                );
            }
        };
    }

    public static UnaryOperator<Signal> zeroPadSignal(int newLength) {
        return zeroPadSignal(newLength, 0);
    }

    public static UnaryOperator<Signal> zeroPadSignal(int newLength, int delay) {
        return signal -> {
            final int length = signal.getLength();
            final TimeRange samplingTime = signal.getSamplingTime();
            if (delay == 0 && newLength <= length) {
                return new LazySignal(signal::getValue, newLength, samplingTime);
            } else {
                return new LazySignal(
                        p -> delay <= p && p < delay + length
                                ? signal.getValue(p - delay) : 0,
                        newLength, samplingTime
                );
            }
        };
    }

    public static UnaryOperator<Sound> shorten(int skip, int take) {
        return sound -> new BufferSound(
                sound.values().skip(skip).limit(take).toArray(),
                sound.getSamplingTime()
        );
    }

    public static UnaryOperator<Sound> shortenToPowerOfTwo() {
        return sound -> {
            int length = sound.getLength();
            if (MathUtils.isPowerOfTwo(length)) {
                return sound;
            } else {
                int newLength = 1 << MathUtils.log2(length);
                return shorten(0, newLength).apply(sound);
            }
        };
    }

    public static UnaryOperator<Signal> shortenSignal(int skip, int take) {
        return signal -> new BufferSignal(
                signal.values().skip(skip).limit(take).toArray(),
                signal.getSamplingTime()
        );
    }

}
