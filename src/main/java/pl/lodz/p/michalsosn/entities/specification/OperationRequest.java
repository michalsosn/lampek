package pl.lodz.p.michalsosn.entities.specification;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.springframework.context.ApplicationContext;
import pl.lodz.p.michalsosn.domain.image.channel.Channel;
import pl.lodz.p.michalsosn.domain.image.image.GrayImage;
import pl.lodz.p.michalsosn.domain.image.image.Image;
import pl.lodz.p.michalsosn.domain.image.image.ImageVisitor;
import pl.lodz.p.michalsosn.domain.image.image.RgbImage;
import pl.lodz.p.michalsosn.domain.image.statistic.Errors;
import pl.lodz.p.michalsosn.domain.image.transform.ColorConvertions;
import pl.lodz.p.michalsosn.domain.image.transform.Kernel;
import pl.lodz.p.michalsosn.domain.image.transform.NoiseFilters;
import pl.lodz.p.michalsosn.domain.utils.FunctionAdapters;
import pl.lodz.p.michalsosn.entities.ImageEntity;
import pl.lodz.p.michalsosn.entities.OperationEntity;
import pl.lodz.p.michalsosn.entities.ProcessEntity;
import pl.lodz.p.michalsosn.entities.ResultEntity;
import pl.lodz.p.michalsosn.io.BufferedImageIO;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Map;
import java.util.function.Function;
import java.util.function.UnaryOperator;

import static com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import static pl.lodz.p.michalsosn.domain.image.statistic.Histograms.valueHistogram;
import static pl.lodz.p.michalsosn.domain.image.transform.ChannelOps.convolution;
import static pl.lodz.p.michalsosn.domain.image.transform.ChannelOps.kirschOperator;
import static pl.lodz.p.michalsosn.domain.image.transform.HistogramAdjustments.hyperbolicDensity;
import static pl.lodz.p.michalsosn.domain.image.transform.HistogramAdjustments.uniformDensity;
import static pl.lodz.p.michalsosn.domain.image.transform.ValueOps.*;
import static pl.lodz.p.michalsosn.domain.utils.AudaciousConsumer.AudaciousConsumerAdapter;
import static pl.lodz.p.michalsosn.domain.utils.Lift.lift;
import static pl.lodz.p.michalsosn.entities.ResultEntity.HistogramResultEntity;
import static pl.lodz.p.michalsosn.entities.ResultEntity.ImageResultEntity;

/**
 * @author Michał Sośnicki
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY,
              property = "type")
@JsonSubTypes(value = {
        @Type(name = "START",
              value = OperationRequest.StartRequest.class),
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
        @Type(name = "KIRSH_OPERATOR",
              value = OperationRequest.KirshOperatorRequest.class),
        @Type(name = "ERROR_MEASUREMENT",
              value = OperationRequest.ErrorMeasurementRequest.class),
        @Type(name = "TO_GRAYSCALE_CONVERSION",
        value = OperationRequest.ToGrayscaleConversionRequest.class),
        @Type(name = "COLOR_EXTRACTION",
        value = OperationRequest.ColorExtractionRequest.class)
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

    public static class StartRequest extends OperationRequest {

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
            return OperationSpecification.START;
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

        @Override
        protected void execute(Map<String, ResultEntity> results,
                            ResultEntity last) throws IOException {
            transformDomainImage(
                    results, last,
                    lift(NoiseFilters.arithmeticMean(range))
            );
        }

        @Override
        public OperationSpecification getSpecification() {
            return OperationSpecification.ARITHMETIC_MEAN_FILTER;
        }

        public int getRange() {
            return range;
        }
    }

    public static class MedianFilterRequest extends OperationRequest {
        private int range;

        @Override
        protected void execute(Map<String, ResultEntity> results,
                            ResultEntity last) throws IOException {
            transformDomainImage(
                    results, last,
                    lift(NoiseFilters.median(range))
            );
        }

        @Override
        public OperationSpecification getSpecification() {
            return OperationSpecification.MEDIAN_FILTER;
        }

        public int getRange() {
            return range;
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
        protected void execute(Map<String, ResultEntity> results,
                            ResultEntity last) throws IOException {
            BufferedImage bufferedImage = ((ImageResultEntity) last).getImage();
            Image domainImage = BufferedImageIO.toImage(bufferedImage);
            Map<String, Channel> channelMap = domainImage.getChannels();

            channelMap.forEach((key, channel) -> results.put(key,
                    new HistogramResultEntity(valueHistogram(channel))
            ));
        }

        @Override
        public OperationSpecification getSpecification() {
            return OperationSpecification.VALUE_HISTOGRAM;
        }
    }

    public static class ConvolutionRequest extends OperationRequest {
        private double[][] kernel;

        protected void execute(Map<String, ResultEntity> results,
                            ResultEntity last) throws IOException {
            Kernel normalized = Kernel.normalized(kernel);
            transformDomainImage(results, last, lift(convolution(normalized)));
        }

        @Override
        public OperationSpecification getSpecification() {
            return OperationSpecification.CONVOLUTION;
        }

        public double[][] getKernel() {
            return kernel;
        }
    }

    public static class KirshOperatorRequest extends OperationRequest {

        protected void execute(Map<String, ResultEntity> results,
                            ResultEntity last) throws IOException {
            transformDomainImage(results, last, lift(kirschOperator()));
        }

        @Override
        public OperationSpecification getSpecification() {
            return OperationSpecification.KIRSH_OPERATOR;
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

            results.put("MSE", new ResultEntity.DoubleResultEntity(
                    Errors.meanSquaredError(expectedImage, actualImage)
                            .getAsDouble()
            ));
            results.put("SNR", new ResultEntity.DoubleResultEntity(
                    Errors.signalNoiseRatio(expectedImage, actualImage)
                            .getAsDouble()
            ));
            results.put("PSNR", new ResultEntity.DoubleResultEntity(
                    Errors.peakSignalNoiseRatio(expectedImage, actualImage)
                            .getAsDouble()
            ));
            results.put("ENOB", new ResultEntity.DoubleResultEntity(
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
                            ColorConvertions::rgbToGray
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
                                        ColorConvertions.extractRed(rgbImage)
                                )
                        ));
                        results.put("Green", new ImageResultEntity(
                                BufferedImageIO.fromImage(
                                        ColorConvertions.extractGreen(rgbImage)
                                )
                        ));
                        results.put("Blue", new ImageResultEntity(
                                BufferedImageIO.fromImage(
                                        ColorConvertions.extractBlue(rgbImage)
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

}
