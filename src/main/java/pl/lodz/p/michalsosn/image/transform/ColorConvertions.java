package pl.lodz.p.michalsosn.image.transform;

import pl.lodz.p.michalsosn.image.*;

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

        int[][] grayValues = new int[height][width];

        for (int x = 0; x < height; ++x) {
            for (int y = 0; y < width; y++) {
                int red = redChannel.getValue(x, y);
                int green = greenChannel.getValue(x, y);
                int blue = blueChannel.getValue(x, y);

                grayValues[x][y] = (red + green + blue) / 3;
            }
        }

        Channel grayChannel = new BufferChannel(grayValues);
        return new GrayImage(grayChannel);
    }

    public static RgbImage grayToRgb(GrayImage grayImage) {
        int height = grayImage.getHeight();
        int width = grayImage.getWidth();
        Channel grayChannel = grayImage.getGray();

        int[][] redValues = new int[height][width];
        int[][] greenValues = new int[height][width];
        int[][] blueValues = new int[height][width];

        for (int x = 0; x < height; ++x) {
            for (int y = 0; y < width; y++) {
                int gray = grayChannel.getValue(x, y);

                redValues[x][y] = gray;
                greenValues[x][y] = gray;
                blueValues[x][y] = gray;
            }
        }

        Channel redChannel = new BufferChannel(redValues);
        Channel greenChannel = new BufferChannel(greenValues);
        Channel blueChannel = new BufferChannel(blueValues);
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

        Channel[] channels = {
                new ConstChannel(height, width, 0),
                new ConstChannel(height, width, 0),
                new ConstChannel(height, width, 0)
        };

        channels[channelIndex] = replacement;

        return new RgbImage(channels[0], channels[1], channels[2]);
    }

}
