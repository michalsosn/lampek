package pl.lodz.p.michalsosn.domain.image.spectrum;

/**
 * @author Michał Sośnicki
 */
public final class PolarComplex implements Complex {

    public static final PolarComplex ZERO = new PolarComplex(0.0, 0.0);

    private final double abs;
    private final double phase;

    private PolarComplex(double abs, double phase) {
        this.abs = abs;
        this.phase = phase;
    }

    public static PolarComplex of(double abs, double phase) {
        return new PolarComplex(abs, phase);
    }

    public static PolarComplex ofAbs(double abs) {
        return new PolarComplex(abs, 0.0);
    }

    public static PolarComplex ofReIm(double re, double im) {
        return new PolarComplex(Math.hypot(re, im), Math.atan2(im, re));
    }

    @Override
    public double getRe() {
        return abs * Math.cos(phase);
    }

    @Override
    public double getIm() {
        return abs * Math.sin(phase);
    }

    @Override
    public double getAbs() {
        return abs;
    }

    @Override
    public double getPhase() {
        return phase;
    }

    @Override
    public PolarComplex add(Complex augend) {
        double resultRe = abs * Math.cos(phase)
                + augend.getAbs() * Math.cos(augend.getPhase());
        double resultIm = abs * Math.sin(phase)
                + augend.getAbs() * Math.sin(augend.getPhase());
        return PolarComplex.ofReIm(resultRe, resultIm);
    }

    @Override
    public PolarComplex subtract(Complex subtrahend) {
        double resultRe = abs * Math.cos(phase)
                - subtrahend.getAbs() * Math.cos(subtrahend.getPhase());
        double resultIm = abs * Math.sin(phase)
                - subtrahend.getAbs() * Math.sin(subtrahend.getPhase());
        return PolarComplex.ofReIm(resultRe, resultIm);
    }

    @Override
    public PolarComplex multiply(Complex multiplicand) {
        return new PolarComplex(abs * multiplicand.getAbs(),
                                phase + multiplicand.getPhase());
    }

    @Override
    public PolarComplex divide(Complex divisor) {
        return new PolarComplex(abs / divisor.getAbs(),
                                phase - divisor.getPhase());
    }

    @Override
    public PolarComplex conjugate() {
        return new PolarComplex(abs, -phase);
    }

    @Override
    public Complex scale(double scale) {
        return new PolarComplex(abs * scale, phase);
    }

    @Override
    public String toString() {
        if (phase == 0) {
            return abs + "";
        }
        return abs + "e^i(" + phase + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final PolarComplex complex = (PolarComplex) o;

        return complex.abs == abs && complex.phase == phase;

    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        temp = Double.doubleToLongBits(abs);
        result = (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(phase);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

}
