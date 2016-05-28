package pl.lodz.p.michalsosn.domain.sound.transform;

import pl.lodz.p.michalsosn.domain.sound.Size1d;
import pl.lodz.p.michalsosn.domain.sound.TimeRange;
import pl.lodz.p.michalsosn.domain.sound.filter.BufferFilter;
import pl.lodz.p.michalsosn.domain.sound.filter.Filter;
import pl.lodz.p.michalsosn.domain.sound.filter.LazyFilter;
import pl.lodz.p.michalsosn.domain.sound.signal.BufferSignal;
import pl.lodz.p.michalsosn.domain.sound.signal.LazySignal;
import pl.lodz.p.michalsosn.domain.sound.signal.Signal;
import pl.lodz.p.michalsosn.domain.sound.sound.BufferSound;
import pl.lodz.p.michalsosn.domain.sound.sound.LazySound;
import pl.lodz.p.michalsosn.domain.sound.sound.Sound;
import pl.lodz.p.michalsosn.domain.util.IntBiFunction;
import pl.lodz.p.michalsosn.domain.util.MathUtils;

import java.util.function.IntFunction;
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
                        p -> p < length ? sound.getValue(p) : 0,
                        newLength, samplingTime
                );
            }
        };
    }

    public static UnaryOperator<Signal> zeroPadSignal(int newLength) {
        return signal -> {
            final int length = signal.getLength();
            final TimeRange samplingTime = signal.getSamplingTime();
            if (newLength <= length) {
                return new LazySignal(signal::getValue, newLength, samplingTime);
            } else {
                return new LazySignal(
                        p -> p < length ? signal.getValue(p) : 0,
                        newLength, samplingTime
                );
            }
        };
    }

    public static UnaryOperator<Filter> zeroPadFilter(int newLength) {
        return signal -> {
            final int length = signal.getLength();
            final int negativeLength = signal.getNegativeLength();
            final int positiveLength = signal.getPositiveLength();
            final TimeRange samplingTime = signal.getSamplingTime();
            if (newLength <= length) {
                return new LazyFilter(signal::getValue, newLength,
                                      negativeLength, samplingTime);
            } else {
                return new LazyFilter(
                        p -> p < positiveLength ? signal.getValue(p) : 0,
                        newLength, negativeLength, samplingTime
                );
            }
        };
    }

    public static <T extends Size1d> UnaryOperator<T> zeroPadToPowerOfTwo(
            IntFunction<UnaryOperator<T>> zeroPad
    ) {
        return sized -> {
            int length = sized.getLength();
            if (MathUtils.isPowerOfTwo(length)) {
                return sized;
            } else {
                int newLength = 2 << MathUtils.log2(length);
                return zeroPad.apply(newLength).apply(sized);
            }
        };
    }

    public static UnaryOperator<Sound> shortenSound(int skip, int take) {
        return sound -> new BufferSound(
                sound.values().skip(skip).limit(take).toArray(),
                sound.getSamplingTime()
        );
    }

    public static UnaryOperator<Signal> shortenSignal(int skip, int take) {
        return signal -> new BufferSignal(
                signal.values().skip(skip).limit(take).toArray(),
                signal.getSamplingTime()
        );
    }

    public static UnaryOperator<Filter> shortenFilter(int skip, int take) {
        return filter -> new BufferFilter(
                filter.values().skip(skip).limit(take).toArray(),
                filter.getNegativeLength(), filter.getSamplingTime()
        );
    }

    public static <T extends Size1d> UnaryOperator<T> shortenToPowerOfTwo(
            IntBiFunction<UnaryOperator<T>> shorten
    ) {
        return sized -> {
            int length = sized.getLength();
            if (MathUtils.isPowerOfTwo(length)) {
                return sized;
            } else {
                int newLength = 1 << MathUtils.log2(length);
                return shorten.apply(0, newLength).apply(sized);
            }
        };
    }

    public static UnaryOperator<Signal> delaySignal(int delay) {
        return signal -> {
            if (delay == 0) {
                return signal;
            } else {
                return new LazySignal(
                        p -> p < delay ? 0 : signal.getValue(p - delay),
                        signal.getLength() + delay, signal.getSamplingTime()
                );
            }
        };
    }

    public static UnaryOperator<Signal> rotateLeftSignal(int change) {
        return signal -> {
            if (change == 0) {
                return signal;
            } else {
                final int length = signal.getLength();
                final int breakpoint = length - change;
                return new LazySignal(
                        p -> p < breakpoint ? signal.getValue(p + change)
                                            : signal.getValue(p - breakpoint),
                        length, signal.getSamplingTime()
                );
            }
        };
    }

}
