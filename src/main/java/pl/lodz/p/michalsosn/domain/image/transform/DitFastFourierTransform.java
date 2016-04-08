package pl.lodz.p.michalsosn.domain.image.transform;

import pl.lodz.p.michalsosn.domain.image.Size2d;
import pl.lodz.p.michalsosn.domain.image.channel.BufferChannel;
import pl.lodz.p.michalsosn.domain.image.channel.Channel;
import pl.lodz.p.michalsosn.domain.image.spectrum.BufferSpectrum;
import pl.lodz.p.michalsosn.domain.image.spectrum.Complex;
import pl.lodz.p.michalsosn.domain.image.spectrum.ReImComplex;
import pl.lodz.p.michalsosn.domain.image.spectrum.Spectrum;
import pl.lodz.p.michalsosn.util.MathUtils;

import java.util.Arrays;
import java.util.function.BiFunction;
import java.util.function.IntFunction;
import java.util.stream.IntStream;


/**
 * @author Michał Sośnicki
 */
public final class DitFastFourierTransform {

    private DitFastFourierTransform() {
    }

    public static Spectrum transform(Channel channel) {
        checkSizePowerOfTwo(channel);

        int[][] values = channel.copyValues();
        Complex[][] complexValues = Arrays.stream(values).map(
                row -> Arrays.stream(row).mapToObj(Complex::ofRe)
                        .toArray(Complex[]::new)
        ).toArray(Complex[][]::new);

        complexValues =
            fftArray(complexValues, DitFastFourierTransform::transformKernel);
        complexValues = transposeMatrix(
                complexValues, (y, x) -> new Complex[y][x]
        );

        return new BufferSpectrum(complexValues);
    }

    public static Channel inverse(Spectrum spectrum) {
        checkSizePowerOfTwo(spectrum);

        Complex[][] values = spectrum.copyValues();

        values = transposeMatrix(values, (y, x) -> new Complex[y][x]);
        values = fftArray(values, DitFastFourierTransform::inverseKernel);

        int[][] integerValues = Arrays.stream(values).map(
                row -> Arrays.stream(row).mapToInt(
                        v -> (int) Math.round(v.getRe())
                ).toArray()
        ).toArray(int[][]::new);

        return new BufferChannel(integerValues);
    }

    private static Complex[][] fftArray(Complex[][] array,
                                 IntFunction<Complex[]> makeKernel) {
        if (array.length == 0) {
            return array;
        }

        int height = array.length;
        int width = array[0].length;
        Complex[] rowKernel = makeKernel.apply(width);
        Arrays.stream(array).forEach(row ->
                fftSingleRow(row, rowKernel, false)
        );

        Complex[][] transposed =  transposeMatrix(
                array, (h, w) -> new Complex[h][w]
        );

        Complex[] colKernel = makeKernel.apply(height);
        Arrays.stream(array).forEach(col ->
                fftSingleRow(col, colKernel, true)
        );

        return transposed;
    }

    private static <T> T[][] transposeMatrix(
            T[][] matrix, BiFunction<Integer, Integer, T[][]> matrixMaker
    ) {
        if (matrix.length == 0) {
            return matrix;
        }

        int height = matrix.length;
        int width = matrix[0].length;

        if (height == width) {
            T temp;
            for (int size = height - 1; size > 1; --size) {
                for (int i = 1; i < size; ++i) {
                    temp = matrix[size][i];
                    matrix[size][i] = matrix[i][size];
                    matrix[i][size] = temp;
                }
            }
            return matrix;
        } else {
            T[][] transposed = matrixMaker.apply(width, height);
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    transposed[x][y] = matrix[y][x];
                }
            }
            return transposed;
        }
    }

    private static void fftSingleRow(
            Complex[] row, Complex[] kernel, boolean normalize
    ) {
        int length = row.length;
        int recDepth = MathUtils.log2(length);

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

    private static Complex[] transformKernel(int basicPeriod) {
        return IntStream.range(0, basicPeriod / 2)
                .mapToObj(kn -> ReImComplex.ofPolar(
                        1.0, -kn * 2 * Math.PI / basicPeriod
                )).toArray(Complex[]::new);
    }

    private static Complex[] inverseKernel(int basicPeriod) {
        return IntStream.range(0, basicPeriod / 2)
                .mapToObj(kn -> ReImComplex.ofPolar(
                        1.0, kn * 2 * Math.PI / basicPeriod
                )).toArray(Complex[]::new);
    }

    private static void checkSizePowerOfTwo(Size2d size2d) {
        int height = size2d.getHeight();
        int width = size2d.getWidth();
        if (!MathUtils.isPowerOfTwo(height)) {
            throw new IllegalArgumentException(
                    "Height not a power of two but " + height
            );
        }
        if (!MathUtils.isPowerOfTwo(width)) {
            throw new IllegalArgumentException(
                    "Width not a power of two but " + width
            );
        }
    }

}
