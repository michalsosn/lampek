package pl.lodz.p.michalsosn.domain.image.statistic;

import pl.lodz.p.michalsosn.domain.image.channel.Channel;
import pl.lodz.p.michalsosn.domain.image.image.Image;

import java.util.Map;
import java.util.OptionalDouble;

/**
 * @author Michał Sośnicki
 */
public final class Errors {

    private Errors() {
    }

    public static OptionalDouble averagePower(Image image) {
        return image.getChannels().values().stream()
                .flatMapToInt(Channel::values)
                .mapToDouble(value -> value * value)
                .average();
    }

    public static OptionalDouble meanSquaredError(Image expected,
                                                  Image actual) {
        if (!expected.isEqualSize(actual)) {
            throw new IllegalArgumentException("Images differ in size.");
        }

        Map<String, Channel> expectedChannels = expected.getChannels();
        Map<String, Channel> actualChannels = actual.getChannels();
        if (!expectedChannels.keySet().equals(actualChannels.keySet())) {
            throw new IllegalArgumentException(
                    "Images have different channels."
            );
        }
        int channelCount = expectedChannels.size();

        if (expected.getSize() == 0) {
            return OptionalDouble.empty();
        }
        int height = expected.getHeight();
        int width = expected.getWidth();

        double sum = 0;
        for (String name : expectedChannels.keySet()) {
            Channel expectedChannel = expectedChannels.get(name);
            Channel actualChannel = actualChannels.get(name);
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    double diff = expectedChannel.getValue(y, x)
                                - actualChannel.getValue(y, x);
                    sum += diff * diff;
                }
            }
        }

        return OptionalDouble.of(sum / (height * width * channelCount));
    }

    public static OptionalDouble signalNoiseRatio(Image expected,
                                                  Image channel) {
        OptionalDouble meanSquareError = meanSquaredError(expected, channel);
        OptionalDouble averagePower = averagePower(channel);

        if (!meanSquareError.isPresent() || !averagePower.isPresent()) {
            return OptionalDouble.empty();
        }

        return OptionalDouble.of(10 * Math.log10(
                averagePower.getAsDouble() / meanSquareError.getAsDouble()
        ));
    }

    public static OptionalDouble peakSignalNoiseRatio(Image expected,
                                                      Image channel) {
        OptionalDouble meanSquareError = meanSquaredError(expected, channel);

        if (!meanSquareError.isPresent()) {
            return OptionalDouble.empty();
        }

        return OptionalDouble.of(10 * Math.log10(
                Image.MAX_VALUE / meanSquareError.getAsDouble()
        ));
    }

    public static OptionalDouble effectiveNumberOfBits(Image expected,
                                                       Image channel) {
        OptionalDouble signalNoiseRatio = signalNoiseRatio(expected, channel);

        if (!signalNoiseRatio.isPresent()) {
            return OptionalDouble.empty();
        }

        return OptionalDouble.of(
                (signalNoiseRatio.getAsDouble() - 1.76) / 6.02
        );
    }

}
