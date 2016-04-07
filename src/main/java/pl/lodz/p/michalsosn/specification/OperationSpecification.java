package pl.lodz.p.michalsosn.specification;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.context.ApplicationContext;
import pl.lodz.p.michalsosn.entities.OperationEntity;
import pl.lodz.p.michalsosn.entities.ProcessEntity;
import pl.lodz.p.michalsosn.entities.ValueType;

import java.util.*;
import java.util.function.Consumer;

import static pl.lodz.p.michalsosn.domain.image.image.Image.MAX_VALUE;
import static pl.lodz.p.michalsosn.domain.image.image.Image.MIN_VALUE;

/**
 * @author Michał Sośnicki
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum OperationSpecification {
    IMAGE_ROOT(OperationRequest.ImageRootRequest.class, self ->
        self.withDescription("Load image")
        .inCategory("Image")
        .withImageParam("imageEntity", "image")),
    NEGATE(OperationRequest.NegateRequest.class, self ->
        self.acceptingTypes(ValueType.IMAGE)
        .inCategory("Image")),
    CHANGE_BRIGHTNESS(OperationRequest.ChangeBrightnessRequest.class, self ->
        self.acceptingTypes(ValueType.IMAGE)
        .inCategory("Image")
        .withIntegerParam("brightness change", "change",
                          -MAX_VALUE, MAX_VALUE)),
    CHANGE_CONTRAST(OperationRequest.ChangeContrastRequest.class, self ->
        self.acceptingTypes(ValueType.IMAGE)
        .inCategory("Image")
        .withDoubleParam("contrast change", "change", 0.0, 128.0)),
    CLIP_BELOW(OperationRequest.ClipBelowRequest.class, self ->
        self.acceptingTypes(ValueType.IMAGE)
        .inCategory("Image")
        .withIntegerParam("clipping threshold", "threshold",
                          MIN_VALUE, MAX_VALUE)),
    ARITHMETIC_MEAN_FILTER(OperationRequest.ArithmeticMeanFilterRequest.class,
        self -> self.acceptingTypes(ValueType.IMAGE)
        .inCategory("Image")
        .withIntegerParam("range", 0, 30)
        .withBooleanParam("use sliding window algorithm", "runningWindow")),
    MEDIAN_FILTER(OperationRequest.MedianFilterRequest.class, self ->
        self.acceptingTypes(ValueType.IMAGE)
        .inCategory("Image")
        .withIntegerParam("range", 0, 30)
        .withBooleanParam("use sliding window algorithm", "runningWindow")),
    UNIFORM_DENSITY(OperationRequest.UniformDensityRequest.class, self ->
        self.acceptingTypes(ValueType.IMAGE)
        .inCategory("Image")
        .withIntegerParam("lower bound", "minValue", MIN_VALUE, MAX_VALUE)
        .withIntegerParam("upper bound", "maxValue", MIN_VALUE, MAX_VALUE)),
    HYPERBOLIC_DENSITY(OperationRequest.HyperbolicDensityRequest.class, self ->
        self.acceptingTypes(ValueType.IMAGE)
        .inCategory("Image")
        .withIntegerParam("lower bound", "minValue", MIN_VALUE + 1, MAX_VALUE)
        .withIntegerParam("upper bound", "maxValue", MIN_VALUE + 1, MAX_VALUE)),
    VALUE_HISTOGRAM(OperationRequest.ValueHistogramRequest.class, self ->
        self.acceptingTypes(ValueType.IMAGE)
        .inCategory("Image")
        .withBooleanParam("running total", "runningTotal")),
    CONVOLUTION(OperationRequest.ConvolutionRequest.class, self ->
        self.acceptingTypes(ValueType.IMAGE)
        .inCategory("Image")
        .withMatrixParam("kernel")
        .withBooleanParam("keep size", "keepSize")),
    KIRSCH_OPERATOR(OperationRequest.KirschOperatorRequest.class, self ->
        self.acceptingTypes(ValueType.IMAGE)
        .inCategory("Image")),
    ERROR_MEASUREMENT(OperationRequest.ErrorMeasurementRequest.class, self ->
        self.acceptingTypes(ValueType.IMAGE)
        .inCategory("Image")
        .withImageParam("expected image", "imageEntity", "image")),
    TO_GRAYSCALE_CONVERSION(OperationRequest.ToGrayscaleConversionRequest.class,
        self -> self.acceptingTypes(ValueType.IMAGE)
        .inCategory("Image")),
    COLOR_EXTRACTION(OperationRequest.ColorExtractionRequest.class, self ->
        self.acceptingTypes(ValueType.IMAGE)
        .inCategory("Image"));

    private final Class requestClass;
    private final List<ValueType> lastResult = new ArrayList<>();
    private final Map<String, ParameterSpecification> parameters
            = new HashMap<>();
    private String description;
    private String category;

    OperationSpecification(
            Class requestClass, Consumer<OperationSpecification> configurer
    ) {
        this.requestClass = requestClass;
        configurer.accept(this);
    }

    private OperationSpecification acceptingTypes(ValueType... types) {
        lastResult.clear();
        lastResult.addAll(Arrays.asList(types));
        return this;
    }

    private OperationSpecification withDescription(String description) {
        this.description = description;
        return this;
    }

    private OperationSpecification inCategory(String category) {
        this.category = category;
        return this;
    }

    private OperationSpecification withImageParam(
            String description, String imageField, String nameField
    ) {
        try {
            parameters.put(
                    nameField, new ImageParameterSpecification(
                            description, requestClass, imageField, nameField
                    )
            );
            return this;
        } catch (NoSuchFieldException e) {
            throw new IllegalStateException("Configuration failure.", e);
        }
    }

    private OperationSpecification withImageParam(
            String imageField, String nameField
    ) {
        return withImageParam(null, imageField, nameField);
    }

    private OperationSpecification withIntegerParam(
            String description, String name, int min, int max
    ) {
        try {
            parameters.put(name, new IntegerParameterSpecification(
                    description, requestClass, name, min, max
            ));
            return this;
        } catch (NoSuchFieldException e) {
            throw new IllegalStateException("Configuration failure.", e);
        }
    }

    private OperationSpecification withIntegerParam(
            String name, int min, int max
    ) {
        return withIntegerParam(null, name, min, max);
    }

    private OperationSpecification withDoubleParam(
            String description, String name, double min, double max
    ) {
        try {
            parameters.put(name, new DoubleParameterSpecification(
                    description, requestClass, name, min, max
            ));
            return this;
        } catch (NoSuchFieldException e) {
            throw new IllegalStateException("Configuration failure.", e);
        }
    }

    private OperationSpecification withDoubleParam(
            String name, double min, double max
    ) {
        return withDoubleParam(null, name, min, max);
    }

    private OperationSpecification withMatrixParam(
            String description, String name
    ) {
        try {
            parameters.put(name, new MatrixParameterSpecification(
                    description, requestClass, name
            ));
            return this;
        } catch (NoSuchFieldException e) {
            throw new IllegalStateException("Configuration failure.", e);
        }
    }

    private OperationSpecification withMatrixParam(String name) {
        return withMatrixParam(null, name);
    }

    private OperationSpecification withBooleanParam(
            String description, String name
    ) {
        try {
            parameters.put(name, new BooleanParameterSpecification(
                    description, requestClass, name
            ));
            return this;
        } catch (NoSuchFieldException e) {
            throw new IllegalStateException("Configuration failure.", e);
        }
    }

    private OperationSpecification withBooleanParam(String name) {
        return withBooleanParam(null, name);
    }

    public List<ValueType> getLastResult() {
        return lastResult;
    }

    public Map<String, ParameterSpecification> getParameters() {
        return parameters;
    }

    public String getType() {
        return name();
    }

    public String getDescription() {
        return description;
    }

    public String getCategory() {
        return category;
    }

    public OperationEntity entitize(OperationRequest request,
                                    ProcessEntity process,
                                    ApplicationContext context,
                                    String username) {
        if (!lastResult.isEmpty() && request.getLastResult() == null) {
            throw new IllegalArgumentException("No last result in request");
        }

        OperationEntity operationEntity = new OperationEntity(
                this, request.getLastResult(), process
        );
        process.getOperations().add(operationEntity);

        parameters.forEach((key, parameterSpec) ->
            operationEntity.getArguments().put(key,
                parameterSpec.createArgumentEntity(request, context, username)
            )
        );

        operationEntity.getArguments().forEach((role, argumentEntity) -> {
            argumentEntity.setOperation(operationEntity);
            argumentEntity.setRole(role);
        });

        return operationEntity;
    }

    public OperationRequest deentitize(OperationEntity operationEntity) {
        OperationRequest request;
        try {
            Object requestObject = requestClass.getConstructor().newInstance();
            request = (OperationRequest) requestObject;
        } catch (Exception e) {
            throw new IllegalStateException("Can't dentitize", e);
        }

        request.setLastResult(operationEntity.getPreviousResult());

        parameters.forEach((role, parameterSpec) ->
            parameterSpec.applyArgumentEntity(request,
                    operationEntity.getArguments().get(role)
            )
        );

        return request;
    }
}
