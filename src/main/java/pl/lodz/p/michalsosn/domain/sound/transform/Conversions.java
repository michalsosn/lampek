package pl.lodz.p.michalsosn.domain.sound.transform;

import pl.lodz.p.michalsosn.domain.complex.Complex;
import pl.lodz.p.michalsosn.domain.sound.filter.Filter;
import pl.lodz.p.michalsosn.domain.sound.filter.LazyFilter;
import pl.lodz.p.michalsosn.domain.sound.signal.LazySignal;
import pl.lodz.p.michalsosn.domain.sound.signal.Signal;
import pl.lodz.p.michalsosn.domain.sound.sound.BufferSound;
import pl.lodz.p.michalsosn.domain.sound.sound.Sound;
import pl.lodz.p.michalsosn.domain.sound.spectrum.BufferSpectrum1d;
import pl.lodz.p.michalsosn.domain.sound.spectrum.Spectrum1d;

import java.util.function.IntToDoubleFunction;

/**
 * @author Michał Sośnicki
 */
public final class Conversions {

    private Conversions() {
    }

    public static Sound toSound(Signal signal) {
        final int[] values = signal.values().mapToInt(value -> (int)
                Math.max(Sound.MIN_VALUE, Math.min(Sound.MAX_VALUE, Math.round(value)))
        ).toArray();
        return new BufferSound(values, signal.getSamplingTime());
    }

    public static Sound toSound(Spectrum1d spectrum1d) {
        return toSound(toSignal(spectrum1d));
    }

    public static Sound toSound(Filter filter) {
        return toSound(toSignal(filter));
    }

    public static Signal toSignal(Sound sound) {
        return new LazySignal(
                sound::getValue, sound.getLength(), sound.getSamplingTime()
        );
    }

    public static Signal toSignal(Spectrum1d spectrum1d) {
        final IntToDoubleFunction valueFunction = p -> spectrum1d.getValue(p).getRe();
        return new LazySignal(valueFunction, spectrum1d.getLength(),
                              spectrum1d.getBasicTime());
    }

    public static Signal toSignal(Filter filter) {
        final int length = filter.getLength();
        final int positiveLength = filter.getPositiveLength();
        final IntToDoubleFunction valueFunction = p -> p < positiveLength
                ? filter.getValue(p) : filter.getValue(p - length);
        return new LazySignal(valueFunction, filter.getLength(),
                              filter.getSamplingTime());
    }

    public static Filter toFilter(Sound sound) {
        return toFilter(toSignal(sound));
    }

    public static Filter toFilter(Signal signal) {
        return new LazyFilter(
            signal::getValue, signal.getLength(), 0, signal.getSamplingTime()
        );
    }

    public static Filter toFilter(Spectrum1d spectrum1d) {
        return toFilter(toSignal(spectrum1d));
    }

    public static Spectrum1d toSpectrum1d(Sound sound) {
        return toSpectrum1d(toSignal(sound));
    }

    public static Spectrum1d toSpectrum1d(Signal signal) {
        final Complex[] values = signal.values()
                .mapToObj(Complex::ofRe).toArray(Complex[]::new);
        return new BufferSpectrum1d(values, signal.getSamplingTime());
    }

    public static Spectrum1d toSpectrum1d(Filter filter) {
        return toSpectrum1d(toSignal(filter));
    }

}
