package pl.lodz.p.michalsosn.specification;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import pl.lodz.p.michalsosn.domain.image.channel.*;
import pl.lodz.p.michalsosn.domain.image.spectrum.ImageSpectrum;
import pl.lodz.p.michalsosn.domain.image.spectrum.Spectrum;
import pl.lodz.p.michalsosn.domain.image.statistic.Errors;
import pl.lodz.p.michalsosn.domain.image.transform.*;
import pl.lodz.p.michalsosn.domain.image.transform.segmentation.Mask;
import pl.lodz.p.michalsosn.domain.image.transform.segmentation.Segmentations;
import pl.lodz.p.michalsosn.domain.util.ArrayUtils;
import pl.lodz.p.michalsosn.domain.util.Record;
import pl.lodz.p.michalsosn.entities.ImageEntity;
import pl.lodz.p.michalsosn.entities.OperationEntity;
import pl.lodz.p.michalsosn.entities.ProcessEntity;
import pl.lodz.p.michalsosn.entities.ResultEntity;
import pl.lodz.p.michalsosn.io.BufferedImageIO;
import pl.lodz.p.michalsosn.util.FunctionAdapters;
import pl.lodz.p.michalsosn.util.Timed;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.UnaryOperator;

import static com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import static pl.lodz.p.michalsosn.domain.Lift.lift;
import static pl.lodz.p.michalsosn.domain.image.statistic.Histograms.valueHistogram;
import static pl.lodz.p.michalsosn.domain.image.statistic.Histograms.valueHistogramRunningTotal;
import static pl.lodz.p.michalsosn.domain.image.transform.ChannelOps.convolution;
import static pl.lodz.p.michalsosn.domain.image.transform.ChannelOps.kirschOperator;
import static pl.lodz.p.michalsosn.domain.image.transform.Filters.*;
import static pl.lodz.p.michalsosn.domain.image.transform.HistogramAdjustments.hyperbolicDensity;
import static pl.lodz.p.michalsosn.domain.image.transform.HistogramAdjustments.uniformDensity;
import static pl.lodz.p.michalsosn.domain.image.transform.ValueOps.*;
import static pl.lodz.p.michalsosn.entities.ResultEntity.*;
import static pl.lodz.p.michalsosn.util.AudaciousConsumer.AudaciousConsumerAdapter;
import static pl.lodz.p.michalsosn.util.Maps.applyToValues;

