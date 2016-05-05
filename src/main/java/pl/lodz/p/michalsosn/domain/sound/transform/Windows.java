package pl.lodz.p.michalsosn.domain.sound.transform;

import pl.lodz.p.michalsosn.domain.sound.sound.BufferSound;
import pl.lodz.p.michalsosn.domain.sound.sound.Sound;

import java.util.function.IntToDoubleFunction;
import java.util.function.UnaryOperator;

/**
 * @author Michał Sośnicki
 */
public final class Windows {
    private Windows() {
    }

    public static UnaryOperator<Sound> hann() {
        return sound -> {
            final double coefficient = 2 * Math.PI  / (sound.getLength() - 1);
            return applyWindow(i -> 0.5 * (1 - Math.cos(i * coefficient)), sound);
        };
    }

    public static UnaryOperator<Sound> hamming() {
        return sound -> {
            final double coefficient = 2 * Math.PI / (sound.getLength() - 1);
            return applyWindow(i -> 0.53836 - 0.46164 * Math.cos(i * coefficient), sound);
        };
    }

    private static Sound applyWindow(IntToDoubleFunction multiplicand, Sound sound) {
        final int length = sound.getLength();
        int[] values = sound.values().toArray();

        for (int i = 0; i < length; i++) {
            values[i] *= multiplicand.applyAsDouble(i);
        }

        return new BufferSound(values, sound.getSamplingTime());
    }
}
