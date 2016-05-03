package pl.lodz.p.michalsosn.io;

import net.sourceforge.javaflacencoder.FLACFileWriter;
import pl.lodz.p.michalsosn.domain.sound.TimeRange;
import pl.lodz.p.michalsosn.domain.sound.sound.BufferSound;
import pl.lodz.p.michalsosn.domain.sound.sound.Sound;

import javax.sound.sampled.*;
import java.io.*;
import java.nio.file.Path;

import static pl.lodz.p.michalsosn.domain.sound.sound.Sound.MID_VALUE;

/**
 * @author Michał Sośnicki
 */
public final class SoundIO {

    public enum AudioType {
        WAVE(AudioFileFormat.Type.WAVE, "wav"), FLAC(FLACFileWriter.FLAC, "flac");

        private final AudioFileFormat.Type fileType;
        private final String extension;

        AudioType(AudioFileFormat.Type fileType, String extension) {
            this.fileType = fileType;
            this.extension = extension;
        }

        public AudioFileFormat.Type getFileType() {
            return fileType;
        }

        public String getExtension() {
            return extension;
        }

        public static AudioType fromExtension(String extension) {
            for (AudioType audioType : AudioType.values()) {
                if (audioType.extension.equalsIgnoreCase(extension)) {
                    return audioType;
                }
            }
            throw new IllegalArgumentException("Extension " + extension + " not found.");
        }
    }

    private static final int SAMPLE_BYTES = 2;
    private static final int SAMPLE_BITS = 8 * SAMPLE_BYTES;
    private static final AudioType DEFAULT_TYPE = AudioType.WAVE;

    private SoundIO() {
    }

