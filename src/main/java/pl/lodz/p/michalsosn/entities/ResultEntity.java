package pl.lodz.p.michalsosn.entities;

import pl.lodz.p.michalsosn.domain.image.spectrum.ImageSpectrum;
import pl.lodz.p.michalsosn.domain.image.transform.segmentation.Mask;
import pl.lodz.p.michalsosn.domain.sound.signal.Signal;
import pl.lodz.p.michalsosn.domain.sound.sound.Sound;
import pl.lodz.p.michalsosn.domain.sound.spectrum.Spectrum1d;
import pl.lodz.p.michalsosn.domain.sound.transform.Note;
import pl.lodz.p.michalsosn.domain.util.ArrayUtils;
import pl.lodz.p.michalsosn.io.BufferedImageIO;
import pl.lodz.p.michalsosn.io.CompressionIO;

import javax.persistence.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.Serializable;

import static javax.persistence.DiscriminatorType.STRING;
import static pl.lodz.p.michalsosn.io.BufferedImageIO.fromByteArray;

/**
 * @author Michał Sośnicki
 */
@Entity(name = "Result")
@Table(name = "result",
       indexes = {@Index(columnList = "operation_id")})
@SequenceGenerator(name = "result_sequence",
                   sequenceName = "result_sequence",
                   allocationSize = 1)
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "type", discriminatorType = STRING)
public abstract class ResultEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE,
                    generator = "result_sequence")
    @Column(name = "result_id", nullable = false, updatable = false)
    private long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false,
            insertable = false, updatable = false, length = 32)
    private ResultType type;

    @Column(name = "role", nullable = false, updatable = false, length = 32)
    private String role;

    @ManyToOne(optional = false)
    @JoinColumn(name = "operation_id", referencedColumnName = "operation_id",
                nullable = false)
    private OperationEntity operation;

    public ResultEntity() {
    }

    public long getId() {
        return id;
    }

    public ResultType getType() {
        return type;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public OperationEntity getOperation() {
        return operation;
    }

    public void setOperation(OperationEntity operation) {
        this.operation = operation;
    }

    @Override
    public String toString() {
        return "ResultEntity{"
             + "id=" + id
             + '}';
    }

    @Entity(name = "NoneResult")
    @DiscriminatorValue("NONE")
    public static class NoneResultEntity extends ResultEntity {

        public NoneResultEntity() {
            super();
        }

        @Override
        public String toString() {
            return "NoneResultEntity{} " + super.toString();
        }
    }

    @Entity(name = "IntegerResult")
    @DiscriminatorValue("INTEGER")
    public static class IntegerResultEntity extends ResultEntity {

        @Column(name = "integer_value")
        private Integer value;

        public IntegerResultEntity() {
        }

        public IntegerResultEntity(Integer value) {
            this.value = value;
        }

        public Integer getValue() {
            return value;
        }

        public void setValue(Integer value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return "IntegerResultEntity{"
                  + "value=" + value
                  + "} " + super.toString();
        }
    }

    @Entity(name = "DoubleResult")
    @DiscriminatorValue("DOUBLE")
    public static class DoubleResultEntity extends ResultEntity {

        @Column(name = "double_value")
        private Double value;

        public DoubleResultEntity() {
        }

        public DoubleResultEntity(Double value) {
            this.value = value;
        }

        public Double getValue() {
            return value;
        }

        public void setValue(Double value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return "DoubleResultEntity{"
                 + "value=" + value
                 + "} " + super.toString();
        }
    }

    @Entity(name = "ImageResult")
    @DiscriminatorValue("IMAGE")
    public static class ImageResultEntity extends ResultEntity {

        @Lob
        @Column(name = "data")
        @Basic(fetch = FetchType.LAZY)
        private byte[] data;

        public ImageResultEntity() {
        }

        public ImageResultEntity(BufferedImage image) throws IOException {
            setImage(image);
        }

        public BufferedImage getImage() throws IOException {
            return fromByteArray(data);
        }

        public void setImage(BufferedImage image) throws IOException {
            data = BufferedImageIO.toByteArray(image);
        }

        public byte[] getData() {
            return data;
        }

        public void setData(byte[] data) {
            this.data = data;
        }

        @Override
        public String toString() {
            return "ImageResultEntity{"
                    + "data=" + ArrayUtils.limitedToString(data, 10)
                    + "} " + super.toString();
        }
    }

    @Entity(name = "ImageHistogramResult")
    @DiscriminatorValue("IMAGE_HISTOGRAM")
    public static class ImageHistogramResultEntity extends ResultEntity {

        @Column(name = "image_histogram")
        private int[] histogram;

        public ImageHistogramResultEntity() {
        }

        public ImageHistogramResultEntity(int[] histogram) {
            this.histogram = histogram;
        }

        public int[] getHistogram() {
            return histogram;
        }

        public void setHistogram(int[] histogram) {
            this.histogram = histogram;
        }

        @Override
        public String toString() {
            return "HistogramResultEntity{} " + super.toString();
        }
    }

    @Entity(name = "ImageSpectrumResult")
    @DiscriminatorValue("IMAGE_SPECTRUM")
    public static class ImageSpectrumResultEntity extends ResultEntity {

        @Lob
        @Column(name = "data")
        @Basic(fetch = FetchType.LAZY)
        private byte[] data;

        @Lob
        @Column(name = "data_presentation")
        @Basic(fetch = FetchType.LAZY)
        private byte[] presentationData;

        public ImageSpectrumResultEntity() {
        }

        public ImageSpectrumResultEntity(ImageSpectrum imageSpectrum,
                                         BufferedImage presentationImage)
                throws IOException {
            setImageSpectrum(imageSpectrum);
            setPresentationImage(presentationImage);
        }

        public ImageSpectrum getImageSpectrum() throws IOException {
            return CompressionIO.toImageSpectrum(data);
        }

        public void setImageSpectrum(ImageSpectrum imageSpectrum)
                throws IOException {
            data = CompressionIO.fromImageSpectrum(imageSpectrum);
        }

        public byte[] getData() {
            return data;
        }

        public void setData(byte[] data) {
            this.data = data;
        }

        public BufferedImage getPresentationImage() throws IOException {
            return fromByteArray(presentationData);
        }

        public void setPresentationImage(BufferedImage image)
                throws IOException {
            presentationData = BufferedImageIO.toByteArray(image);
        }

        public byte[] getPresentationData() {
            return presentationData;
        }

        public void setPresentationData(byte[] presentationData) {
            this.presentationData = presentationData;
        }

        @Override
        public String toString() {
            return "ImageSpectrumResultEntity{"
                    + "data=" + ArrayUtils.limitedToString(data, 10)
                    + ", dataPresentation="
                    + ArrayUtils.limitedToString(presentationData, 10)
                    + "} " + super.toString();
        }
    }

    @Entity(name = "ImageMaskResult")
    @DiscriminatorValue("IMAGE_MASK")
    public static class ImageMaskResultEntity extends ResultEntity {

        @Column(name = "image_mask")
        private boolean[][] data;

        public ImageMaskResultEntity() {
        }

        public ImageMaskResultEntity(boolean[][] data) {
            this.data = data;
        }

        public ImageMaskResultEntity(Mask mask) {
            setMask(mask);
        }

        public boolean[][] getData() {
            return data;
        }

        public void setData(boolean[][] data) {
            this.data = data;
        }

        public Mask getMask() {
            return new Mask(data);
        }

        public void setMask(Mask mask) {
            data = mask.copyMask();
        }

        @Override
        public String toString() {
            return "ImageMaskResultEntity{} " + super.toString();
        }
    }

    @Entity(name = "SoundResult")
    @DiscriminatorValue("SOUND")
    public static class SoundResultEntity extends ResultEntity {

        @Lob
        @Column(name = "data")
        @Basic(fetch = FetchType.LAZY)
        private byte[] data;

        public SoundResultEntity() {
        }

        public SoundResultEntity(Sound sound) throws IOException {
            setSound(sound);
        }

        public Sound getSound() throws IOException {
            return CompressionIO.toSound(data);
        }

        public void setSound(Sound sound) throws IOException {
            data = CompressionIO.fromSound(sound);
        }

        public byte[] getData() {
            return data;
        }

        public void setData(byte[] data) {
            this.data = data;
        }

        @Override
        public String toString() {
            return "SoundResultEntity{"
                 + "data=" + ArrayUtils.limitedToString(data, 10)
                 + "} " + super.toString();
        }
    }

    @Entity(name = "SoundSpectrumResult")
    @DiscriminatorValue("SOUND_SPECTRUM")
    public static class SoundSpectrumResultEntity extends ResultEntity {

        @Lob
        @Column(name = "data")
        @Basic(fetch = FetchType.LAZY)
        private byte[] data;

        public SoundSpectrumResultEntity() {
        }

        public SoundSpectrumResultEntity(Spectrum1d spectrum) throws IOException {
            setSpectrum(spectrum);
        }

        public Spectrum1d getSpectrum() throws IOException {
            return CompressionIO.toSoundSpectrum(data);
        }

        public void setSpectrum(Spectrum1d spectrum) throws IOException {
            data = CompressionIO.fromSoundSpectrum(spectrum);
        }

        public byte[] getData() {
            return data;
        }

        public void setData(byte[] data) {
            this.data = data;
        }

        @Override
        public String toString() {
            return "SoundSpectrumResultEntity{"
                  + "data=" + ArrayUtils.limitedToString(data, 10)
                  + "} " + super.toString();
        }
    }

    @Entity(name = "SignalResult")
    @DiscriminatorValue("SIGNAL")
    public static class SignalResultEntity extends ResultEntity {

        @Lob
        @Column(name = "data")
        @Basic(fetch = FetchType.LAZY)
        private byte[] data;

        public SignalResultEntity() {
        }

        public SignalResultEntity(Signal signal) throws IOException {
            setSignal(signal);
        }

        public Signal getSignal() throws IOException {
            return CompressionIO.toSignal(data);
        }

        public void setSignal(Signal signal) throws IOException {
            data = CompressionIO.fromSignal(signal);
        }

        public byte[] getData() {
            return data;
        }

        public void setData(byte[] data) {
            this.data = data;
        }

        @Override
        public String toString() {
            return "SignalResultEntity{"
                 + "data=" + ArrayUtils.limitedToString(data, 10)
                 + "} " + super.toString();
        }
    }

    @Entity(name = "NoteSequenceResult")
    @DiscriminatorValue("NOTE_SEQUENCE")
    public static class NoteSequenceResultEntity extends ResultEntity {

        @Lob
        @Column(name = "data")
        @Basic(fetch = FetchType.LAZY)
        private byte[] data;

        public NoteSequenceResultEntity() {
        }

        public NoteSequenceResultEntity(Note[] notes) throws IOException {
            setNotes(notes);
        }

        public Note[] getNotes() throws IOException {
            return CompressionIO.toNotes(data);
        }

        public void setNotes(Note[] notes) throws IOException {
            data = CompressionIO.fromNotes(notes);
        }

        public byte[] getData() {
            return data;
        }

        public void setData(byte[] data) {
            this.data = data;
        }

        @Override
        public String toString() {
            return "NoteSequenceResultEntity{"
                    + "data=" + ArrayUtils.limitedToString(data, 10)
                    + "} " + super.toString();
        }
    }

}
