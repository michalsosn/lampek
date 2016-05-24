package pl.lodz.p.michalsosn.domain.sound.transform;

import pl.lodz.p.michalsosn.domain.sound.signal.BufferSignal;
import pl.lodz.p.michalsosn.domain.sound.signal.LazySignal;
import pl.lodz.p.michalsosn.domain.sound.signal.Signal;
import pl.lodz.p.michalsosn.domain.sound.sound.BufferSound;
import pl.lodz.p.michalsosn.domain.sound.sound.LazySound;
import pl.lodz.p.michalsosn.domain.sound.sound.Sound;
import pl.lodz.p.michalsosn.domain.util.MathUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.IntToDoubleFunction;
import java.util.function.IntUnaryOperator;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Michał Sośnicki
 */
public final class Windows {

    public enum WindowType {
        RECTANGLE(UnaryOperator.identity(), UnaryOperator.identity()),
        HANN(Windows.hannSound(), Windows.hannSignal()),
        HAMMING(Windows.hammingSound(), Windows.hammingSignal());

        private final UnaryOperator<Sound> soundFunction;
        private final UnaryOperator<Signal> signalFunction;

        WindowType(UnaryOperator<Sound> soundFunction,
                   UnaryOperator<Signal> signalFunction) {
            this.soundFunction = soundFunction;
            this.signalFunction = signalFunction;
        }

        public UnaryOperator<Sound> getSoundFunction() {
            return soundFunction;
        }

        public UnaryOperator<Signal> getSignalFunction() {
            return signalFunction;
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
                final int cOff = off;
                final IntUnaryOperator windowValues = off < 0
                        ? p -> sound.getValue(MathUtils.mod(p + cOff, length))
                        : p -> sound.getValue(p + cOff);
                LazySound windowSound = new LazySound(
                        windowValues, cutLength, sound.getSamplingTime()
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

//            final int offStart = causal ? 0 : -((windowLength - 1) / 2);
            for (int off = 0; off < length; off += hopSize) {
                final int cutLength = Math.min(windowLength, length - off);
                final int cOff = off;
                final IntToDoubleFunction windowValues = off < 0
                        ? p -> signal.getValue(MathUtils.mod(p + cOff, length))
                        : p -> signal.getValue(p + cOff);
                Signal windowSignal = new LazySignal(
                        windowValues, cutLength, signal.getSamplingTime()
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
                        .filter(window -> window.getLength() >= windowLength - hopSize)
                        .map(mapper)
        );
    }

    public static UnaryOperator<Sound> hannSound() {
        return sound -> applyToSound(hann(sound.getLength()), sound);
    }

    public static UnaryOperator<Signal> hannSignal() {
        return signal -> applyToSignal(hann(signal.getLength()), signal);
    }

    private static IntToDoubleFunction hann(int length) {
        final double coefficient = 2 * Math.PI  / (length % 2 == 0 ? length : length - 1);
        return i -> 0.5 * (1 - Math.cos(i * coefficient));
    }

    public static UnaryOperator<Sound> hammingSound() {
        return sound -> applyToSound(hamming(sound.getLength()), sound);
    }

    public static UnaryOperator<Signal> hammingSignal() {
        return signal -> applyToSignal(hamming(signal.getLength()), signal);
    }

    private static IntToDoubleFunction hamming(int length) {
        final double coefficient = 2 * Math.PI / (length - 1);
        return i -> 0.53836 - 0.46164 * Math.cos(i * coefficient);
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

}
