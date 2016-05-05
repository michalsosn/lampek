package pl.lodz.p.michalsosn.domain.util;

/**
 * @author Michał Sośnicki
 */
public final class MathUtils {

    private MathUtils() {
    }

    public static double mod(double a, double b) {
        double result = a % b;
        if (result < 0) {
            result += b;
        }
        return result;
    }

    public static int mod(int a, int b) {
        int result = a % b;
        if (result < 0) {
            result += b;
        }
        return result;
    }

    public static int gcd(int a, int b) {
        while (b != 0) {
            int t = b;
            b = a % b;
            a = t;
        }
        return a;
    }

    public static double sinc(double x) {
        double t = x * Math.PI;
        return x == 0.0 ? 1.0 : Math.sin(t) / t;
    }

    public static boolean isPowerOfTwo(int a) {
        return (a & (a - 1)) == 0;
    }

    public static int log2(int a) {
        int r = 0;
        while (a > 1) {
            a >>= 1;
            r += 1;
        }
        return r;
    }

    public static double pow2(double x) {
        return x * x;
    }

    public static int reverseBits(int a, int n) {
        int b = 0;
        while (n > 0) {
            b <<= 1;
            b |= a & 1;
            a >>= 1;
            --n;
        }
        return b;
    }

}
