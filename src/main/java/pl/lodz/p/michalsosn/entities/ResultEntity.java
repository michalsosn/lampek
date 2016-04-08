package pl.lodz.p.michalsosn.entities;

import pl.lodz.p.michalsosn.domain.image.spectrum.ImageSpectrum;
import pl.lodz.p.michalsosn.io.BufferedImageIO;
import pl.lodz.p.michalsosn.io.CompressionIO;

import javax.persistence.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.Serializable;

import static javax.persistence.DiscriminatorType.INTEGER;
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
@DiscriminatorColumn(name = "type", discriminatorType = INTEGER)
public abstract class ResultEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE,
                    generator = "result_sequence")
    @Column(name = "result_id", nullable = false, updatable = false)
    private long id;

    @Enumerated(EnumType.ORDINAL)
    @Column(name = "type", nullable = false,
            insertable = false, updatable = false)
    private ValueType type;

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

    public ValueType getType() {
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
    @DiscriminatorValue("0")
    public static class NoneResultEntity extends ResultEntity {

        public NoneResultEntity() {
            super();
        }

        @Override
        public String toString() {
            return "NoneResultEntity{} " + super.toString();
        }
    }

    @Entity(name = "ImageResult")
    @DiscriminatorValue("1")
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
                 + "data=" + data
                 + "} " + super.toString();
        }
    }

    @Entity(name = "DoubleResult")
    @DiscriminatorValue("3")
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

    @Entity(name = "HistogramResult")
    @DiscriminatorValue("5")
    public static class HistogramResultEntity extends ResultEntity {

        @Column(name = "histogram")
        private int[] histogram;

        public HistogramResultEntity() {
        }

        public HistogramResultEntity(int[] histogram) {
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
    @DiscriminatorValue("7")
    public static class ImageSpectrumResultEntity extends ResultEntity {

        @Lob
        @Column(name = "data")
        @Basic(fetch = FetchType.LAZY)
        private byte[] data;

        public ImageSpectrumResultEntity() {
        }

        public ImageSpectrumResultEntity(ImageSpectrum imageSpectrum)
                throws IOException {
            setImageSpectrum(imageSpectrum);
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

        @Override
        public String toString() {
            return "ImageSpectrumResultEntity{"
                    + "data=" + data
                    + "} " + super.toString();
        }
    }

}
