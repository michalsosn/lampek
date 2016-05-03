package pl.lodz.p.michalsosn.specification;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.context.ApplicationContext;
import pl.lodz.p.michalsosn.domain.sound.sound.Sound;
import pl.lodz.p.michalsosn.entities.OperationEntity;
import pl.lodz.p.michalsosn.entities.ProcessEntity;
import pl.lodz.p.michalsosn.entities.ResultType;
import pl.lodz.p.michalsosn.specification.SoundOperationRequests.*;
import pl.lodz.p.michalsosn.specification.SoundSpectrumOperationRequests.BasicFrequencyCepstrumRequest;

import java.util.*;
import java.util.function.Consumer;

import static pl.lodz.p.michalsosn.domain.image.channel.Image.MAX_VALUE;
import static pl.lodz.p.michalsosn.domain.image.channel.Image.MIN_VALUE;
import static pl.lodz.p.michalsosn.domain.sound.sound.Sound.MID_VALUE;
import static pl.lodz.p.michalsosn.specification.ImageOperationRequests.*;
import static pl.lodz.p.michalsosn.specification.ImageSpectrumOperationRequests.*;
import static pl.lodz.p.michalsosn.specification.SoundSpectrumOperationRequests.*;
import static pl.lodz.p.michalsosn.specification.SoundSpectrumOperationRequests.SoundDitFftRequest;
import static pl.lodz.p.michalsosn.specification.SoundSpectrumOperationRequests.SoundInverseDitFftRequest;

