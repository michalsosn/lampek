package pl.lodz.p.michalsosn.image.io;

import pl.lodz.p.michalsosn.image.*;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.awt.image.Raster;
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
        int width = bufferedImage.getWidth();
        int height = bufferedImage.getHeight();

        int[][] newValues = new int[width][height];

        for (int x = 0; x < height; x++) {
            for (int y = 0; y < width; y++) {
                int gray = bufferedImage.getRGB(x, y) & 0xff;
                newValues[x][y] = gray;
            }
        }

        Channel grayChannel = new Channel(newValues);
        return new GrayImage(grayChannel);
    }

    private static Image readRgbImage(BufferedImage bufferedImage) {
        int width = bufferedImage.getWidth();
        int height = bufferedImage.getHeight();

        int[][] redValues = new int[width][height];
        int[][] greenValues = new int[width][height];
        int[][] blueValues = new int[width][height];

        for (int x = 0; x < height; x++) {
            for (int y = 0; y < width; y++) {
                int color = bufferedImage.getRGB(x, y);

                int blue = color & 0xff;
                int green = (color & 0xff00) >> 8;
                int red = (color & 0xff0000) >> 16;

                redValues[x][y] = red;
                greenValues[x][y] = green;
                blueValues[x][y] = blue;
            }
        }

        Channel redChannel = new Channel(redValues);
        Channel greenChannel = new Channel(greenValues);
        Channel blueChannel = new Channel(blueValues);
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
        class WriteVisitor implements ImageVisitor {
            private BufferedImage bufferedImage = null;

            @Override
            public void visit(GrayImage image) {
                int height = image.getHeight();
                int width = image.getWidth();
                Channel gray = image.getGray();

                bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
                int[] buffer = ((DataBufferInt) bufferedImage.getRaster().getDataBuffer()).getData();

                for (int x = 0; x < height; x++) {
                    gray.copyTo(x, buffer, x * width);
                }
            }

            @Override
            public void visit(RgbImage image) {
                int width = image.getWidth();
                int height = image.getHeight();
                Channel redChannel = image.getRed();
                Channel greenChannel = image.getGreen();
                Channel blueChannel = image.getBlue();

                bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);

                for (int x = 0; x < height; x++) {
                    for (int y = 0; y < width; y++) {
                        int red = redChannel.getValue(x, y);
                        int green = greenChannel.getValue(x, y);
                        int blue = blueChannel.getValue(x, y);

                        int value = blue | green << 8 | red << 16;
                        bufferedImage.setRGB(x, y, value);
                    }
                }
            }
        }

        WriteVisitor writeVisitor = new WriteVisitor();

        image.accept(writeVisitor);

        if (!ImageIO.write(writeVisitor.bufferedImage, format, path.toFile())) {
            throw new IllegalArgumentException("Format " + format + " not supported.");
        }
    }

}
