package pl.lodz.p.michalsosn.domain.image.spectrum;

import pl.lodz.p.michalsosn.domain.Lift;
import pl.lodz.p.michalsosn.domain.complex.Complex;
import pl.lodz.p.michalsosn.domain.image.Size2d;

import java.util.function.UnaryOperator;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * @author Michał Sośnicki
 */
public interface Spectrum2d extends Size2d, Lift<UnaryOperator<Complex>, Spectrum2d> {

    Complex getValue(int y, int x);

    default Stream<Complex> values() {
        return IntStream.range(0, getHeight()).boxed().flatMap(y ->
                IntStream.range(0, getWidth()).mapToObj(x -> getValue(y, x))
        );
    }

    Complex[][] copyValues();

    void copyTo(int y, Complex[] buffer, int dest);

}
