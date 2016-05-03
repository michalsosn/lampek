package pl.lodz.p.michalsosn.specification;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.springframework.context.ApplicationContext;
import pl.lodz.p.michalsosn.entities.OperationEntity;
import pl.lodz.p.michalsosn.entities.ProcessEntity;
import pl.lodz.p.michalsosn.entities.ResultEntity;

import java.util.Map;

import static com.fasterxml.jackson.annotation.JsonSubTypes.Type;

/**
 * @author Michał Sośnicki
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY,
              property = "type")
@JsonSubTypes(value = {
        @Type(name = "LOAD_IMAGE",
              value = ImageOperationRequests.LoadImageRequest.class),
        @Type(name = "NEGATE",
              value = ImageOperationRequests.NegateRequest.class),
        @Type(name = "CHANGE_BRIGHTNESS",
              value = ImageOperationRequests.ChangeBrightnessRequest.class),
        @Type(name = "CHANGE_CONTRAST",
              value = ImageOperationRequests.ChangeContrastRequest.class),
        @Type(name = "CLIP_BELOW",
              value = ImageOperationRequests.ClipBelowRequest.class),
        @Type(name = "ARITHMETIC_MEAN_FILTER",
              value = ImageOperationRequests.ArithmeticMeanFilterRequest.class),
        @Type(name = "MEDIAN_FILTER",
              value = ImageOperationRequests.MedianFilterRequest.class),
        @Type(name = "UNIFORM_DENSITY",
              value = ImageOperationRequests.UniformDensityRequest.class),
        @Type(name = "HYPERBOLIC_DENSITY",
              value = ImageOperationRequests.HyperbolicDensityRequest.class),
        @Type(name = "VALUE_HISTOGRAM",
              value = ImageOperationRequests.ValueHistogramRequest.class),
        @Type(name = "CONVOLUTION",
              value = ImageOperationRequests.ConvolutionRequest.class),
        @Type(name = "KIRSCH_OPERATOR",
              value = ImageOperationRequests.KirschOperatorRequest.class),
        @Type(name = "ERROR_MEASUREMENT",
              value = ImageOperationRequests.ErrorMeasurementRequest.class),
        @Type(name = "TO_GRAYSCALE_CONVERSION",
              value = ImageOperationRequests.ToGrayscaleConversionRequest.class),
        @Type(name = "COLOR_EXTRACTION",
              value = ImageOperationRequests.ColorExtractionRequest.class),
        @Type(name = "SPLIT_MERGE_MAX_RANGE",
              value = ImageOperationRequests.SplitMergeMaxRangeRequest.class),
        @Type(name = "SPLIT_MERGE_MAX_STDDEV",
              value = ImageOperationRequests.SplitMergeMaxStdDevRequest.class),
        @Type(name = "APPLY_IMAGE_MASK",
              value = ImageOperationRequests.ApplyImageMaskRequest.class),
        @Type(name = "DIT_FFT",
              value = ImageSpectrumOperationRequests.DitFftRequest.class),
        @Type(name = "INVERSE_DIT_FFT",
              value = ImageSpectrumOperationRequests.InverseDitFftRequest.class),
        @Type(name = "EXTRACT_RE_IM_PARTS",
              value = ImageSpectrumOperationRequests.ExtractReImPartsRequest.class),
        @Type(name = "EXTRACT_ABS_PHASE_PARTS",
              value = ImageSpectrumOperationRequests.ExtractAbsPhasePartsRequest.class),
        @Type(name = "SHIFT_SPECTRUM_PHASE",
              value = ImageSpectrumOperationRequests.ShiftSpectrumPhaseRequest.class),
        @Type(name = "LOW_PASS_FILTER",
              value = ImageSpectrumOperationRequests.LowPassFilterRequest.class),
        @Type(name = "HIGH_PASS_FILTER",
              value = ImageSpectrumOperationRequests.HighPassFilterRequest.class),
        @Type(name = "BAND_PASS_FILTER",
              value = ImageSpectrumOperationRequests.BandPassFilterRequest.class),
        @Type(name = "BAND_STOP_FILTER",
              value = ImageSpectrumOperationRequests.BandStopFilterRequest.class),
        @Type(name = "EDGE_DETECTION_FILTER",
              value = ImageSpectrumOperationRequests.EdgeDetectionFilterRequest.class),
        @Type(name = "LOAD_SOUND",
              value = SoundOperationRequests.LoadSoundRequest.class),
        @Type(name = "GENERATE_SINE_SOUND",
              value = SoundOperationRequests.GenerateSineSoundlRequest.class),
        @Type(name = "SCALE_VALUE",
              value = SoundOperationRequests.ScaleValueRequest.class),
        @Type(name = "CLIP_ABOVE",
              value = SoundOperationRequests.ClipAboveRequest.class),
        @Type(name = "SHORTEN_SOUND",
              value = SoundOperationRequests.ShortenSoundRequest.class),
        @Type(name = "SHORTEN_TO_POWER_OF_TWO",
              value = SoundOperationRequests.ShortenToPowerOfTwoRequest.class),
        @Type(name = "CYCLIC_AUTOCORRELATION",
              value = SoundOperationRequests.CyclicAutocorrelationRequest.class),
        @Type(name = "LINEAR_AUTOCORRELATION",
              value = SoundOperationRequests.LinearAutocorrelationRequest.class),
        @Type(name = "BASIC_FREQUENCY_AUTOCORRELATION",
              value = SoundOperationRequests.BasicFrequencyAutocorrelationRequest.class),
        @Type(name = "APPROXIMATE_AUTOCORRELATION",
              value = SoundOperationRequests.ApproximateAutocorrelationRequest.class),
        @Type(name = "SOUND_DIT_FFT",
              value = SoundSpectrumOperationRequests.SoundDitFftRequest.class),
        @Type(name = "SOUND_INVERSE_DIT_FFT",
              value = SoundSpectrumOperationRequests.SoundInverseDitFftRequest.class),
        @Type(name = "CEPSTRUM",
              value = SoundSpectrumOperationRequests.CepstrumRequest.class),
        @Type(name = "BASIC_FREQUENCY_CEPSTRUM",
              value = SoundSpectrumOperationRequests.BasicFrequencyCepstrumRequest.class),
        @Type(name = "APPROXIMATE_CEPSTRUM",
              value = SoundSpectrumOperationRequests.ApproximateCepstrumRequest.class)
})
public abstract class OperationRequest {

    private String lastResult;

    public String getLastResult() {
        return lastResult;
    }

    public void setLastResult(String lastResult) {
        this.lastResult = lastResult;
    }

    protected abstract void execute(Map<String, ResultEntity> results,
                                    ResultEntity last)
            throws Exception;

    public void execute(OperationEntity entity, OperationEntity parent)
            throws Exception {
        ResultEntity lastResultEntity = null;
        if (parent != null) {
            if (parent.isFailed()) {
                throw new IllegalArgumentException(
                        "Parent " + parent.getId() + " ended in failure"
                );
            }
            lastResultEntity = parent.getResults().get(lastResult);
        }

        execute(entity.getResults(), lastResultEntity);

        entity.getResults().forEach((role, resultEntity) -> {
            resultEntity.setOperation(entity);
            resultEntity.setRole(role);
        });
    }

    public abstract OperationSpecification getSpecification();

    public OperationEntity entitize(ProcessEntity process,
                                    ApplicationContext context,
                                    String username) {
        return getSpecification().entitize(this, process, context, username);
    }


}
