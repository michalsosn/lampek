package pl.lodz.p.michalsosn.entities;

import javax.persistence.*;
import java.io.Serializable;

import static javax.persistence.DiscriminatorType.STRING;

/**
 * @author Michał Sośnicki
 */
@Entity(name = "Argument")
@Table(name = "argument",
       indexes = {@Index(columnList = "operation_id")})
@SequenceGenerator(name = "argument_sequence",
                   sequenceName = "argument_sequence",
                   allocationSize = 2)
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "type", discriminatorType = STRING)
public abstract class ArgumentEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE,
                    generator = "argument_sequence")
    @Column(name = "argument_id", nullable = false, updatable = false)
    private long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false,
            insertable = false, updatable = false, length = 32)
    private ValueType type;

    @Column(name = "role", nullable = false, updatable = false, length = 32)
    private String role;

    @ManyToOne(optional = false)
    @JoinColumn(name = "operation_id", referencedColumnName = "operation_id",
                nullable = false)
    private OperationEntity operation;

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

    ArgumentEntity() {
    }

    @Entity(name = "ImageArgument")
    @DiscriminatorValue("IMAGE")
    public static class ImageArgumentEntity extends ArgumentEntity {

        @ManyToOne
        @JoinColumn(name = "image_id", referencedColumnName = "image_id")
        private ImageEntity image;

        ImageArgumentEntity() {
        }

        public ImageArgumentEntity(ImageEntity image) {
            this.image = image;
        }

        public ImageEntity getImage() {
            return image;
        }

        public void setImage(ImageEntity image) {
            this.image = image;
        }
    }

    @Entity(name = "IntegerArgument")
    @DiscriminatorValue("INTEGER")
    public static class IntegerArgumentEntity extends ArgumentEntity {

        @Column(name = "integer_value")
        private Integer value;

        IntegerArgumentEntity() {
        }

        public IntegerArgumentEntity(Integer value) {
            this.value = value;
        }

        public Integer getValue() {
            return value;
        }

        public void setValue(Integer value) {
            this.value = value;
        }
    }

    @Entity(name = "DoubleArgument")
    @DiscriminatorValue("DOUBLE")
    public static class DoubleArgumentEntity extends ArgumentEntity {

        @Column(name = "double_value")
        private Double value;

        DoubleArgumentEntity() {
        }

        public DoubleArgumentEntity(Double value) {
            this.value = value;
        }

        public Double getValue() {
            return value;
        }

        public void setValue(Double value) {
            this.value = value;
        }
    }

    @Entity(name = "MatrixArgument")
    @DiscriminatorValue("MATRIX")
    public static class MatrixArgumentEntity extends ArgumentEntity {

        @Column(name = "matrix")
        private double[][] matrix;

        MatrixArgumentEntity() {
        }

        public MatrixArgumentEntity(double[][] matrix) {
            this.matrix = matrix;
        }

        public double[][] getMatrix() {
            return matrix;
        }

        public void setMatrix(double[][] matrix) {
            this.matrix = matrix;
        }
    }


    @Entity(name = "BooleanArgument")
    @DiscriminatorValue("BOOLEAN")
    public static class BooleanArgumentEntity extends ArgumentEntity {

        @Column(name = "boolean_value")
        private Boolean value;

        BooleanArgumentEntity() {
        }

        public BooleanArgumentEntity(Boolean value) {
            this.value = value;
        }

        public Boolean getValue() {
            return value;
        }

        public void setValue(Boolean value) {
            this.value = value;
        }
    }
}
