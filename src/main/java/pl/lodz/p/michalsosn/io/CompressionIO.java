package pl.lodz.p.michalsosn.io;

import pl.lodz.p.michalsosn.domain.image.spectrum.BufferSpectrum;
import pl.lodz.p.michalsosn.domain.image.spectrum.Complex;
import pl.lodz.p.michalsosn.domain.image.spectrum.ImageSpectrum;
import pl.lodz.p.michalsosn.domain.image.spectrum.Spectrum;
import pl.lodz.p.michalsosn.util.Maps;

import java.io.*;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * @author Michał Sośnicki
 */
public final class CompressionIO {

    private CompressionIO() {
    }

    public static byte[] fromImageSpectrum(ImageSpectrum imageSpectrum)
            throws IOException {
        Map<String, Spectrum> spectra = imageSpectrum.getSpectra();
        Map<String, OutputStreamConsumer> consumers = Maps.applyToValues(
                spectra, spectrum -> dataStream -> {
                    int height = spectrum.getHeight();
                    int width = spectrum.getWidth();
                    dataStream.writeInt(height);
                    dataStream.writeInt(width);
                    for (int y = 0; y < height; ++y) {
                        for (int x = 0; x < width; ++x) {
                            Complex value = spectrum.getValue(y, x);
                            dataStream.writeDouble(value.getRe());
                            dataStream.writeDouble(value.getIm());
                        }
                    }
                }
        );
        return writeMultiple(consumers);
    }

    public static ImageSpectrum toImageSpectrum(byte[] bytes)
            throws IOException {
        Map<String, Spectrum> spectra = readMultiple(bytes,
                dataStream -> {
            int height = dataStream.readInt();
            int width = dataStream.readInt();

            Complex[][] values = new Complex[height][width];
            for (int y = 0; y < height; ++y) {
                for (int x = 0; x < width; ++x) {
                    double re = dataStream.readDouble();
                    double im = dataStream.readDouble();
                    values[y][x] = Complex.ofReIm(re, im);
                }
            }

            return new BufferSpectrum(values);
        });
        return new ImageSpectrum(spectra);
    }

    public static byte[] fromDoubleArray(double[][] array) throws IOException {
        return writeSingle("double_array.data", dataStream -> {
            int height = array.length;
            dataStream.writeInt(height);
            if (height == 0) {
                return;
            }

            int width = array[0].length;
            dataStream.writeInt(width);
            for (int y = 0; y < height; ++y) {
                for (int x = 0; x < width; ++x) {
                    dataStream.writeDouble(array[y][x]);
                }
            }
        });
    }

    public static double[][] toDoubleArray(byte[] bytes)
            throws IOException {
        return readSingle(bytes, "double_array.data", dataStream -> {
            int height = dataStream.readInt();
            if (height == 0) {
                return new double[0][0];
            }

            int width = dataStream.readInt();
            double[][] array = new double[height][width];

            for (int y = 0; y < height; ++y) {
                for (int x = 0; x < width; ++x) {
                    array[y][x] = dataStream.readDouble();
                }
            }

            return array;
        });
    }

    @FunctionalInterface
    private interface OutputStreamConsumer {
        void write(DataOutputStream outputStream) throws IOException;
    }

    private static byte[] writeSingle(
            String entryName, OutputStreamConsumer consumer
    ) throws IOException {
        return writeMultiple(Collections.singletonMap(entryName, consumer));
    }

    private static byte[] writeMultiple(
            Map<String, OutputStreamConsumer> consumers
    ) throws IOException {
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        try (ZipOutputStream zipStream = new ZipOutputStream(byteStream)) {

            for (String entryName : consumers.keySet()) {
                ZipEntry singleEntry = new ZipEntry(entryName);
                zipStream.putNextEntry(singleEntry);

                DataOutputStream dataStream = new DataOutputStream(zipStream);
                consumers.get(entryName).write(dataStream);
            }

        }
        return byteStream.toByteArray();
    }

    @FunctionalInterface
    private interface InputStreamConsumer<T> {
        T read(DataInputStream inputStream) throws IOException;
    }

    private static <T> T readSingle(
            byte[] bytes, String entryName, InputStreamConsumer<T> consumer
    ) throws IOException {
        return readMultiple(
                bytes, Collections.singletonMap(entryName, consumer)
        ).get(entryName);
    }

    /**
     * Reads data compressed with zip, passing entry to a matching consumers.
     * It will ignore entries that does not have consumers associated in the
     * consumer map.
     * @param bytes Byte array with data compressed with zip.
     * @param consumers A map from zip entry names to consumers that are to
     *                  read data from them.
     * @param <T> Type of results returned by the consumers.
     * @return A map associating zip entry names and data returned by consumers.
     * @throws IOException when IO goes wrong
     */
    private static <T> Map<String, T> readMultiple(
            byte[] bytes, Map<String, InputStreamConsumer<T>> consumers
    ) throws IOException {
        Map<String, T> resultMap = new HashMap<>();

        try (ByteArrayInputStream byteStream = new ByteArrayInputStream(bytes);
             ZipInputStream zipStream = new ZipInputStream(byteStream)) {

            ZipEntry nextEntry = zipStream.getNextEntry();
            while (nextEntry != null) {
                InputStreamConsumer<T> consumer
                        = consumers.get(nextEntry.getName());
                if (consumer != null) {
                    DataInputStream dataStream = new DataInputStream(zipStream);
                    T result = consumer.read(dataStream);
                    resultMap.put(nextEntry.getName(), result);
                }
                nextEntry = zipStream.getNextEntry();
            }
        }

        return resultMap;
    }

    /**
     * Reads all entries and passess it to the consumer, expecting it to handle
     * them all.
     * @param bytes Byte array with data compressed with zip.
     * @param consumer Consumer that can read data from each zipped file.
     * @param <T> Type of data read by the InputStreamConsumer.
     * @return A map containing all results generated by the consumer.
     * @throws IOException when an IO operation fails
     */
    private static <T> Map<String, T> readMultiple(
            byte[] bytes, InputStreamConsumer<T> consumer
    ) throws IOException {
        Map<String, T> resultMap = new HashMap<>();

        try (ByteArrayInputStream byteStream = new ByteArrayInputStream(bytes);
             ZipInputStream zipStream = new ZipInputStream(byteStream)) {

            ZipEntry nextEntry = zipStream.getNextEntry();
            while (nextEntry != null) {
                DataInputStream dataStream = new DataInputStream(zipStream);
                T result = consumer.read(dataStream);
                resultMap.put(nextEntry.getName(), result);

                nextEntry = zipStream.getNextEntry();
            }
        }

        return resultMap;
    }

}
