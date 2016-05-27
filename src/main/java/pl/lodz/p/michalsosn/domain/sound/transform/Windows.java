package pl.lodz.p.michalsosn.domain.sound.transform;

import pl.lodz.p.michalsosn.domain.sound.filter.BufferFilter;
import pl.lodz.p.michalsosn.domain.sound.filter.Filter;
import pl.lodz.p.michalsosn.domain.sound.signal.BufferSignal;
import pl.lodz.p.michalsosn.domain.sound.signal.LazySignal;
import pl.lodz.p.michalsosn.domain.sound.signal.Signal;
import pl.lodz.p.michalsosn.domain.sound.sound.BufferSound;
import pl.lodz.p.michalsosn.domain.sound.sound.LazySound;
import pl.lodz.p.michalsosn.domain.sound.sound.Sound;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.IntToDoubleFunction;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.function.UnaryOperator.*;

/**
 * @author Michał Sośnicki
 */
public final class Windows {

    public enum WindowType {
        RECTANGULAR(identity(), identity(), identity()),
        HANN(hannSound(), hannSignal(), hannFilter()),
        HAMMING(hammingSound(), hammingSignal(), hammingFilter());

        private final UnaryOperator<Sound> soundFunction;
        private final UnaryOperator<Signal> signalFunction;
        private final UnaryOperator<Filter> filterFunction;

        WindowType(
                UnaryOperator<Sound> soundFunction, UnaryOperator<Signal> signalFunction,
                UnaryOperator<Filter> filterFunction
        ) {
            this.soundFunction = soundFunction;
            this.signalFunction = signalFunction;
            this.filterFunction = filterFunction;
        }

        public UnaryOperator<Sound> getSoundFunction() {
            return soundFunction;
        }

        public UnaryOperator<Signal> getSignalFunction() {
            return signalFunction;
        }

        public UnaryOperator<Filter> getFilterFunction() {
            return filterFunction;
        }
    }

    private Windows() {
    }

    public static Function<Sound, Stream<Sound>> sliding(int windowLength) {
        return sliding(windowLength, windowLength);
    }

    public static Function<Sound, Stream<Sound>> sliding(
            int windowLength, int hopSize
    ) {
        return sound -> {
            final int length = sound.getLength();
            final List<Sound> windowList = new ArrayList<>();

//            final int offStart = causal ? 0 : -((windowLength - 1) / 2);
            for (int off = 0; off < length; off += hopSize) {
                final int cutLength = Math.min(windowLength, length - off);
                if (cutLength < windowLength - hopSize && off > 0) {
                    break;
                }
                final int cOff = off;
//                final IntUnaryOperator windowValues = off < 0
//                        ? p -> sound.getValue(MathUtils.mod(p + cOff, length))
//                        : p -> sound.getValue(p + cOff);
                LazySound windowSound = new LazySound(
                        p -> sound.getValue(p + cOff), cutLength, sound.getSamplingTime()
                );
                windowList.add(windowSound);
            }

            return windowList.stream();
        };
    }

    public static Function<Signal, Stream<Signal>> slidingSignal(
            int windowLength, int hopSize
    ) {
        return signal -> {
            final int length = signal.getLength();
            final List<Signal> windowList = new ArrayList<>();

            for (int off = 0; off < length; off += hopSize) {
                final int cutLength = Math.min(windowLength, length - off);
                if (cutLength < windowLength - hopSize && off > 0) {
                    break;
                }
                final int cOff = off;
                Signal windowSignal = new LazySignal(
                    p -> signal.getValue(p + cOff), cutLength, signal.getSamplingTime()
                );
                windowList.add(windowSignal);
            }

            return windowList.stream();
        };
    }

    public static Function<Stream<Signal>, Signal> overlapAdd(int hopSize) {
        return windows -> {
            final List<Signal> list = windows.collect(Collectors.toList());
            if (list.isEmpty()) {
                throw new IllegalArgumentException("no windows");
            }
            final int listSize = list.size();
            final int resultLength = (listSize - 1) * hopSize
                    + list.get(listSize - 1).getLength();

            double[] values = new double[resultLength];

            for (int i = 0; i < listSize; ++i) {
                final int offset = i * hopSize;
                final Signal window = list.get(i);
                final int windowLength = Math.min(
                        window.getLength(), resultLength - offset
                );
                for (int j = 0; j < windowLength; ++j) {
                    values[offset + j] += window.getValue(j);
                }
            }

            return new BufferSignal(values, list.get(0).getSamplingTime());
        };
    }

    public static Function<Signal, Signal> overlapAdd(
            int windowLength, int hopSize, UnaryOperator<Signal> mapper
    ) {
        return signal -> overlapAdd(hopSize).apply(
                slidingSignal(windowLength, hopSize)
                        .apply(signal)
                        .map(mapper)
        );
    }

    public static UnaryOperator<Sound> hannSound() {
        return sound -> applyToSound(hann(sound.getLength()), sound);
    }

    public static UnaryOperator<Signal> hannSignal() {
        return signal -> applyToSignal(hann(signal.getLength()), signal);
    }

    public static UnaryOperator<Filter> hannFilter() {
        return filter -> applyToFilter(hann(filter.getLength()), filter);
    }

    private static IntToDoubleFunction hann(int length) {
        if (length == 1) {
            return i -> 1.0;
        }
        final double coefficient = 2 * Math.PI  / (length % 2 == 0 ? length : length - 1);
        return i -> 0.5 * (1 - Math.cos(i * coefficient));
    }

    public static UnaryOperator<Sound> hammingSound() {
        return sound -> applyToSound(hamming(sound.getLength()), sound);
    }

    public static UnaryOperator<Signal> hammingSignal() {
        return signal -> applyToSignal(hamming(signal.getLength()), signal);
    }

    public static UnaryOperator<Filter> hammingFilter() {
        return filter -> applyToFilter(hamming(filter.getLength()), filter);
    }

    private static IntToDoubleFunction hamming(int length) {
        if (length == 1) {
            return i -> 1.0;
        }
        if (length % 2 == 0) {
            final double coefficient = 2 * Math.PI / length;
            return i -> 0.53836 - 0.46164 * Math.cos(i * coefficient);
        }
        final double coefficient = 2 * Math.PI / (length - 1);
        return i -> {
            final double value = 0.53836 - 0.46164 * Math.cos(i * coefficient);
            return i == 0 || i == length - 1 ? value / 2 : value;
        };
    }

    private static Sound applyToSound(IntToDoubleFunction multiplicand, Sound sound) {
        final int length = sound.getLength();
        int[] values = sound.values().toArray();

        for (int i = 0; i < length; i++) {
            values[i] *= multiplicand.applyAsDouble(i);
        }

        return new BufferSound(values, sound.getSamplingTime());
    }

    private static Signal applyToSignal(IntToDoubleFunction multiplicand, Signal signal) {
        final int length = signal.getLength();
        double[] values = signal.values().toArray();

        for (int i = 0; i < length; i++) {
            values[i] *= multiplicand.applyAsDouble(i);
        }

        return new BufferSignal(values, signal.getSamplingTime());
    }

    private static Filter applyToFilter(IntToDoubleFunction multiplicand, Filter filter) {
        final int length = filter.getLength();
        double[] values = filter.values().toArray();

        for (int i = 0; i < length; i++) {
            values[i] *= multiplicand.applyAsDouble(i);
        }

        return new BufferFilter(
                values, filter.getNegativeLength(), filter.getSamplingTime()
        );
    }

}
