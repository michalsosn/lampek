package pl.lodz.p.michalsosn.entities.specification;

import com.fasterxml.jackson.annotation.JsonFormat;
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
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum OperationSpecification {
    START(OperationRequest.StartRequest.class, self ->
        self.withImageParam("imageEntity", "image")),
    NEGATE(OperationRequest.NegateRequest.class, self ->
        self.acceptingTypes(ValueType.IMAGE)),
    CHANGE_BRIGHTNESS(OperationRequest.ChangeBrightnessRequest.class, self ->
        self.acceptingTypes(ValueType.IMAGE)
        .withIntegerParam("change", -MAX_VALUE, MAX_VALUE)),
    CHANGE_CONTRAST(OperationRequest.ChangeContrastRequest.class, self ->
        self.acceptingTypes(ValueType.IMAGE)
        .withDoubleParam("change", 0.0, 128.0)),
    CLIP_BELOW(OperationRequest.ClipBelowRequest.class, self ->
        self.acceptingTypes(ValueType.IMAGE)
        .withIntegerParam("threshold", MIN_VALUE, MAX_VALUE)),
    ARITHMETIC_MEAN_FILTER(OperationRequest.ArithmeticMeanFilterRequest.class,
        self -> self.acceptingTypes(ValueType.IMAGE)
        .withIntegerParam("range", 0, 30)),
    MEDIAN_FILTER(OperationRequest.MedianFilterRequest.class, self ->
        self.acceptingTypes(ValueType.IMAGE)
        .withIntegerParam("range", 0, 30)),
    UNIFORM_DENSITY(OperationRequest.UniformDensityRequest.class, self ->
        self.acceptingTypes(ValueType.IMAGE)
        .withIntegerParam("minValue", MIN_VALUE, MAX_VALUE)
        .withIntegerParam("maxValue", MIN_VALUE, MAX_VALUE)),
    HYPERBOLIC_DENSITY(OperationRequest.HyperbolicDensityRequest.class, self ->
        self.acceptingTypes(ValueType.IMAGE)
        .withIntegerParam("minValue", MIN_VALUE, MAX_VALUE)
        .withIntegerParam("maxValue", MIN_VALUE, MAX_VALUE)),
    VALUE_HISTOGRAM(OperationRequest.ValueHistogramRequest.class, self ->
        self.acceptingTypes(ValueType.IMAGE)),
    CONVOLUTION(OperationRequest.ConvolutionRequest.class, self ->
        self.acceptingTypes(ValueType.IMAGE)
        .withMatrixParam("kernel")),
    KIRSH_OPERATOR(OperationRequest.KirshOperatorRequest.class, self ->
        self.acceptingTypes(ValueType.IMAGE)),
    ERROR_MEASUREMENT(OperationRequest.ErrorMeasurementRequest.class, self ->
        self.acceptingTypes(ValueType.IMAGE)
        .withImageParam("imageEntity", "image")),
    TO_GRAYSCALE_CONVERSION(OperationRequest.ToGrayscaleConversionRequest.class,
            self -> self.acceptingTypes(ValueType.IMAGE)),
    COLOR_EXTRACTION(OperationRequest.ColorExtractionRequest.class, self ->
            self.acceptingTypes(ValueType.IMAGE));

    private final Class requestClass;
    private final List<ValueType> lastResult = new ArrayList<>();
    private final Map<String, ParameterSpecification> parameters
            = new HashMap<>();

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

    private OperationSpecification withImageParam(
            String imageField, String nameField
    ) {
        try {
            parameters.put(
                    nameField, new ImageParameterSpecification(
                            requestClass, imageField, nameField
                    )
            );
            return this;
        } catch (NoSuchFieldException e) {
            throw new IllegalStateException("Configuration failure.", e);
        }
    }

    private OperationSpecification withIntegerParam(
            String name, int min, int max
    ) {
        try {
            parameters.put(name, new IntegerParameterSpecification(
                    requestClass, name, min, max
            ));
            return this;
        } catch (NoSuchFieldException e) {
            throw new IllegalStateException("Configuration failure.", e);
        }
    }

    private OperationSpecification withDoubleParam(
            String name, double min, double max
    ) {
        try {
            parameters.put(name, new DoubleParameterSpecification(
                    requestClass, name, min, max
            ));
            return this;
        } catch (NoSuchFieldException e) {
            throw new IllegalStateException("Configuration failure.", e);
        }
    }

    private OperationSpecification withMatrixParam(String name) {
        try {
            parameters.put(name,
                    new MatrixParameterSpecification(requestClass, name)
            );
            return this;
        } catch (NoSuchFieldException e) {
            throw new IllegalStateException("Configuration failure.", e);
        }
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

        parameters.forEach((role, parameterSpec) -> {
            parameterSpec.applyArgumentEntity(request,
                    operationEntity.getArguments().get(role)
            );
        });

        return request;
    }
}
