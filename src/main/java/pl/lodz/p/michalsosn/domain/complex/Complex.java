package pl.lodz.p.michalsosn.domain.complex;

/**
 * @author Michał Sośnicki
 */
public interface Complex {

    Complex ZERO = ReImComplex.ZERO;

    static Complex ofReIm(double re, double im) {
        return ReImComplex.of(re, im);
    }

    static Complex ofRe(double re) {
        return ReImComplex.ofRe(re);
    }

    static Complex ofPolar(double abs, double phase) {
        return PolarComplex.of(abs, phase);
    }

    static Complex ofAbs(double abs) {
        return PolarComplex.ofAbs(abs);
    }

    double getRe();
    double getIm();
    double getAbs();
    double getPhase();

    Complex add(Complex augend);
    Complex subtract(Complex subtrahend);
    Complex multiply(Complex multiplicand);
    Complex divide(Complex divisor);
    Complex conjugate();
    Complex scale(double scale);

}
