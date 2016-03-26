package pl.lodz.p.michalsosn.io;

import pl.lodz.p.michalsosn.domain.image.channel.BufferChannel;
import pl.lodz.p.michalsosn.domain.image.channel.Channel;
import pl.lodz.p.michalsosn.domain.image.image.GrayImage;
import pl.lodz.p.michalsosn.domain.image.image.Image;
import pl.lodz.p.michalsosn.domain.image.image.ImageVisitor;
import pl.lodz.p.michalsosn.domain.image.image.RgbImage;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.IOException;
import java.nio.file.Path;

/**
 * @author Michał Sośnicki
 */
public final class BufferedImageIO {

    private BufferedImageIO() { }

    public static Image readImage(Path path) throws IOException {
        BufferedImage bufferedImage = ImageIO.read(path.toFile());

        Raster raster = bufferedImage.getRaster();
        int elementNum = raster.getNumDataElements();

        switch (elementNum) {
            case 1:
                return readGrayImage(bufferedImage);
            case 3:
                return readRgbImage(bufferedImage);
            default:
                throw new IOException("Can't read image with " + elementNum + " channels.");
        }
    }

    private static Image readGrayImage(BufferedImage bufferedImage) {
        int height = bufferedImage.getHeight();
        int width = bufferedImage.getWidth();

        if (bufferedImage.getType() != BufferedImage.TYPE_BYTE_GRAY) {
            BufferedImage convertedImage = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
            Graphics2D g2d = convertedImage.createGraphics();
            g2d.drawImage(bufferedImage, 0, 0, null);
            g2d.dispose();
            bufferedImage = convertedImage;
        }

        WritableRaster raster = bufferedImage.getRaster();

        int[][] newValues = new int[height][width];

        for (int y = 0; y < height; y++) {
            raster.getPixels(0, y, width, 1, newValues[y]);
        }

        Channel grayChannel = new BufferChannel(newValues);
        return new GrayImage(grayChannel);
    }

    private static Image readRgbImage(BufferedImage bufferedImage) {
        int height = bufferedImage.getHeight();
        int width = bufferedImage.getWidth();

        int[][] redValues = new int[height][width];
        int[][] greenValues = new int[height][width];
        int[][] blueValues = new int[height][width];

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int color = bufferedImage.getRGB(x, y);

                int blue = color & 0xff;
                int green = (color & 0xff00) >> 8;
                int red = (color & 0xff0000) >> 16;

                redValues[y][x] = red;
                greenValues[y][x] = green;
                blueValues[y][x] = blue;
            }
        }

        Channel redChannel = new BufferChannel(redValues);
        Channel greenChannel = new BufferChannel(greenValues);
        Channel blueChannel = new BufferChannel(blueValues);
        return new RgbImage(redChannel, greenChannel, blueChannel);
    }

    public static void writeImage(Image image, Path path) throws IOException {
        String fileName = path.getFileName().toString();
        int formatStart = fileName.lastIndexOf('.');
        if (formatStart < 0) {
            throw new IllegalArgumentException(path + " has no extension.");
        }
        String format = fileName.substring(formatStart + 1);
        writeImage(image, path, format);
    }

    public static void writeImage(Image image, Path path, String format) throws IOException {
        BufferedImage resultImage = image.accept(ImageVisitor.imageVisitor(
                grayImage -> {
                    int height = grayImage.getHeight();
                    int width = grayImage.getWidth();
                    Channel grayChannel = grayImage.getGray();

                    BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
                    WritableRaster raster = bufferedImage.getRaster();

                    grayImage.forEach((y, x) ->
                            raster.setSample(x, y, 0, grayChannel.getValue(y, x))
                    );

                    return bufferedImage;
                },
                rgbImage -> {
                    int height = rgbImage.getHeight();
                    int width = rgbImage.getWidth();
                    Channel redChannel = rgbImage.getRed();
                    Channel greenChannel = rgbImage.getGreen();
                    Channel blueChannel = rgbImage.getBlue();

                    BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);

                    rgbImage.forEach((y, x) -> {
                        int red = redChannel.getValue(y, x);
                        int green = greenChannel.getValue(y, x);
                        int blue = blueChannel.getValue(y, x);

                        int value = blue | green << 8 | red << 16;
                        bufferedImage.setRGB(x, y, value);
                    });

                    return bufferedImage;
                }
        ));

        if (!ImageIO.write(resultImage, format, path.toFile())) {
            throw new IllegalArgumentException("Format " + format + " not supported.");
        }
    }

}
