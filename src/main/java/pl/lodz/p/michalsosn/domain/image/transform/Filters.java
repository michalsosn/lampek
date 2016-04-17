package pl.lodz.p.michalsosn.domain.image.transform;

import pl.lodz.p.michalsosn.domain.image.spectrum.BufferSpectrum;
import pl.lodz.p.michalsosn.domain.image.spectrum.Complex;
import pl.lodz.p.michalsosn.domain.image.spectrum.Spectrum;
import pl.lodz.p.michalsosn.domain.util.MathUtils;

import java.util.function.IntPredicate;
import java.util.function.UnaryOperator;

import static pl.lodz.p.michalsosn.domain.image.spectrum.Complex.ZERO;

/**
 * @author Michał Sośnicki
 */
public final class Filters {

    private Filters() {
    }

    public static UnaryOperator<Spectrum> filterLowPass(int range) {
        int rangeSq = range * range;
        return filterDistance(distSq -> distSq >= rangeSq);
    }

    public static UnaryOperator<Spectrum> filterHighPass(int range) {
        int rangeSq = range * range;
        return filterDistance(distSq -> distSq < rangeSq);
    }

    public static UnaryOperator<Spectrum> filterBandPass(int innerRange,
                                                         int outerRange) {
        int innerSq = innerRange * innerRange;
        int outerSq = outerRange * outerRange;
        return filterDistance(distSq -> distSq < innerSq || distSq >= outerSq);
    }

    public static UnaryOperator<Spectrum> filterBandStop(int innerRange,
                                                         int outerRange) {
        int innerSq = innerRange * innerRange;
        int outerSq = outerRange * outerRange;
        return filterDistance(distSq -> distSq >= innerSq && distSq < outerSq);
    }

    public static UnaryOperator<Spectrum> filterEdgeDetection(
            int innerRange, int outerRange, double direction, double angle
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

            for (int y = 0; y < yMid; ++y) {
                int dy = yMid - y;
                int ySq = dy * dy;
                for (int x = 0; x < xMid; ++x) {
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

                    int mirrorY = height - y - 1;
                    int mirrorX = width - x - 1;
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

            return new BufferSpectrum(values);
        };
    }

    private static UnaryOperator<Spectrum> filterDistance(IntPredicate eraseTest) {
        return spectrum -> {
            int height = spectrum.getHeight();
            int width = spectrum.getWidth();
            int yMid = (height + 1) / 2;
            int xMid = (width + 1) / 2;

            Complex[][] values = spectrum.copyValues();

            for (int y = 0; y < yMid; ++y) {
                int dy = yMid - y;
                int ySq = dy * dy;
                for (int x = 0; x < xMid; ++x) {
                    int dx = xMid - x;
                    int xSq = dx * dx;
                    if (eraseTest.test(xSq + ySq)) {
                        int mirrorY = height - y - 1;
                        int mirrorX = width - x - 1;
                        values[y][x] = ZERO;
                        values[mirrorY][x] = ZERO;
                        values[y][mirrorX] = ZERO;
                        values[mirrorY][mirrorX] = ZERO;
                    }
                }
            }

            return new BufferSpectrum(values);
        };
    }

}