/**
 * @author Michał Sośnicki
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum OperationSpecification {
    LOAD_IMAGE(LoadImageRequest.class, self ->
        self.inCategory("Image")
        .withImageParam("Image", "imageEntity", "image")),
    NEGATE(NegateRequest.class, self ->
        self.acceptingTypes(ResultType.IMAGE)
        .inCategory("Image")),
    CHANGE_BRIGHTNESS(ChangeBrightnessRequest.class, self ->
        self.acceptingTypes(ResultType.IMAGE)
        .inCategory("Image")
        .withIntegerParam("brightness change", "change", -MAX_VALUE, MAX_VALUE)),
    CHANGE_CONTRAST(ChangeContrastRequest.class, self ->
        self.acceptingTypes(ResultType.IMAGE)
        .inCategory("Image")
        .withDoubleParam("contrast change", "change", 0.0, 128.0)),
    CLIP_BELOW(ClipBelowRequest.class, self ->
        self.acceptingTypes(ResultType.IMAGE)
        .inCategory("Image")
        .withIntegerParam("clipping threshold", "threshold", MIN_VALUE, MAX_VALUE)),
    ARITHMETIC_MEAN_FILTER(ArithmeticMeanFilterRequest.class, self ->
        self.acceptingTypes(ResultType.IMAGE)
        .inCategory("Image")
        .withIntegerParam("range", 0, 30)
        .withBooleanParam("use sliding window algorithm", "runningWindow")),
    MEDIAN_FILTER(MedianFilterRequest.class, self ->
        self.acceptingTypes(ResultType.IMAGE)
        .inCategory("Image")
        .withIntegerParam("range", 0, 30)
        .withBooleanParam("use sliding window algorithm", "runningWindow")),
    UNIFORM_DENSITY(UniformDensityRequest.class, self ->
        self.acceptingTypes(ResultType.IMAGE)
        .inCategory("Image")
        .withIntegerParam("lower bound", "minValue", MIN_VALUE, MAX_VALUE)
        .withIntegerParam("upper bound", "maxValue", MIN_VALUE, MAX_VALUE)),
    HYPERBOLIC_DENSITY(HyperbolicDensityRequest.class, self ->
        self.acceptingTypes(ResultType.IMAGE)
        .inCategory("Image")
        .withIntegerParam("lower bound", "minValue", MIN_VALUE + 1, MAX_VALUE)
        .withIntegerParam("upper bound", "maxValue", MIN_VALUE + 1, MAX_VALUE)),
    VALUE_HISTOGRAM(ValueHistogramRequest.class, self ->
        self.acceptingTypes(ResultType.IMAGE)
        .inCategory("Image")
        .withBooleanParam("running total", "runningTotal")),
    CONVOLUTION(ConvolutionRequest.class, self ->
        self.acceptingTypes(ResultType.IMAGE)
        .inCategory("Image")
        .withMatrixParam("kernel")
        .withBooleanParam("keep size", "keepSize")),
    KIRSCH_OPERATOR(KirschOperatorRequest.class, self ->
        self.acceptingTypes(ResultType.IMAGE)
        .inCategory("Image")),
    ERROR_MEASUREMENT(ErrorMeasurementRequest.class, self ->
        self.acceptingTypes(ResultType.IMAGE)
        .inCategory("Image")
        .withImageParam("expected image", "imageEntity", "image")),
    TO_GRAYSCALE_CONVERSION(ToGrayscaleConversionRequest.class, self ->
        self.acceptingTypes(ResultType.IMAGE)
        .inCategory("Image")),
    COLOR_EXTRACTION(ColorExtractionRequest.class, self ->
        self.acceptingTypes(ResultType.IMAGE)
        .inCategory("Image")),
    SPLIT_MERGE_MAX_RANGE(SplitMergeMaxRangeRequest.class, self ->
        self.acceptingTypes(ResultType.IMAGE_MASK)
        .inCategory("Image 2")
        .withIntegerParam("maxRange", MIN_VALUE, MAX_VALUE)
        .withBooleanParam("countOnly")),
    SPLIT_MERGE_MAX_STDDEV(SplitMergeMaxStdDevRequest.class, self ->
        self.acceptingTypes(ResultType.IMAGE_MASK)
        .inCategory("Image 2")
        .withDoubleParam("maxStdDev", 0, 128.0)
        .withBooleanParam("countOnly")),
    APPLY_IMAGE_MASK(ApplyImageMaskRequest.class, self ->
        self.acceptingTypes(ResultType.IMAGE_MASK)
        .inCategory("Image 2")
        .withImageParam("image to mask", "imageEntity", "image")),
    DIT_FFT(DitFftRequest.class, self ->
        self.acceptingTypes(ResultType.IMAGE)
        .withDescription("DIT FFT")
        .inCategory("Image 2")),
    INVERSE_DIT_FFT(InverseDitFftRequest.class, self ->
        self.acceptingTypes(ResultType.IMAGE_SPECTRUM)
        .withDescription("Inverse DIT FFT")
        .inCategory("Image 2")),
    EXTRACT_RE_IM_PARTS(ExtractReImPartsRequest.class, self ->
        self.acceptingTypes(ResultType.IMAGE_SPECTRUM)
        .withDescription("Extract real and imaginary parts")
        .inCategory("Image 2")),
    EXTRACT_ABS_PHASE_PARTS(ExtractAbsPhasePartsRequest.class, self ->
        self.acceptingTypes(ResultType.IMAGE_SPECTRUM)
        .withDescription("Extract value and phase parts")
        .inCategory("Image 2")),
    SHIFT_SPECTRUM_PHASE(ShiftSpectrumPhaseRequest.class, self ->
        self.acceptingTypes(ResultType.IMAGE_SPECTRUM)
        .inCategory("Image 2")
        .withIntegerParam("k", -1024, 1024)
        .withIntegerParam("l", -1024, 1024)),
    LOW_PASS_FILTER(LowPassFilterRequest.class, self ->
        self.acceptingTypes(ResultType.IMAGE_SPECTRUM)
        .inCategory("Image 2")
        .withIntegerParam("range", 0, 1024)),
    HIGH_PASS_FILTER(HighPassFilterRequest.class, self ->
        self.acceptingTypes(ResultType.IMAGE_SPECTRUM)
        .inCategory("Image 2")
        .withIntegerParam("range", 0, 1024)
        .withBooleanParam("preserveMean")),
    BAND_PASS_FILTER(BandPassFilterRequest.class, self ->
        self.acceptingTypes(ResultType.IMAGE_SPECTRUM)
        .inCategory("Image 2")
        .withIntegerParam("innerRange", 0, 1024)
        .withIntegerParam("outerRange", 0, 1024)
        .withBooleanParam("preserveMean")),
    BAND_STOP_FILTER(BandStopFilterRequest.class, self ->
        self.acceptingTypes(ResultType.IMAGE_SPECTRUM)
        .inCategory("Image 2")
        .withIntegerParam("innerRange", 0, 1024)
        .withIntegerParam("outerRange", 0, 1024)
        .withBooleanParam("preserveMean")),
    EDGE_DETECTION_FILTER(EdgeDetectionFilterRequest.class, self ->
        self.acceptingTypes(ResultType.IMAGE_SPECTRUM)
        .inCategory("Image 2")
        .withIntegerParam("innerRange", 0, 1024)
        .withIntegerParam("outerRange", 0, 1024)
        .withDoubleParam("direction", 0.0, 360.0)
        .withDoubleParam("angle", 0.0, 180.0)
        .withBooleanParam("preserveMean")),
    LOAD_SOUND(LoadSoundRequest.class, self ->
        self.inCategory("Sound")
        .withSoundParam("Sound", "soundEntity", "sound")),
    GENERATE_SINE_SOUND(GenerateSineSoundlRequest.class, self ->
        self.inCategory("Sound")
        .withIntegerParam("Amplitude", "amplitude", 0, Sound.MAX_VALUE - MID_VALUE)
        .withDoubleParam("Basic frequency (Hz)", "basicFrequency",
                         0.0, Integer.MAX_VALUE)
        .withDoubleParam("Start phase (degrees)", "startPhase", 0.0, 360.0)
        .withIntegerParam("Length (samples)", "length", 0, Integer.MAX_VALUE)
        .withDoubleParam("Sampling frequency (Hz)", "samplingFrequency",
                         0.0, Integer.MAX_VALUE)
    ),
    SCALE_VALUE(ScaleValueRequest.class, self ->
        self.acceptingTypes(ResultType.SOUND)
        .inCategory("Sound")
        .withDoubleParam("value scale", "change", 0.0,
                         Sound.MAX_VALUE - MID_VALUE)),
    CLIP_ABOVE(ClipAboveRequest.class, self ->
        self.acceptingTypes(ResultType.SOUND)
        .inCategory("Sound")
        .withIntegerParam("clipping threshold", "threshold",
                          0, Sound.MAX_VALUE - MID_VALUE)),
    SHORTEN_SOUND(ShortenSoundRequest.class, self ->
        self.acceptingTypes(ResultType.SOUND)
        .inCategory("Sound")
        .withIntegerParam("skip", 0, Integer.MAX_VALUE)
        .withIntegerParam("take", 0, Integer.MAX_VALUE)),
    SHORTEN_TO_POWER_OF_TWO(ShortenToPowerOfTwoRequest.class, self ->
        self.acceptingTypes(ResultType.SOUND)
        .inCategory("Sound")),
    CYCLIC_AUTOCORRELATION(CyclicAutocorrelationRequest.class, self ->
        self.acceptingTypes(ResultType.SOUND)
        .inCategory("Sound")),
    LINEAR_AUTOCORRELATION(LinearAutocorrelationRequest.class, self ->
        self.acceptingTypes(ResultType.SOUND)
        .inCategory("Sound")),
    BASIC_FREQUENCY_AUTOCORRELATION(BasicFrequencyAutocorrelationRequest.class, self ->
        self.acceptingTypes(ResultType.SOUND)
        .withDescription("Find basic frequency using autocorrelation")
        .inCategory("Sound")
        .withDoubleParam("threshold", 0.0, 1.0)
        .withIntegerParam("windowLength", 1, Integer.MAX_VALUE)),
    APPROXIMATE_AUTOCORRELATION(ApproximateAutocorrelationRequest.class, self ->
        self.acceptingTypes(ResultType.SOUND)
        .withDescription("Approximate with basic frequencies using autocorrelation")
        .inCategory("Sound")
        .withDoubleParam("threshold", 0.0, 1.0)
        .withIntegerParam("windowLength", 1, Integer.MAX_VALUE)),
    SOUND_DIT_FFT(SoundDitFftRequest.class, self ->
        self.acceptingTypes(ResultType.SOUND, ResultType.SOUND_SPECTRUM)
        .withDescription("DIT FFT")
        .inCategory("Sound 2")),
    SOUND_INVERSE_DIT_FFT(SoundInverseDitFftRequest.class, self ->
        self.acceptingTypes(ResultType.SOUND_SPECTRUM)
        .withDescription("Inverse DIT FFT")
        .inCategory("Sound 2")),
    CEPSTRUM(CepstrumRequest.class, self ->
        self.acceptingTypes(ResultType.SOUND)
        .inCategory("Sound 2")),
    BASIC_FREQUENCY_CEPSTRUM(BasicFrequencyCepstrumRequest.class, self ->
        self.acceptingTypes(ResultType.SOUND)
        .withDescription("Find basic frequency using cepstrum")
        .inCategory("Sound 2")
        .withIntegerParam("windowLength", 1, Integer.MAX_VALUE)),
    APPROXIMATE_CEPSTRUM(ApproximateCepstrumRequest.class, self ->
        self.acceptingTypes(ResultType.SOUND)
        .withDescription("Approximate with basic frequencies using cepstrum")
        .inCategory("Sound 2")
        .withIntegerParam("windowLength", 1, Integer.MAX_VALUE));

    private final Class requestClass;
    private final List<ResultType> lastResult = new ArrayList<>();
    private final Map<String, ParameterSpecification> parameters = new HashMap<>();
    private String description;
    private String category;

    OperationSpecification(
            Class requestClass, Consumer<OperationSpecification> configurer
    ) {
        this.requestClass = requestClass;
        configurer.accept(this);
    }

    private OperationSpecification acceptingTypes(ResultType... types) {
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

    private OperationSpecification withSoundParam(
            String description, String soundField, String nameField
    ) {
        try {
            parameters.put(
                    nameField, new SoundParameterSpecification(
                            description, requestClass, soundField, nameField
                    )
            );
            return this;
        } catch (NoSuchFieldException e) {
            throw new IllegalStateException("Configuration failure.", e);
        }
    }

    private OperationSpecification withSoundParam(
            String imageField, String nameField
    ) {
        return withSoundParam(null, imageField, nameField);
    }

    public List<ResultType> getLastResult() {
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
