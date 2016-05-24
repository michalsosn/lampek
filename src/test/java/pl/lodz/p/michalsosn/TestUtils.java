package pl.lodz.p.michalsosn;

import org.hamcrest.Matcher;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.arrayContaining;
import static org.hamcrest.number.IsCloseTo.closeTo;

/**
 * @author Michał Sośnicki
 */
public interface TestUtils {

    double DELTA = 2e-5;

    static boolean close(double first, double second) {
        return close(DELTA, first, second);
    }

    static boolean close(double error, double first, double second) {
        return Math.abs(first - second) < error;
    }

    static Matcher<Double[]> arrayCloseTo(double error, double... array) {
        List<Matcher<? super Double>> matchers = new ArrayList<>();
        for (double d : array) {
            matchers.add(closeTo(d, error));
        }
        return arrayContaining(matchers);
    }

    static Matcher<Double[]> arrayCloseTo(double error, Double... array) {
        List<Matcher<? super Double>> matchers = new ArrayList<>();
        for (double d : array) {
            matchers.add(closeTo(d, error));
        }
        return arrayContaining(matchers);
    }

}