    public static Sound readSound(byte[] soundData) throws IOException {
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(soundData)) {
            return readSound(inputStream);
        }
    }

    public static Sound readSound(InputStream inputStream) throws IOException {
        try (AudioInputStream audioStream
                     = AudioSystem.getAudioInputStream(inputStream)) {
            return readSound(audioStream);
        } catch (UnsupportedAudioFileException ex) {
            throw new IOException(ex);
        }
    }

    public static Sound readSound(Path path) throws IOException {
        File file = path.toFile();
        try (AudioInputStream audioStream
                     = AudioSystem.getAudioInputStream(file)) {
            return readSound(audioStream);
        } catch (UnsupportedAudioFileException ex) {
            throw new IOException(ex);
        }
    }

    public static Sound readSound(AudioInputStream audioStream) throws IOException {
        AudioFormat sourceFormat = audioStream.getFormat();
        double sampleRate = sourceFormat.getSampleRate();
        AudioFormat targetFormat = makeDefaultFormat(sampleRate);
        try (AudioInputStream convertedStream
                     = AudioSystem.getAudioInputStream(targetFormat, audioStream)) {
            AudioFormat convertedFormat = convertedStream.getFormat();
            int frameSize = convertedFormat.getFrameSize();
            int frameLength = limitedFrameLength(audioStream);
            double frameRate = convertedFormat.getFrameRate();

            int levels = 1 << frameSize * Byte.SIZE;
            if (levels != Sound.MAX_VALUE - Sound.MIN_VALUE + 1) {
                throw new IOException("Unsupported number of levels " + levels);
            }

            int[] readSamples = new int[frameLength];
            byte[] singleFrame = new byte[frameSize];
            for (int i = 0; i < frameLength; ++i) {
                if (convertedStream.read(singleFrame) != frameSize) {
                    throw new IOException(String.format(
                            "Could not read a full frame of size %d "
                          + "when reading the %dth frame", frameSize, i
                    ));
                }
                int nextSample = MID_VALUE;
                nextSample += singleFrame[0] & 0xff;
                nextSample += singleFrame[1] << Byte.SIZE;
                readSamples[i] = nextSample;
            }

            return new BufferSound(readSamples, TimeRange.ofFrequency(frameRate));
        }
    }

    public static void writeSound(Sound sound, Path path) throws IOException {
        String extension = IOUtils.separateExtension(path);
        AudioType type = AudioType.fromExtension(extension);
        writeSound(sound, path, type);
    }

    public static void writeSound(Sound sound, Path path, AudioType type)
            throws IOException {
        File file = path.toFile();
        AudioInputStream audioStream = writeSoundStream(sound);
        AudioFileFormat.Type fileType = type.getFileType();
        AudioSystem.write(audioStream, fileType, file);
    }

    public static void writeSound(Sound sound, OutputStream outputStream,
                                  AudioType type) throws IOException {
        AudioInputStream audioStream = writeSoundStream(sound);
        AudioFileFormat.Type fileType = type.getFileType();
        AudioSystem.write(audioStream, fileType, outputStream);
    }

    public static byte[] writeSound(Sound sound, AudioType type) throws IOException {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            writeSound(sound, outputStream, type);
            return outputStream.toByteArray();
        }
    }

    public static void writeSound(Sound sound, OutputStream outputStream)
            throws IOException {
        writeSound(sound, outputStream, DEFAULT_TYPE);
    }

    public static byte[] convertAudio(InputStream inputStream, AudioType type)
            throws IOException {
        try (BufferedInputStream bufferedStream = new BufferedInputStream(inputStream);
             AudioInputStream audioStream
                     = AudioSystem.getAudioInputStream(bufferedStream)) {
            AudioFormat sourceFormat = audioStream.getFormat();
            AudioFormat targetFormat = makeDefaultFormat(sourceFormat.getSampleRate());
            try (AudioInputStream convertedStream
                         = AudioSystem.getAudioInputStream(targetFormat, audioStream);
                 ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
                AudioSystem.write(convertedStream, type.getFileType(), outputStream);
                return outputStream.toByteArray();
            }
        } catch (UnsupportedAudioFileException ex) {
            throw new IOException(ex);
        }
    }

    public static byte[] convertAudio(InputStream inputStream) throws IOException {
        return convertAudio(inputStream, DEFAULT_TYPE);
    }

    public static byte[] convertAudio(byte[] input, AudioType type)
            throws IOException {
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(input)) {
            return convertAudio(inputStream, type);
        }
    }

    public static byte[] convertAudio(byte[] input)
            throws IOException {
        return convertAudio(input, DEFAULT_TYPE);
    }

    private static AudioInputStream writeSoundStream(Sound sound) {
        double sampleRate = sound.getSamplingTime().getFrequency();
        AudioFormat audioFormat = makeDefaultFormat(sampleRate);

        int length = sound.getLength();
        byte[] data = copyToByteBuffer(sound);
        ByteArrayInputStream stream = new ByteArrayInputStream(data);

        return new AudioInputStream(stream, audioFormat, length);
    }

    public static void play(Sound sound) throws LineUnavailableException {
        double sampleRate = sound.getSamplingTime().getFrequency();
        AudioFormat audioFormat = makeDefaultFormat(sampleRate);
        SourceDataLine dataLine = AudioSystem.getSourceDataLine(audioFormat);

        dataLine.open(audioFormat, (int) Math.ceil(sampleRate));
        dataLine.start();

        byte[] toneBuffer = copyToByteBuffer(sound);

        dataLine.write(toneBuffer, 0, toneBuffer.length);

        dataLine.drain();
        dataLine.close();
    }

    private static byte[] copyToByteBuffer(Sound sound) {
        int length = sound.getLength();
        byte[] data = new byte[2 * length];
        sound.stream().forEach(p -> {
            int value = sound.getValue(p) - MID_VALUE;
            data[2 * p] = (byte) (value & 0xff);
            data[2 * p + 1] = (byte) (value >> Byte.SIZE);
        });
        return data;
    }

    private static int limitedFrameLength(AudioInputStream audioStream) {
        return (int) Math.min(audioStream.getFrameLength(), Integer.MAX_VALUE);
    }

    private static AudioFormat makeDefaultFormat(double sampleRate) {
        return new AudioFormat((float) sampleRate, SAMPLE_BITS, 1, true, false);
    }

}
