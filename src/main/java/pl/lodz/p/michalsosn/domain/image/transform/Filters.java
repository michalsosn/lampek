package pl.lodz.p.michalsosn.domain.image.transform;

import pl.lodz.p.michalsosn.domain.image.spectrum.BufferSpectrum2d;
import pl.lodz.p.michalsosn.domain.complex.Complex;
import pl.lodz.p.michalsosn.domain.image.spectrum.Spectrum2d;
import pl.lodz.p.michalsosn.domain.util.MathUtils;

import java.util.function.IntPredicate;
import java.util.function.UnaryOperator;

import static pl.lodz.p.michalsosn.domain.complex.Complex.ZERO;

/**
 * @author Michał Sośnicki
 */
public final class Filters {

    private Filters() {
    }

    public static UnaryOperator<Spectrum2d> filterLowPass(int range) {
        int rangeSq = range * range;
        return filterDistance(distSq -> distSq >= rangeSq, false);
    }

    public static UnaryOperator<Spectrum2d> filterHighPass(int range,
                                                           boolean preserveMean) {
        int rangeSq = range * range;
        return filterDistance(distSq -> distSq < rangeSq, preserveMean);
    }

    public static UnaryOperator<Spectrum2d> filterBandPass(
            int innerRange, int outerRange, boolean preserveMean
    ) {
        int innerSq = innerRange * innerRange;
        int outerSq = outerRange * outerRange;
        IntPredicate filterPredicate = distSq -> distSq < innerSq || distSq >= outerSq;
        return filterDistance(filterPredicate, preserveMean);
    }

    public static UnaryOperator<Spectrum2d> filterBandStop(
            int innerRange, int outerRange, boolean preserveMean
    ) {
        int innerSq = innerRange * innerRange;
        int outerSq = outerRange * outerRange;
        IntPredicate filterPredicate = distSq -> distSq >= innerSq && distSq < outerSq;
        return filterDistance(filterPredicate, preserveMean);
    }

    public static UnaryOperator<Spectrum2d> filterEdgeDetection(
            int innerRange, int outerRange, double direction,
            double angle, boolean preserveMean
    ) {
        int innerSq = innerRange * innerRange;
        int outerSq = outerRange * outerRange;
        double directionRads = MathUtils.mod(Math.toRadians(direction), Math.PI);
        double angleRads = Math.toRadians(angle / 2);

        return spectrum -> {
            int height = spectrum.getHeight();
            int width = spectrum.getWidth();
            int yMid = (height + 1) / 2;
            int xMid = (width + 1) / 2;

            Complex[][] values = spectrum.copyValues();

            for (int y = 0; y <= yMid; ++y) {
                int dy = yMid - y;
                int ySq = dy * dy;
                for (int x = 0; x <= xMid; ++x) {
                    int dx = xMid - x;
                    int xSq = dx * dx;
                    int distSq = xSq + ySq;

                    boolean zero1 = true;
                    boolean zero2 = true;
                    if (distSq >= innerSq && distSq < outerSq) {
                        double theta1 = Math.atan2(dy, dx);
                        double theta2 = Math.atan2(dy, x - xMid);

                        if (MathUtils.mod(theta1 - directionRads, Math.PI) <= angleRads
                         || MathUtils.mod(directionRads - theta1, Math.PI) <= angleRads) {
                            zero1 = false;
                        }

                        if (MathUtils.mod(theta2 - directionRads, Math.PI) <= angleRads
                         || MathUtils.mod(directionRads - theta2, Math.PI) <= angleRads) {
                            zero2 = false;
                        }
                    }

                    int mirrorY = (height - y) % height;
                    int mirrorX = (width - x) % width;
                    if (zero1) {
                        values[y][x] = ZERO;
                        values[mirrorY][mirrorX] = ZERO;
                    }
                    if (zero2) {
                        values[mirrorY][x] = ZERO;
                        values[y][mirrorX] = ZERO;
                    }
                }
            }

            if (preserveMean && spectrum.getSize() > 0) {
                values[height / 2][width / 2] = spectrum.getValue(height / 2, width / 2);
            }
            return new BufferSpectrum2d(values);
        };
    }

    private static UnaryOperator<Spectrum2d> filterDistance(IntPredicate eraseTest,
                                                            boolean preserveMean) {
        return spectrum -> {
            int height = spectrum.getHeight();
            int width = spectrum.getWidth();
            int yMid = (height + 1) / 2;
            int xMid = (width + 1) / 2;

            Complex[][] values = spectrum.copyValues();

            for (int y = 0; y <= yMid; ++y) {
                int dy = yMid - y;
                int ySq = dy * dy;
                for (int x = 0; x <= xMid; ++x) {
                    int dx = xMid - x;
                    int xSq = dx * dx;
                    if (eraseTest.test(xSq + ySq)) {
                        int mirrorY = (height - y) % height;
                        int mirrorX = (width - x) % width;
                        values[y][x] = ZERO;
                        values[mirrorY][x] = ZERO;
                        values[y][mirrorX] = ZERO;
                        values[mirrorY][mirrorX] = ZERO;
                    }
                }
            }

            if (preserveMean && spectrum.getSize() > 0) {
                values[height / 2][width / 2] = spectrum.getValue(height / 2, width / 2);
            }
            return new BufferSpectrum2d(values);
        };
    }

}
