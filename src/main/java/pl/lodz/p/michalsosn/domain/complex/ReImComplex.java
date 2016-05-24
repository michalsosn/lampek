package pl.lodz.p.michalsosn.domain.complex;

/**
 * @author Michał Sośnicki
 */
public final class ReImComplex implements Complex {

    public static final ReImComplex ZERO = new ReImComplex(0.0, 0.0);

    private final double re;
    private final double im;

    private ReImComplex(double re, double im) {
        this.re = re;
        this.im = im;
    }

    public static ReImComplex of(double re, double im) {
        return new ReImComplex(re, im);
    }

    public static ReImComplex ofRe(double re) {
        return new ReImComplex(re, 0.0);
    }

    public static ReImComplex ofPolar(double abs, double phase) {
        return new ReImComplex(abs * Math.cos(phase), abs * Math.sin(phase));
    }

    @Override
    public double getRe() {
        return re;
    }

    @Override
    public double getIm() {
        return im;
    }

    @Override
    public double getAbs() {
        return Math.hypot(re, im);
    }

    @Override
    public double getPhase() {
        return Math.atan2(im, re);
    }

    @Override
    public double getAbsSquare() {
        return re * re + im * im;
    }

    @Override
    public ReImComplex add(Complex augend) {
        return new ReImComplex(re + augend.getRe(), im + augend.getIm());
    }

    @Override
    public ReImComplex subtract(Complex subtrahend) {
        return new ReImComplex(re - subtrahend.getRe(),
                               im - subtrahend.getIm());
    }

    @Override
    public ReImComplex multiply(Complex multiplicand) {
        double resultRe = re * multiplicand.getRe() - im * multiplicand.getIm();
        double resultIm = re * multiplicand.getIm() + im * multiplicand.getRe();
        return new ReImComplex(resultRe, resultIm);
    }

    @Override
    public ReImComplex divide(Complex divisor) {
        double scale = Math.hypot(divisor.getRe(), divisor.getIm());
        double resultRe = re * divisor.getRe() + im * divisor.getIm();
        double resultIm = re * -divisor.getIm() + im * divisor.getRe();
        return new ReImComplex(resultRe / scale, resultIm / scale);
    }

    @Override
    public ReImComplex conjugate() {
        return new ReImComplex(re, -im);
    }

    @Override
    public Complex scale(double scale) {
        return new ReImComplex(re * scale, im * scale);
    }

    @Override
    public String toString() {
        if (im == 0) {
            return re + "";
        }
        if (re == 0) {
            return im + "i";
        }
        return re + " + " + im + "i";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final ReImComplex complex = (ReImComplex) o;

        return complex.re == re && complex.im == im;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        temp = Double.doubleToLongBits(re);
        result = (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(im);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

}
