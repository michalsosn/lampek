package pl.lodz.p.michalsosn.io;

import pl.lodz.p.michalsosn.domain.image.channel.BufferChannel;
import pl.lodz.p.michalsosn.domain.image.channel.Channel;
import pl.lodz.p.michalsosn.domain.image.channel.GrayImage;
import pl.lodz.p.michalsosn.domain.image.channel.Image;
import pl.lodz.p.michalsosn.domain.image.channel.ImageVisitor;
import pl.lodz.p.michalsosn.domain.image.channel.RgbImage;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Path;

/**
 * @author Michał Sośnicki
 */
public final class BufferedImageIO {

    private static final String DEFAULT_FORMAT = "png";

    private BufferedImageIO() {
    }

    public static byte[] toByteArray(BufferedImage image) throws IOException {
        ByteArrayOutputStream arrayOutputStream
                = new ByteArrayOutputStream();
        ImageIO.write(image, DEFAULT_FORMAT, arrayOutputStream);
        return arrayOutputStream.toByteArray();
    }

    public static BufferedImage fromByteArray(byte[] data) throws IOException {
        ByteArrayInputStream arrayInputStream = new ByteArrayInputStream(data);
        return ImageIO.read(arrayInputStream);
    }

    public static Image toImage(BufferedImage bufferedImage)
            throws IOException {
        Raster raster = bufferedImage.getRaster();
        int elementNum = raster.getNumDataElements();

        switch (elementNum) {
            case 1:
                return toGrayImage(bufferedImage);
            case 3:
            case 4:
                return toRgbImage(bufferedImage);
            default:
                throw new IOException(
                        "Can't read image with " + elementNum + " channels."
                );
        }
    }

    private static Image toGrayImage(BufferedImage bufferedImage) {
        int height = bufferedImage.getHeight();
        int width = bufferedImage.getWidth();

        if (bufferedImage.getType() != BufferedImage.TYPE_BYTE_GRAY) {
            BufferedImage convertedImage
                    = new BufferedImage(width, height,
                                        BufferedImage.TYPE_BYTE_GRAY);
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

    private static Image toRgbImage(BufferedImage bufferedImage) {
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

    public static BufferedImage fromImage(Image image) {
        return image.accept(ImageVisitor.imageVisitor(
                grayImage -> {
                    int height = grayImage.getHeight();
                    int width = grayImage.getWidth();
                    Channel grayChannel = grayImage.getGray();

                    BufferedImage bufferedImage
                            = new BufferedImage(width, height,
                                                BufferedImage.TYPE_BYTE_GRAY);
                    WritableRaster raster = bufferedImage.getRaster();

                    grayImage.forEach((y, x) ->
                            raster.setSample(
                                    x, y, 0, grayChannel.getValue(y, x)
                            )
                    );

                    return bufferedImage;
                },
                rgbImage -> {
                    int height = rgbImage.getHeight();
                    int width = rgbImage.getWidth();
                    Channel redChannel = rgbImage.getRed();
                    Channel greenChannel = rgbImage.getGreen();
                    Channel blueChannel = rgbImage.getBlue();

                    BufferedImage bufferedImage
                            = new BufferedImage(width, height,
                                                BufferedImage.TYPE_3BYTE_BGR);

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
    }

    public static Image readImage(Path path) throws IOException {
        BufferedImage bufferedImage = ImageIO.read(path.toFile());
        return toImage(bufferedImage);
    }

    public static void writeImage(Image image, Path path) throws IOException {
        String format = IOUtils.separateExtension(path);
        writeImage(image, path, format);
    }

    public static void writeImage(Image image, Path path, String format)
            throws IOException {
        BufferedImage resultImage = fromImage(image);

        if (!ImageIO.write(resultImage, format, path.toFile())) {
            throw new IllegalArgumentException(
                    "Format " + format + " not supported."
            );
        }
    }

}
