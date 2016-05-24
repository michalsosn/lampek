package pl.lodz.p.michalsosn.domain.complex;

import pl.lodz.p.michalsosn.domain.util.MathUtils;

import java.util.stream.IntStream;

/**
 * @author Michał Sośnicki
 */
public final class Fourier {
    private Fourier() {
    }

    public static void fft(Complex[] row, Complex[] kernel, boolean normalize) {
        final int length = row.length;
        final int recDepth = MathUtils.log2(length);

        for (int i = 0; i < length; ++i) {
            int rev = MathUtils.reverseBits(i, recDepth);
            if (rev < i) {
                Complex temp = row[i];
                row[i] = row[rev];
                row[rev] = temp;
            }
        }

        Complex temp;
        int half;
        int width = 1;
        int kernelStep = length;
        for (int depth = 0; depth < recDepth; ++depth) {
            half = width;
            width <<= 1;
            kernelStep >>= 1;
            for (int offset = 0; offset < length; offset += width) {
                int halfOffset = offset + half;
                for (int i = 1; i < half; ++i) {
                    row[i + halfOffset]
                            = kernel[i * kernelStep]
                            .multiply(row[i + halfOffset]);
                }
                for (int i = offset; i < halfOffset; ++i) {
                    temp = row[i].add(row[i + half]);
                    row[i + half] = row[i].subtract(row[i + half]);
                    row[i] = temp;
                }
            }
        }

        if (normalize) {
            for (int i = 0; i < length; ++i) {
                row[i] = row[i].scale(1.0 / length);
            }
        }
    }

    public static Complex[] fourierBasis(int basicPeriod) {
        return fourierBasis(basicPeriod, basicPeriod);
    }

    public static Complex[] fourierBasis(int basicPeriod, int size) {
        return IntStream.range(0, size)
                .mapToObj(kn -> ReImComplex.ofPolar(
                        1.0, -kn * 2 * Math.PI / basicPeriod
                )).toArray(Complex[]::new);
    }

    public static Complex[] inverseBasis(int basicPeriod) {
        return inverseBasis(basicPeriod, basicPeriod);
    }

    public static Complex[] inverseBasis(int basicPeriod, int size) {
        return IntStream.range(0, size)
                .mapToObj(kn -> ReImComplex.ofPolar(
                        1.0, kn * 2 * Math.PI / basicPeriod
                )).toArray(Complex[]::new);
    }
}
