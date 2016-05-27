package pl.lodz.p.michalsosn.io;

import pl.lodz.p.michalsosn.domain.complex.Complex;
import pl.lodz.p.michalsosn.domain.image.spectrum.BufferSpectrum2d;
import pl.lodz.p.michalsosn.domain.image.spectrum.ImageSpectrum;
import pl.lodz.p.michalsosn.domain.image.spectrum.Spectrum2d;
import pl.lodz.p.michalsosn.domain.sound.TimeRange;
import pl.lodz.p.michalsosn.domain.sound.filter.BufferFilter;
import pl.lodz.p.michalsosn.domain.sound.filter.Filter;
import pl.lodz.p.michalsosn.domain.sound.signal.BufferSignal;
import pl.lodz.p.michalsosn.domain.sound.signal.Signal;
import pl.lodz.p.michalsosn.domain.sound.sound.BufferSound;
import pl.lodz.p.michalsosn.domain.sound.sound.Sound;
import pl.lodz.p.michalsosn.domain.sound.spectrum.BufferSpectrum1d;
import pl.lodz.p.michalsosn.domain.sound.spectrum.Spectrum1d;
import pl.lodz.p.michalsosn.domain.sound.transform.Note;
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
        final Map<String, Spectrum2d> spectra = imageSpectrum.getSpectra();
        final Map<String, OutputStreamConsumer> consumers = Maps.applyToValues(
                spectra, spectrum -> dataStream -> {
                    final int height = spectrum.getHeight();
                    final int width = spectrum.getWidth();
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
        Map<String, Spectrum2d> spectra = readMultiple(bytes,
                dataStream -> {
            final int height = dataStream.readInt();
            final int width = dataStream.readInt();

            final Complex[][] values = new Complex[height][width];
            for (int y = 0; y < height; ++y) {
                for (int x = 0; x < width; ++x) {
                    final double re = dataStream.readDouble();
                    final double im = dataStream.readDouble();
                    values[y][x] = Complex.ofReIm(re, im);
                }
            }

            return new BufferSpectrum2d(values);
        });
        return new ImageSpectrum(spectra);
    }

    private static final String SOUND_ENTRY = "sound";

    public static byte[] fromSound(Sound sound) throws IOException {
        return writeSingle(SOUND_ENTRY, dataStream -> {
            final double duration = sound.getSamplingTime().getDuration();
            final int length = sound.getLength();
            dataStream.writeDouble(duration);
            dataStream.writeInt(length);
            for (int i = 0; i < length; ++i) {
                dataStream.writeInt(sound.getValue(i));
            }
        });
    }
    public static Sound toSound(byte[] data) throws IOException {
        return readSingle(data, SOUND_ENTRY, dataStream -> {
            final double duration = dataStream.readDouble();
            final int length = dataStream.readInt();
            final int[] values = new int[length];
            for (int i = 0; i < length; ++i) {
                values[i] = dataStream.readInt();
            }
            return new BufferSound(values, TimeRange.ofDuration(duration));
        });
    }

    private static final String SOUND_SPECTRUM_ENTRY = "sound_spectrum";

    public static byte[] fromSoundSpectrum(Spectrum1d spectrum) throws IOException {
        return writeSingle(SOUND_SPECTRUM_ENTRY, dataStream -> {
            final double duration = spectrum.getBasicTime().getDuration();
            final int length = spectrum.getLength();
            dataStream.writeDouble(duration);
            dataStream.writeInt(length);
            for (int i = 0; i < length; ++i) {
                Complex value = spectrum.getValue(i);
                dataStream.writeDouble(value.getRe());
                dataStream.writeDouble(value.getIm());
            }
        });
    }
    public static Spectrum1d toSoundSpectrum(byte[] data) throws IOException {
        return readSingle(data, SOUND_SPECTRUM_ENTRY, dataStream -> {
            final double duration = dataStream.readDouble();
            final int length = dataStream.readInt();
            final Complex[] values = new Complex[length];
            for (int i = 0; i < length; ++i) {
                final double re = dataStream.readDouble();
                final double im = dataStream.readDouble();
                values[i] = Complex.ofReIm(re, im);
            }
            return new BufferSpectrum1d(values, TimeRange.ofDuration(duration));
        });
    }

    private static final String SIGNAL_ENTRY = "signal";

    public static byte[] fromSignal(Signal signal) throws IOException {
        return writeSingle(SIGNAL_ENTRY, dataStream -> {
            final double duration = signal.getSamplingTime().getDuration();
            final int length = signal.getLength();
            dataStream.writeDouble(duration);
            dataStream.writeInt(length);
            for (int i = 0; i < length; ++i) {
                dataStream.writeDouble(signal.getValue(i));
            }
        });
    }

    public static Signal toSignal(byte[] data) throws IOException {
        return readSingle(data, SIGNAL_ENTRY, dataStream -> {
            final double duration = dataStream.readDouble();
            final int length = dataStream.readInt();
            final double[] values = new double[length];
            for (int i = 0; i < length; ++i) {
                values[i] = dataStream.readDouble();
            }
            return new BufferSignal(values, TimeRange.ofDuration(duration));
        });
    }

    private static final String SOUND_FILTER_ENTRY = "sound_filter";

    public static byte[] fromFilter(Filter filter) throws IOException {
        return writeSingle(SOUND_FILTER_ENTRY, dataStream -> {
            final int length = filter.getLength();
            final int negativeLength = filter.getNegativeLength();
            final double duration = filter.getSamplingTime().getDuration();
            dataStream.writeInt(length);
            dataStream.writeInt(negativeLength);
            dataStream.writeDouble(duration);
            for (int i = -negativeLength; i < filter.getPositiveLength(); ++i) {
                dataStream.writeDouble(filter.getValue(i));
            }
        });
    }

    public static Filter toFilter(byte[] data) throws IOException {
        return readSingle(data, SOUND_FILTER_ENTRY, dataStream -> {
            final int length = dataStream.readInt();
            final int negativeLength = dataStream.readInt();
            final double duration = dataStream.readDouble();
            final double[] values = new double[length];
            for (int i = 0; i < length; ++i) {
                values[i] = dataStream.readDouble();
            }
            return new BufferFilter(
                    values, negativeLength, TimeRange.ofDuration(duration)
            );
        });
    }


    private static final String NOTES_ENTRY = "notes";

    public static byte[] fromNotes(Note[] notes) throws IOException {
        return writeSingle(NOTES_ENTRY, dataStream -> {
            final int arrayLength = notes.length;
            dataStream.writeInt(arrayLength);

            for (Note note : notes) {
                final boolean isPresent = note.getPitch().isPresent();
                dataStream.writeBoolean(isPresent);
                dataStream.writeInt(note.getLength());
                dataStream.writeDouble(note.getSamplingTime().getFrequency());
                if (isPresent) {
                    dataStream.writeDouble(note.getPitch().getAsDouble());
                    dataStream.writeInt(note.getAmplitudeStart().getAsInt());
                    dataStream.writeInt(note.getAmplitudeEnd().getAsInt());
                }
            }
        });
    }

    public static Note[] toNotes(byte[] data) throws IOException {
        return readSingle(data, NOTES_ENTRY, dataStream -> {
            final int arrayLength = dataStream.readInt();
            final Note[] notes = new Note[arrayLength];

            for (int i = 0; i < arrayLength; ++i) {
                final boolean isPresent = dataStream.readBoolean();
                final int length = dataStream.readInt();
                final TimeRange time = TimeRange.ofFrequency(dataStream.readDouble());
                if (isPresent) {
                    final double pitch = dataStream.readDouble();
                    final int amplitudeStart = dataStream.readInt();
                    final int amplitudeEnd = dataStream.readInt();
                    notes[i] = Note.of(pitch, amplitudeStart, amplitudeEnd, length, time);
                } else {
                    notes[i] = Note.unknown(length, time);
                }
            }

            return notes;
        });
    }

    private static final String DOUBLE_ARRAY_ENTRY = "double_array";

    public static byte[] fromDoubleArray(double[][] array) throws IOException {
        return writeSingle(DOUBLE_ARRAY_ENTRY, dataStream -> {
            final int height = array.length;
            dataStream.writeInt(height);
            if (height == 0) {
                return;
            }

            final int width = array[0].length;
            dataStream.writeInt(width);
            for (int y = 0; y < height; ++y) {
                for (int x = 0; x < width; ++x) {
                    dataStream.writeDouble(array[y][x]);
                }
            }
        });
    }

    public static double[][] toDoubleArray(byte[] data)
            throws IOException {
        return readSingle(data, DOUBLE_ARRAY_ENTRY, dataStream -> {
            final int height = dataStream.readInt();
            if (height == 0) {
                return new double[0][0];
            }

            final int width = dataStream.readInt();
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
        final ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        try (ZipOutputStream zipStream = new ZipOutputStream(byteStream)) {

            for (String entryName : consumers.keySet()) {
                final ZipEntry singleEntry = new ZipEntry(entryName);
                zipStream.putNextEntry(singleEntry);

                final DataOutputStream dataStream = new DataOutputStream(zipStream);
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
            byte[] data, String entryName, InputStreamConsumer<T> consumer
    ) throws IOException {
        return readMultiple(
                data, Collections.singletonMap(entryName, consumer)
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
        final Map<String, T> resultMap = new HashMap<>();

        try (ByteArrayInputStream byteStream = new ByteArrayInputStream(bytes);
             ZipInputStream zipStream = new ZipInputStream(byteStream)) {

            ZipEntry nextEntry = zipStream.getNextEntry();
            while (nextEntry != null) {
                final InputStreamConsumer<T> consumer
                        = consumers.get(nextEntry.getName());
                if (consumer != null) {
                    final DataInputStream dataStream = new DataInputStream(zipStream);
                    final T result = consumer.read(dataStream);
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
