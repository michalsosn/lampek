package pl.lodz.p.michalsosn.domain.image.transform;

import pl.lodz.p.michalsosn.domain.complex.Complex;
import pl.lodz.p.michalsosn.domain.complex.Fourier;
import pl.lodz.p.michalsosn.domain.image.Size2d;
import pl.lodz.p.michalsosn.domain.image.channel.BufferChannel;
import pl.lodz.p.michalsosn.domain.image.channel.Channel;
import pl.lodz.p.michalsosn.domain.image.spectrum.BufferSpectrum2d;
import pl.lodz.p.michalsosn.domain.image.spectrum.Spectrum2d;
import pl.lodz.p.michalsosn.domain.util.IntBiFunction;
import pl.lodz.p.michalsosn.domain.util.MathUtils;

import java.util.Arrays;
import java.util.function.IntFunction;

import static pl.lodz.p.michalsosn.domain.image.channel.Image.MAX_VALUE;
import static pl.lodz.p.michalsosn.domain.image.channel.Image.MIN_VALUE;


/**
 * @author Michał Sośnicki
 */
public final class DitFastFourierTransform {

    private DitFastFourierTransform() {
    }

    public static Spectrum2d transform(Channel channel) {
        checkSizePowerOfTwo(channel);

        int[][] values = channel.copyValues();
        Complex[][] complexValues = Arrays.stream(values).map(
                row -> Arrays.stream(row).mapToObj(Complex::ofRe)
                        .toArray(Complex[]::new)
        ).toArray(Complex[][]::new);

        complexValues = fftArray(
                complexValues, Fourier::fourierBasis, false
        );
        complexValues = transposeMatrix(
                complexValues, (y, x) -> new Complex[y][x]
        );
        moveCenter(complexValues, Complex[]::new);

        return new BufferSpectrum2d(complexValues);
    }

    public static Channel inverse(Spectrum2d spectrum) {
        checkSizePowerOfTwo(spectrum);

        Complex[][] values = spectrum.copyValues();

        moveCenter(values, Complex[]::new);
        values = transposeMatrix(values, (y, x) -> new Complex[y][x]);
        values = fftArray(values, Fourier::inverseBasis, true);

        int[][] integerValues = Arrays.stream(values).map(
                row -> Arrays.stream(row).mapToInt(v -> (int) Math.round(
                        Math.max(MIN_VALUE, Math.min(MAX_VALUE, v.getAbs()))
                )).toArray()
        ).toArray(int[][]::new);

        return new BufferChannel(integerValues);
    }

    private static Complex[][] fftArray(Complex[][] array,
                                        IntBiFunction<Complex[]> makeKernel,
                                        boolean normalize) {
        if (array.length == 0) {
            return array;
        }

        int height = array.length;
        int width = array[0].length;
        Complex[] rowKernel = makeKernel.apply(width, width / 2);
        Arrays.stream(array).forEach(row ->
                Fourier.fft(row, rowKernel, normalize)
        );

        Complex[][] transposed =  transposeMatrix(
                array, (h, w) -> new Complex[h][w]
        );

        Complex[] colKernel = makeKernel.apply(height, height / 2);
        Arrays.stream(array).forEach(col ->
                Fourier.fft(col, colKernel, normalize)
        );

        return transposed;
    }

    private static <T> T[][] transposeMatrix(
            T[][] matrix, IntBiFunction<T[][]> matrixMaker
    ) {
        if (matrix.length == 0) {
            return matrix;
        }

        int height = matrix.length;
        int width = matrix[0].length;

        if (height == width) {
            T temp;
            for (int size = height - 1; size > 1; --size) {
                for (int i = 0; i < size; ++i) {
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

    private static <T> void moveCenter(
            T[][] matrix, IntFunction<T[]> arrayMaker
    ) {
        if (matrix.length == 0) {
            return;
        }

        int height = matrix.length;
        int width = matrix[0].length;
        int halfHeight = height / 2; // assumes even sizes
        int halfWidth = width / 2;

        T[] temp = arrayMaker.apply(halfWidth);
        for (int y = 0; y < height; y++) {
            int pairY = (y + halfHeight) % height;
            System.arraycopy(matrix[y], 0, temp, 0, halfWidth);
            System.arraycopy(matrix[pairY], halfWidth, matrix[y], 0, halfWidth);
            System.arraycopy(temp, 0, matrix[pairY], halfWidth, halfWidth);
        }
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