/**
 * @author Michał Sośnicki
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY,
              property = "type")
@JsonSubTypes(value = {
        @Type(name = "IMAGE_ROOT",
              value = OperationRequest.ImageRootRequest.class),
        @Type(name = "NEGATE",
              value = OperationRequest.NegateRequest.class),
        @Type(name = "CHANGE_BRIGHTNESS",
              value = OperationRequest.ChangeBrightnessRequest.class),
        @Type(name = "CHANGE_CONTRAST",
              value = OperationRequest.ChangeContrastRequest.class),
        @Type(name = "CLIP_BELOW",
              value = OperationRequest.ClipBelowRequest.class),
        @Type(name = "ARITHMETIC_MEAN_FILTER",
              value = OperationRequest.ArithmeticMeanFilterRequest.class),
        @Type(name = "MEDIAN_FILTER",
              value = OperationRequest.MedianFilterRequest.class),
        @Type(name = "UNIFORM_DENSITY",
              value = OperationRequest.UniformDensityRequest.class),
        @Type(name = "HYPERBOLIC_DENSITY",
              value = OperationRequest.HyperbolicDensityRequest.class),
        @Type(name = "VALUE_HISTOGRAM",
              value = OperationRequest.ValueHistogramRequest.class),
        @Type(name = "CONVOLUTION",
              value = OperationRequest.ConvolutionRequest.class),
        @Type(name = "KIRSCH_OPERATOR",
              value = OperationRequest.KirschOperatorRequest.class),
        @Type(name = "ERROR_MEASUREMENT",
              value = OperationRequest.ErrorMeasurementRequest.class),
        @Type(name = "TO_GRAYSCALE_CONVERSION",
              value = OperationRequest.ToGrayscaleConversionRequest.class),
        @Type(name = "COLOR_EXTRACTION",
              value = OperationRequest.ColorExtractionRequest.class),
        @Type(name = "DIT_FFT",
              value = OperationRequest.DitFftRequest.class),
        @Type(name = "INVERSE_DIT_FFT",
              value = OperationRequest.InverseDitFftRequest.class),
        @Type(name = "EXTRACT_RE_IM_PARTS",
              value = OperationRequest.ExtractReImPartsRequest.class),
        @Type(name = "EXTRACT_ABS_PHASE_PARTS",
              value = OperationRequest.ExtractAbsPhasePartsRequest.class),
        @Type(name = "SHIFT_SPECTRUM_PHASE",
              value = OperationRequest.ShiftSpectrumPhaseRequest.class),
        @Type(name = "LOW_PASS_FILTER",
              value = OperationRequest.LowPassFilterRequest.class),
        @Type(name = "HIGH_PASS_FILTER",
              value = OperationRequest.HighPassFilterRequest.class),
        @Type(name = "BAND_PASS_FILTER",
              value = OperationRequest.BandPassFilterRequest.class),
        @Type(name = "BAND_STOP_FILTER",
              value = OperationRequest.BandStopFilterRequest.class),
        @Type(name = "EDGE_DETECTION_FILTER",
              value = OperationRequest.EdgeDetectionFilterRequest.class),
        @Type(name = "SPLIT_MERGE_MAX_RANGE",
              value = OperationRequest.SplitMergeMaxRangeRequest.class),
        @Type(name = "SPLIT_MERGE_MAX_STDDEV",
              value = OperationRequest.SplitMergeMaxStdDevRequest.class),
        @Type(name = "APPLY_IMAGE_MASK",
              value = OperationRequest.ApplyImageMaskRequest.class),
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

    public static class ImageRootRequest extends OperationRequest {

        private String image;
        @JsonIgnore
        private ImageEntity imageEntity;

        @Override
        protected void execute(Map<String, ResultEntity> results,
                            ResultEntity last) throws IOException {
            BufferedImage bufferedImage = imageEntity.getImage();
            results.put("image", new ImageResultEntity(bufferedImage));
        }

        @Override
        public OperationSpecification getSpecification() {
            return OperationSpecification.IMAGE_ROOT;
        }

        public String getImage() {
            return image;
        }
    }

    public static class NegateRequest extends OperationRequest {

        @Override
        protected void execute(Map<String, ResultEntity> results,
                            ResultEntity last) throws IOException {
            transformDomainImage(results, last, lift(lift(negate())));
        }

        @Override
        public OperationSpecification getSpecification() {
            return OperationSpecification.NEGATE;
        }
    }

    public static class ChangeBrightnessRequest extends OperationRequest {
        private int change;

        @Override
        protected void execute(Map<String, ResultEntity> results,
                            ResultEntity last) throws IOException {
            transformDomainImage(
                    results, last,
                    lift(lift(precalculating(changeBrightness(change))))
            );
        }

        @Override
        public OperationSpecification getSpecification() {
            return OperationSpecification.CHANGE_BRIGHTNESS;
        }

        public int getChange() {
            return change;
        }
    }

    public static class ChangeContrastRequest extends OperationRequest {
        private double change;

        @Override
        protected void execute(Map<String, ResultEntity> results,
                            ResultEntity last) throws IOException {
            transformDomainImage(
                    results, last,
                    lift(lift(precalculating(changeContrast(change))))
            );
        }

        @Override
        public OperationSpecification getSpecification() {
            return OperationSpecification.CHANGE_CONTRAST;
        }

        public double getChange() {
            return change;
        }
    }

    public static class ClipBelowRequest extends OperationRequest {
        private int threshold;

        @Override
        protected void execute(Map<String, ResultEntity> results,
                            ResultEntity last) throws IOException {
            transformDomainImage(
                    results, last,
                    lift(lift(precalculating(clipBelow(threshold))))
            );
        }

        @Override
        public OperationSpecification getSpecification() {
            return OperationSpecification.CLIP_BELOW;
        }

        public int getThreshold() {
            return threshold;
        }
    }

    public static class ArithmeticMeanFilterRequest extends OperationRequest {
        private int range;
        private boolean runningWindow;

        @Override
        protected void execute(Map<String, ResultEntity> results,
                            ResultEntity last) throws IOException {
            if (!runningWindow) {
                transformDomainImage(
                        results, last,
                        lift(NoiseFilters.arithmeticMean(range))
                );
            } else {
                transformDomainImage(
                        results, last,
                        lift(NoiseFilters.arithmeticMeanRunning(range))
                );
            }
        }

        @Override
        public OperationSpecification getSpecification() {
            return OperationSpecification.ARITHMETIC_MEAN_FILTER;
        }

        public int getRange() {
            return range;
        }

        public boolean isRunningWindow() {
            return runningWindow;
        }
    }

    public static class MedianFilterRequest extends OperationRequest {
        private int range;
        private boolean runningWindow;

        @Override
        protected void execute(Map<String, ResultEntity> results,
                            ResultEntity last) throws IOException {
            if (!runningWindow) {
                transformDomainImage(
                        results, last,
                        lift(NoiseFilters.median(range))
                );
            } else {
                transformDomainImage(
                        results, last,
                        lift(NoiseFilters.medianRunning(range))
                );
            }
        }

        @Override
        public OperationSpecification getSpecification() {
            return OperationSpecification.MEDIAN_FILTER;
        }

        public int getRange() {
            return range;
        }

        public boolean isRunningWindow() {
            return runningWindow;
        }
    }

    public static class UniformDensityRequest extends OperationRequest {
        private int minValue;
        private int maxValue;

        @Override
        protected void execute(Map<String, ResultEntity> results,
                            ResultEntity last) throws IOException {
            transformDomainImage(
                    results, last, lift(uniformDensity(minValue, maxValue))
            );
        }

        @Override
        public OperationSpecification getSpecification() {
            return OperationSpecification.UNIFORM_DENSITY;
        }

        public int getMinValue() {
            return minValue;
        }

        public int getMaxValue() {
            return maxValue;
        }
    }

    public static class HyperbolicDensityRequest extends OperationRequest {
        private int minValue;
        private int maxValue;

        @Override
        protected void execute(Map<String, ResultEntity> results,
                            ResultEntity last) throws IOException {
            transformDomainImage(
                    results, last, lift(hyperbolicDensity(minValue, maxValue))
            );
        }

        @Override
        public OperationSpecification getSpecification() {
            return OperationSpecification.HYPERBOLIC_DENSITY;
        }

        public int getMinValue() {
            return minValue;
        }

        public int getMaxValue() {
            return maxValue;
        }
    }

    public static class ValueHistogramRequest extends OperationRequest {
        private boolean runningTotal;

        protected void execute(Map<String, ResultEntity> results,
                               ResultEntity last) throws IOException {
            BufferedImage bufferedImage = ((ImageResultEntity) last).getImage();
            Image domainImage = BufferedImageIO.toImage(bufferedImage);
            Map<String, Channel> channelMap = domainImage.getChannels();

            if (!runningTotal) {
                channelMap.forEach((key, channel) -> results.put(key,
                        new HistogramResultEntity(valueHistogram(channel))
                ));
            } else {
                channelMap.forEach((key, channel) -> results.put(key,
                        new HistogramResultEntity(
                                valueHistogramRunningTotal(channel)
                        )
                ));
            }
        }

        @Override
        public OperationSpecification getSpecification() {
            return OperationSpecification.VALUE_HISTOGRAM;
        }

        public boolean isRunningTotal() {
            return runningTotal;
        }
    }

    public static class ConvolutionRequest extends OperationRequest {
        private double[][] kernel;
        private boolean keepSize;

        protected void execute(Map<String, ResultEntity> results,
                            ResultEntity last) throws IOException {
            Kernel normalized = Kernel.normalized(ArrayUtils.copy2d(kernel));
            transformDomainImage(results, last,
                    lift(convolution(normalized, keepSize))
            );
        }

        @Override
        public OperationSpecification getSpecification() {
            return OperationSpecification.CONVOLUTION;
        }

        public double[][] getKernel() {
            return kernel;
        }

        public boolean isKeepSize() {
            return keepSize;
        }
    }

    public static class KirschOperatorRequest extends OperationRequest {

        protected void execute(Map<String, ResultEntity> results,
                            ResultEntity last) throws IOException {
            transformDomainImage(results, last, lift(kirschOperator()));
        }

        @Override
        public OperationSpecification getSpecification() {
            return OperationSpecification.KIRSCH_OPERATOR;
        }

    }

    public static class ErrorMeasurementRequest extends OperationRequest {

        private String image;
        @JsonIgnore
        private ImageEntity imageEntity;

        @Override
        protected void execute(Map<String, ResultEntity> results,
                            ResultEntity last) throws IOException {
            BufferedImage bufferedResult
                    = ((ImageResultEntity) last).getImage();
            BufferedImage bufferedArgument = imageEntity.getImage();
            Image actualImage = BufferedImageIO.toImage(bufferedResult);
            Image expectedImage = BufferedImageIO.toImage(bufferedArgument);

            results.put("MSE", new DoubleResultEntity(
                    Errors.meanSquaredError(expectedImage, actualImage)
                            .getAsDouble()
            ));
            results.put("SNR", new DoubleResultEntity(
                    Errors.signalNoiseRatio(expectedImage, actualImage)
                            .getAsDouble()
            ));
            results.put("PSNR", new DoubleResultEntity(
                    Errors.peakSignalNoiseRatio(expectedImage, actualImage)
                            .getAsDouble()
            ));
            results.put("ENOB", new DoubleResultEntity(
                    Errors.effectiveNumberOfBits(expectedImage, actualImage)
                            .getAsDouble()
            ));
        }

        @Override
        public OperationSpecification getSpecification() {
            return OperationSpecification.ERROR_MEASUREMENT;
        }

        public String getImage() {
            return image;
        }
    }

    private static void transformDomainImage(
            Map<String, ResultEntity> results, ResultEntity last,
            UnaryOperator<Image> action
    ) throws IOException {
        BufferedImage bufferedImage = ((ImageResultEntity) last).getImage();
        Image domainImage = BufferedImageIO.toImage(bufferedImage);
        Image domainResult = action.apply(domainImage);
        BufferedImage bufferedResult = BufferedImageIO.fromImage(domainResult);

        results.put("image", new ImageResultEntity(bufferedResult));
    }

    public static class ToGrayscaleConversionRequest extends OperationRequest {

        @Override
        protected void execute(Map<String, ResultEntity> results,
                            ResultEntity last) throws IOException {
            transformDomainImage(results, last, image ->
                    image.accept(ImageVisitor.imageVisitor(
                            Function.identity(),
                            ColorConversions::rgbToGray
                    ))
            );
        }

        @Override
        public OperationSpecification getSpecification() {
            return OperationSpecification.TO_GRAYSCALE_CONVERSION;
        }
    }

    public static class ColorExtractionRequest extends OperationRequest {

        @Override
        protected void execute(Map<String, ResultEntity> results,
                            ResultEntity last) throws Exception {
            BufferedImage bufferedImage = ((ImageResultEntity) last).getImage();
            Image domainImage = BufferedImageIO.toImage(bufferedImage);

            AudaciousConsumerAdapter<GrayImage> grayConsumer =
                    new AudaciousConsumerAdapter<>(grayImage ->
                            results.put("Gray",
                                    new ImageResultEntity(bufferedImage)
                            )
                    );
            AudaciousConsumerAdapter<RgbImage> rgbConsumer =
                    new AudaciousConsumerAdapter<>(rgbImage -> {
                        results.put("Red", new ImageResultEntity(
                                BufferedImageIO.fromImage(
                                        ColorConversions.extractRed(rgbImage)
                                )
                        ));
                        results.put("Green", new ImageResultEntity(
                                BufferedImageIO.fromImage(
                                        ColorConversions.extractGreen(rgbImage)
                                )
                        ));
                        results.put("Blue", new ImageResultEntity(
                                BufferedImageIO.fromImage(
                                        ColorConversions.extractBlue(rgbImage)
                                )
                        ));
                    });
            domainImage.accept(ImageVisitor.imageVisitor(
                    FunctionAdapters.toFunction(grayConsumer),
                    FunctionAdapters.toFunction(rgbConsumer)
            ));
            grayConsumer.rethrow();
            rgbConsumer.rethrow();
        }

        @Override
        public OperationSpecification getSpecification() {
            return OperationSpecification.COLOR_EXTRACTION;
        }
    }

    public static class DitFftRequest extends OperationRequest {


        @Override
        protected void execute(Map<String, ResultEntity> results,
                               ResultEntity last) throws Exception {
            BufferedImage bufferedImage = ((ImageResultEntity) last).getImage();
            Image domainImage = BufferedImageIO.toImage(bufferedImage);

            Map<String, Channel> channels = domainImage.getChannels();
            Record<Map<String, Spectrum>> spectra = new Record<>(null);

            Duration duration = Timed.timed(() ->
                spectra.set(applyToValues(
                        channels, DitFastFourierTransform::transform
                ))
            );

            Logger log = LoggerFactory.getLogger(DitFftRequest.class);
            log.info("Pure FFT executed in " + duration);

            ImageSpectrum imageSpectrum = new ImageSpectrum(spectra.get());
            BufferedImage bufferedPresentation = BufferedImageIO.fromImage(
                    SpectrumConversions.presentSpectrum(imageSpectrum)
            );
            results.put("image spectrum",
                    new ImageSpectrumResultEntity(
                            imageSpectrum, bufferedPresentation
                    )
            );
        }

        @Override
        public OperationSpecification getSpecification() {
            return OperationSpecification.DIT_FFT;
        }
    }

    public static class InverseDitFftRequest extends OperationRequest {

        @Override
        protected void execute(Map<String, ResultEntity> results,
                               ResultEntity last) throws Exception {
            ImageSpectrum imageSpectrum =
                    ((ImageSpectrumResultEntity) last).getImageSpectrum();

            Map<String, Spectrum> spectra = imageSpectrum.getSpectra();
            Map<String, Channel> invertedChannels = applyToValues(
                    spectra, DitFastFourierTransform::inverse
            );

            Image result = Image.fromChannels(invertedChannels);

            BufferedImage bufferedResult = BufferedImageIO.fromImage(result);
            results.put("image",
                    new ImageResultEntity(bufferedResult)
            );
        }

        @Override
        public OperationSpecification getSpecification() {
            return OperationSpecification.INVERSE_DIT_FFT;
        }

    }

    public static class ExtractReImPartsRequest extends OperationRequest {

        @Override
        protected void execute(Map<String, ResultEntity> results,
                               ResultEntity last) throws Exception {
            ImageSpectrum imageSpectrum =
                    ((ImageSpectrumResultEntity) last).getImageSpectrum();

            Map<String, Spectrum> spectra = imageSpectrum.getSpectra();
            Map<String, Channel> extractedChannels = new HashMap<>();

            spectra.forEach((name, spectrum) -> {
                extractedChannels.put(name + "-Re",
                        SpectrumConversions.spectrumToRe(spectrum));
                extractedChannels.put(name + "-Im",
                        SpectrumConversions.spectrumToIm(spectrum));
            });

            for (String name : extractedChannels.keySet()) {
                Channel channel = extractedChannels.get(name);
                GrayImage valueImage = new GrayImage(channel);
                BufferedImage bufferedResult
                        = BufferedImageIO.fromImage(valueImage);
                results.put(name, new ImageResultEntity(bufferedResult));
            }
        }

        @Override
        public OperationSpecification getSpecification() {
            return OperationSpecification.EXTRACT_RE_IM_PARTS;
        }
    }

    public static class ExtractAbsPhasePartsRequest extends OperationRequest {

        @Override
        protected void execute(Map<String, ResultEntity> results,
                               ResultEntity last) throws Exception {
            ImageSpectrum imageSpectrum =
                    ((ImageSpectrumResultEntity) last).getImageSpectrum();

            Map<String, Spectrum> spectra = imageSpectrum.getSpectra();
            Map<String, Channel> extractedChannels = new HashMap<>();

            spectra.forEach((name, spectrum) -> {
                extractedChannels.put(name + "-Abs",
                        SpectrumConversions.spectrumToAbs(spectrum));
                extractedChannels.put(name + "-Phase",
                        SpectrumConversions.spectrumToPhase(spectrum));
            });

            for (String name : extractedChannels.keySet()) {
                Channel channel = extractedChannels.get(name);
                GrayImage valueImage = new GrayImage(channel);
                BufferedImage bufferedResult
                        = BufferedImageIO.fromImage(valueImage);
                results.put(name, new ImageResultEntity(bufferedResult));
            }
        }

        @Override
        public OperationSpecification getSpecification() {
            return OperationSpecification.EXTRACT_ABS_PHASE_PARTS;
        }
    }

    public static class ShiftSpectrumPhaseRequest extends OperationRequest {

        private int k;
        private int l;

        @Override
        protected void execute(Map<String, ResultEntity> results,
                               ResultEntity last) throws Exception {
            ImageSpectrum imageSpectrum =
                    ((ImageSpectrumResultEntity) last).getImageSpectrum();

            ImageSpectrum resultSpectrum
                    = imageSpectrum.map(SpectrumOps.shiftPhase(k, l));

            BufferedImage bufferedPresentation = BufferedImageIO.fromImage(
                    SpectrumConversions.presentSpectrum(resultSpectrum)
            );
            results.put("image spectrum",
                    new ImageSpectrumResultEntity(
                            resultSpectrum, bufferedPresentation
                    )
            );
        }

        @Override
        public OperationSpecification getSpecification() {
            return OperationSpecification.SHIFT_SPECTRUM_PHASE;
        }

        public int getK() {
            return k;
        }

        public int getL() {
            return l;
        }
    }

    public static class LowPassFilterRequest extends OperationRequest {

        private int range;

        @Override
        protected void execute(Map<String, ResultEntity> results,
                               ResultEntity last) throws Exception {
            ImageSpectrum imageSpectrum =
                    ((ImageSpectrumResultEntity) last).getImageSpectrum();

            ImageSpectrum resultSpectrum
                    = imageSpectrum.map(filterLowPass(range));

            BufferedImage bufferedPresentation = BufferedImageIO.fromImage(
                    SpectrumConversions.presentSpectrum(resultSpectrum)
            );
            results.put("image spectrum",
                    new ImageSpectrumResultEntity(
                            resultSpectrum, bufferedPresentation
                    )
            );
        }

        @Override
        public OperationSpecification getSpecification() {
            return OperationSpecification.LOW_PASS_FILTER;
        }

        public int getRange() {
            return range;
        }
    }

    public static class HighPassFilterRequest extends OperationRequest {

        private int range;
        private boolean preserveMean;

        @Override
        protected void execute(Map<String, ResultEntity> results,
                               ResultEntity last) throws Exception {
            ImageSpectrum imageSpectrum =
                    ((ImageSpectrumResultEntity) last).getImageSpectrum();

            ImageSpectrum resultSpectrum
                    = imageSpectrum.map(filterHighPass(range, preserveMean));

            BufferedImage bufferedPresentation = BufferedImageIO.fromImage(
                    SpectrumConversions.presentSpectrum(resultSpectrum)
            );
            results.put("image spectrum",
                    new ImageSpectrumResultEntity(
                            resultSpectrum, bufferedPresentation
                    )
            );
        }

        @Override
        public OperationSpecification getSpecification() {
            return OperationSpecification.HIGH_PASS_FILTER;
        }

        public int getRange() {
            return range;
        }

        public boolean isPreserveMean() {
            return preserveMean;
        }
    }

    public static class BandPassFilterRequest extends OperationRequest {

        private int innerRange;
        private int outerRange;
        private boolean preserveMean;

        @Override
        protected void execute(Map<String, ResultEntity> results,
                               ResultEntity last) throws Exception {
            ImageSpectrum imageSpectrum =
                    ((ImageSpectrumResultEntity) last).getImageSpectrum();

            ImageSpectrum resultSpectrum = imageSpectrum.map(
                    filterBandPass(innerRange, outerRange, preserveMean)
            );

            BufferedImage bufferedPresentation = BufferedImageIO.fromImage(
                    SpectrumConversions.presentSpectrum(resultSpectrum)
            );
            results.put("image spectrum",
                    new ImageSpectrumResultEntity(
                            resultSpectrum, bufferedPresentation
                    )
            );
        }

        @Override
        public OperationSpecification getSpecification() {
            return OperationSpecification.BAND_PASS_FILTER;
        }

        public int getInnerRange() {
            return innerRange;
        }

        public int getOuterRange() {
            return outerRange;
        }

        public boolean isPreserveMean() {
            return preserveMean;
        }
    }

    public static class BandStopFilterRequest extends OperationRequest {

        private int innerRange;
        private int outerRange;
        private boolean preserveMean;

        @Override
        protected void execute(Map<String, ResultEntity> results,
                               ResultEntity last) throws Exception {
            ImageSpectrum imageSpectrum =
                    ((ImageSpectrumResultEntity) last).getImageSpectrum();

            ImageSpectrum resultSpectrum = imageSpectrum.map(
                    filterBandStop(innerRange, outerRange, preserveMean)
            );

            BufferedImage bufferedPresentation = BufferedImageIO.fromImage(
                    SpectrumConversions.presentSpectrum(resultSpectrum)
            );
            results.put("image spectrum",
                    new ImageSpectrumResultEntity(
                            resultSpectrum, bufferedPresentation
                    )
            );
        }

        @Override
        public OperationSpecification getSpecification() {
            return OperationSpecification.BAND_STOP_FILTER;
        }

        public int getInnerRange() {
            return innerRange;
        }

        public int getOuterRange() {
            return outerRange;
        }

        public boolean isPreserveMean() {
            return preserveMean;
        }
    }

    public static class EdgeDetectionFilterRequest extends OperationRequest {

        private int innerRange;
        private int outerRange;
        private double direction;
        private double angle;
        private boolean preserveMean;

        @Override
        protected void execute(Map<String, ResultEntity> results,
                               ResultEntity last) throws Exception {
            ImageSpectrum imageSpectrum =
                    ((ImageSpectrumResultEntity) last).getImageSpectrum();

            ImageSpectrum resultSpectrum = imageSpectrum.map(
                    filterEdgeDetection(innerRange, outerRange, direction,
                                        angle, preserveMean)
            );

            BufferedImage bufferedPresentation = BufferedImageIO.fromImage(
                    SpectrumConversions.presentSpectrum(resultSpectrum)
            );
            results.put("image spectrum",
                    new ImageSpectrumResultEntity(
                            resultSpectrum, bufferedPresentation
                    )
            );
        }

        @Override
        public OperationSpecification getSpecification() {
            return OperationSpecification.EDGE_DETECTION_FILTER;
        }

        public int getInnerRange() {
            return innerRange;
        }

        public int getOuterRange() {
            return outerRange;
        }

        public double getDirection() {
            return direction;
        }

        public double getAngle() {
            return angle;
        }

        public boolean isPreserveMean() {
            return preserveMean;
        }
    }

    public static class SplitMergeMaxRangeRequest extends OperationRequest {

        private int maxRange;
        private boolean countOnly;

        @Override
        protected void execute(Map<String, ResultEntity> results,
                               ResultEntity last) throws Exception {
            BufferedImage bufferedImage = ((ImageResultEntity) last).getImage();
            Image domainImage = BufferedImageIO.toImage(bufferedImage);

            Mask[] masks = Segmentations.splitMergeImageMaxRange(domainImage, maxRange);

            results.put("mask-count", new IntegerResultEntity(masks.length));
            if (!countOnly) {
                for (int i = 0; i < Math.min(masks.length, 20); ++i) {
                    results.put("mask-" + i, new ImageMaskResultEntity(masks[i]));
                }
            }
        }

        @Override
        public OperationSpecification getSpecification() {
            return OperationSpecification.SPLIT_MERGE_MAX_RANGE;
        }

        public int getMaxRange() {
            return maxRange;
        }

        public boolean isCountOnly() {
            return countOnly;
        }
    }

    public static class SplitMergeMaxStdDevRequest extends OperationRequest {

        private double maxStdDev;
        private boolean countOnly;

        @Override
        protected void execute(Map<String, ResultEntity> results,
                               ResultEntity last) throws Exception {
            BufferedImage bufferedImage = ((ImageResultEntity) last).getImage();
            Image domainImage = BufferedImageIO.toImage(bufferedImage);

            Mask[] masks = Segmentations.splitMergeImageMaxStdDev(domainImage, maxStdDev);

            results.put("mask-count", new IntegerResultEntity(masks.length));
            if (!countOnly) {
                for (int i = 0; i < Math.min(masks.length, 20); ++i) {
                    results.put("mask-" + i, new ImageMaskResultEntity(masks[i]));
                }
            }
        }

        @Override
        public OperationSpecification getSpecification() {
            return OperationSpecification.SPLIT_MERGE_MAX_STDDEV;
        }

        public double getMaxStdDev() {
            return maxStdDev;
        }

        public boolean isCountOnly() {
            return countOnly;
        }
    }

    public static class ApplyImageMaskRequest extends OperationRequest {

        private String image;
        @JsonIgnore
        private ImageEntity imageEntity;

        @Override
        protected void execute(Map<String, ResultEntity> results,
                               ResultEntity last) throws Exception {
            Mask mask = ((ImageMaskResultEntity) last).getMask();
            BufferedImage bufferedImage = imageEntity.getImage();
            Image domainImage = BufferedImageIO.toImage(bufferedImage);

            Image maskedResult = domainImage.map(mask.toOperator());
            BufferedImage bufferedResult = BufferedImageIO.fromImage(maskedResult);

            results.put("image", new ImageResultEntity(bufferedResult));
        }

        @Override
        public OperationSpecification getSpecification() {
            return OperationSpecification.APPLY_IMAGE_MASK;
        }

        public String getImage() {
            return image;
        }
    }
}
