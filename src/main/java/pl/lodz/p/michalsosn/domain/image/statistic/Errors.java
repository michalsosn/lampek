package pl.lodz.p.michalsosn.domain.image.statistic;

import pl.lodz.p.michalsosn.domain.image.channel.Channel;

import java.util.OptionalDouble;
import java.util.OptionalInt;

/**
 * @author Michał Sośnicki
 */
public final class Errors {

    private Errors() {
    }

    public static OptionalDouble averagePower(Channel channel) {
        return channel.values().mapToDouble(value -> value * value).average();
    }

    public static OptionalDouble meanSquareError(Channel expected, Channel channel) {
        if (!expected.isEqualSize(channel)) {
            throw new IllegalArgumentException("Channels differ in size.");
        }
        if (expected.getSize() == 0) {
            return OptionalDouble.empty();
        }

        int height = expected.getHeight();
        int width = expected.getWidth();

        double sum = 0;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                double diff = expected.getValue(y, x) - channel.getValue(y, x);
                sum += diff * diff;
            }
        }

        return OptionalDouble.of(sum / (height * width));
    }

    public static OptionalDouble signalNoiseRatio(Channel expected, Channel channel) {
        OptionalDouble meanSquareError = meanSquareError(expected, channel);
        OptionalDouble averagePower = averagePower(channel);

        if (!meanSquareError.isPresent() || !averagePower.isPresent()) {
            return OptionalDouble.empty();
        }

        return OptionalDouble.of(10 * Math.log10(
                averagePower.getAsDouble() / meanSquareError.getAsDouble()
        ));
    }

    public static OptionalDouble peakSignalNoiseRatio(Channel expected, Channel channel) {
        OptionalDouble meanSquareError = meanSquareError(expected, channel);
        OptionalInt max = channel.values().max();

        if (!meanSquareError.isPresent() || !max.isPresent()) {
            return OptionalDouble.empty();
        }

        return OptionalDouble.of(10 * Math.log10(
                max.getAsInt() / meanSquareError.getAsDouble()
        ));
    }

    public static OptionalDouble effectiveNumberOfBits(Channel expected, Channel channel) {
        OptionalDouble signalNoiseRatio = signalNoiseRatio(expected, channel);

        if (!signalNoiseRatio.isPresent()) {
            return OptionalDouble.empty();
        }

        return OptionalDouble.of((signalNoiseRatio.getAsDouble() - 1.76) / 6.02);
    }

}
