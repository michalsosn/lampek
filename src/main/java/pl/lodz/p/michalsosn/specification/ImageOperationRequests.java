package pl.lodz.p.michalsosn.specification;

import com.fasterxml.jackson.annotation.JsonIgnore;
import pl.lodz.p.michalsosn.domain.image.channel.*;
import pl.lodz.p.michalsosn.domain.image.statistic.Errors;
import pl.lodz.p.michalsosn.domain.image.transform.ColorConversions;
import pl.lodz.p.michalsosn.domain.image.transform.Kernel;
import pl.lodz.p.michalsosn.domain.image.transform.NoiseFilters;
import pl.lodz.p.michalsosn.domain.image.transform.segmentation.Mask;
import pl.lodz.p.michalsosn.domain.image.transform.segmentation.Segmentations;
import pl.lodz.p.michalsosn.domain.util.ArrayUtils;
import pl.lodz.p.michalsosn.entities.ImageEntity;
import pl.lodz.p.michalsosn.entities.ResultEntity;
import pl.lodz.p.michalsosn.io.BufferedImageIO;
import pl.lodz.p.michalsosn.util.AudaciousConsumer.AudaciousConsumerAdapter;
import pl.lodz.p.michalsosn.util.FunctionAdapters;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Map;
import java.util.function.Function;
import java.util.function.UnaryOperator;

import static pl.lodz.p.michalsosn.domain.Lift.lift;
import static pl.lodz.p.michalsosn.domain.image.statistic.Histograms.valueHistogram;
import static pl.lodz.p.michalsosn.domain.image.statistic.Histograms.valueHistogramRunningTotal;
import static pl.lodz.p.michalsosn.domain.image.transform.ChannelOps.convolution;
import static pl.lodz.p.michalsosn.domain.image.transform.ChannelOps.kirschOperator;
import static pl.lodz.p.michalsosn.domain.image.transform.HistogramAdjustments.hyperbolicDensity;
import static pl.lodz.p.michalsosn.domain.image.transform.HistogramAdjustments.uniformDensity;
import static pl.lodz.p.michalsosn.domain.image.transform.ValueOps.*;
import static pl.lodz.p.michalsosn.entities.ResultEntity.*;

/**
 * @author Michał Sośnicki
 */
public final class ImageOperationRequests {

    private static final String IMAGE_ENTRY = "image";

    private ImageOperationRequests() {
    }

    public static class LoadImageRequest extends OperationRequest {

        private String image;
        @JsonIgnore
        private ImageEntity imageEntity;

        @Override
        protected void execute(Map<String, ResultEntity> results,
                               ResultEntity last) throws IOException {
            BufferedImage bufferedImage = imageEntity.getImage();
            results.put(IMAGE_ENTRY, new ImageResultEntity(bufferedImage));
        }

        @Override
        public OperationSpecification getSpecification() {
            return OperationSpecification.LOAD_IMAGE;
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
                        new ImageHistogramResultEntity(valueHistogram(channel))
                ));
            } else {
                channelMap.forEach((key, channel) -> results.put(key,
                        new ImageHistogramResultEntity(
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

            results.put(IMAGE_ENTRY, new ImageResultEntity(bufferedResult));
        }

        @Override
        public OperationSpecification getSpecification() {
            return OperationSpecification.APPLY_IMAGE_MASK;
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

        results.put(IMAGE_ENTRY, new ImageResultEntity(bufferedResult));
    }

}
