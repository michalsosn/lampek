package pl.lodz.p.michalsosn.entities.specification;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.springframework.context.ApplicationContext;
import pl.lodz.p.michalsosn.domain.image.channel.Channel;
import pl.lodz.p.michalsosn.domain.image.image.Image;
import pl.lodz.p.michalsosn.domain.image.transform.Kernel;
import pl.lodz.p.michalsosn.entities.ImageEntity;
import pl.lodz.p.michalsosn.entities.OperationEntity;
import pl.lodz.p.michalsosn.entities.ProcessEntity;
import pl.lodz.p.michalsosn.entities.ResultEntity;
import pl.lodz.p.michalsosn.io.BufferedImageIO;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Map;
import java.util.function.UnaryOperator;

import static com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import static pl.lodz.p.michalsosn.domain.image.statistic.Histograms.valueHistogram;
import static pl.lodz.p.michalsosn.domain.image.transform.ChannelOps.convolution;
import static pl.lodz.p.michalsosn.domain.image.transform.HistogramAdjustments.uniformDensity;
import static pl.lodz.p.michalsosn.domain.image.transform.ValueOps.changeBrightness;
import static pl.lodz.p.michalsosn.domain.image.transform.ValueOps.negate;
import static pl.lodz.p.michalsosn.domain.util.Lift.lift;
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
        @Type(name = "UNIFORM_DENSITY",
              value = OperationRequest.UniformDensityRequest.class),
        @Type(name = "VALUE_HISTOGRAM",
              value = OperationRequest.ValueHistogramRequest.class),
        @Type(name = "CONVOLUTION",
              value = OperationRequest.ConvolutionRequest.class)
})
public abstract class OperationRequest {

    private String lastResult;

    public String getLastResult() {
        return lastResult;
    }

    public void setLastResult(String lastResult) {
        this.lastResult = lastResult;
    }

    public abstract void execute(Map<String, ResultEntity> results,
                                 ResultEntity last)
            throws Exception;

    public void execute(OperationEntity entity, OperationEntity parent)
            throws Exception {
        ResultEntity lastResultEntity = null;
        if (parent != null) {
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
        public void execute(Map<String, ResultEntity> results,
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
        public void execute(Map<String, ResultEntity> results,
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
        public void execute(Map<String, ResultEntity> results,
                            ResultEntity last) throws IOException {
            transformDomainImage(
                    results, last, lift(lift(changeBrightness(change)))
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

    public static class UniformDensityRequest extends OperationRequest {
        private int minValue;
        private int maxValue;

        @Override
        public void execute(Map<String, ResultEntity> results,
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

    public static class ValueHistogramRequest extends OperationRequest {
        public void execute(Map<String, ResultEntity> results,
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

        public void execute(Map<String, ResultEntity> results,
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

}
