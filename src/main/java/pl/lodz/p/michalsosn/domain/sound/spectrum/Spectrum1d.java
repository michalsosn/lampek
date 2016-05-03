package pl.lodz.p.michalsosn.domain.sound.spectrum;

import pl.lodz.p.michalsosn.domain.Lift;
import pl.lodz.p.michalsosn.domain.complex.Complex;
import pl.lodz.p.michalsosn.domain.sound.Size1d;
import pl.lodz.p.michalsosn.domain.sound.TimeRange;

import java.util.function.UnaryOperator;
import java.util.stream.Stream;

/**
 * @author Michał Sośnicki
 */
public interface Spectrum1d extends Size1d, Lift<UnaryOperator<Complex>, Spectrum1d> {

    Complex getValue(int sample);

    Stream<Complex> values();

    TimeRange getBasicTime();

}
