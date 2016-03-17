package pl.lodz.p.michalsosn.image.transform;

import pl.lodz.p.michalsosn.image.image.GrayImage;
import pl.lodz.p.michalsosn.image.image.RgbImage;
import pl.lodz.p.michalsosn.image.channel.Channel;

import java.util.function.IntBinaryOperator;

import static pl.lodz.p.michalsosn.image.channel.Channels.constOfType;
import static pl.lodz.p.michalsosn.image.channel.Channels.constructOfType;

/**
 * @author Michał Sośnicki
 */
public final class ColorConvertions {

    private ColorConvertions() {
    }

    public static GrayImage rgbToGray(RgbImage rgbImage) {
        int height = rgbImage.getHeight();
        int width = rgbImage.getWidth();
        Channel redChannel = rgbImage.getRed();
        Channel greenChannel = rgbImage.getGreen();
        Channel blueChannel = rgbImage.getBlue();

        IntBinaryOperator grayFunction = (y, x) -> {
            int red = redChannel.getValue(y, x);
            int green = greenChannel.getValue(y, x);
            int blue = blueChannel.getValue(y, x);
            return (red + green + blue) / 3;
        };

        Channel grayChannel = constructOfType(redChannel, height, width, grayFunction);
        return new GrayImage(grayChannel);
    }

    public static RgbImage grayToRgb(GrayImage grayImage) {
        int height = grayImage.getHeight();
        int width = grayImage.getWidth();
        Channel grayChannel = grayImage.getGray();

        IntBinaryOperator colorFunction = grayChannel::getValue;

        Channel redChannel = constructOfType(grayChannel, height, width, colorFunction);
        Channel greenChannel = constructOfType(grayChannel, height, width, colorFunction);
        Channel blueChannel = constructOfType(grayChannel, height, width, colorFunction);
        return new RgbImage(redChannel, greenChannel, blueChannel);
    }

    public static RgbImage extractRed(RgbImage rgbImage) {
        return extractColor(rgbImage, 0, rgbImage.getRed());
    }

    public static RgbImage extractGreen(RgbImage rgbImage) {
        return extractColor(rgbImage, 1, rgbImage.getGreen());
    }

    public static RgbImage extractBlue(RgbImage rgbImage) {
        return extractColor(rgbImage, 2, rgbImage.getBlue());
    }

    private static RgbImage extractColor(RgbImage rgbImage, int channelIndex, Channel replacement) {
        int height = rgbImage.getHeight();
        int width = rgbImage.getWidth();
        Channel redChannel = rgbImage.getRed();

        Channel[] channels = {
                constOfType(redChannel, height, width, 0),
                constOfType(redChannel, height, width, 0),
                constOfType(redChannel, height, width, 0)
        };

        channels[channelIndex] = replacement;

        return new RgbImage(channels[0], channels[1], channels[2]);
    }

}
