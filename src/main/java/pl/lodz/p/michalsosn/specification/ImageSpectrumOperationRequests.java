package pl.lodz.p.michalsosn.specification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.lodz.p.michalsosn.domain.image.channel.Channel;
import pl.lodz.p.michalsosn.domain.image.channel.GrayImage;
import pl.lodz.p.michalsosn.domain.image.channel.Image;
import pl.lodz.p.michalsosn.domain.image.spectrum.ImageSpectrum;
import pl.lodz.p.michalsosn.domain.image.spectrum.Spectrum2d;
import pl.lodz.p.michalsosn.domain.image.transform.DitFastFourierTransform;
import pl.lodz.p.michalsosn.domain.image.transform.SpectrumConversions;
import pl.lodz.p.michalsosn.domain.image.transform.SpectrumOps;
import pl.lodz.p.michalsosn.domain.util.Record;
import pl.lodz.p.michalsosn.entities.ResultEntity;
import pl.lodz.p.michalsosn.entities.ResultEntity.ImageResultEntity;
import pl.lodz.p.michalsosn.entities.ResultEntity.ImageSpectrumResultEntity;
import pl.lodz.p.michalsosn.io.BufferedImageIO;
import pl.lodz.p.michalsosn.util.Timed;

import java.awt.image.BufferedImage;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import static pl.lodz.p.michalsosn.domain.image.transform.Filters.*;
import static pl.lodz.p.michalsosn.util.Maps.applyToValues;

/**
 * @author Michał Sośnicki
 */
public final class ImageSpectrumOperationRequests {

    private static final String IMAGE_ENTRY = "image";
    private static final String IMAGE_SPECTRUM_ENTRY = "image spectrum";

    private ImageSpectrumOperationRequests() {
    }

    public static class DitFftRequest extends OperationRequest {

        @Override
        protected void execute(Map<String, ResultEntity> results,
                               ResultEntity last) throws Exception {
            BufferedImage bufferedImage = ((ImageResultEntity) last).getImage();
            Image domainImage = BufferedImageIO.toImage(bufferedImage);

            Map<String, Channel> channels = domainImage.getChannels();
            Record<Map<String, Spectrum2d>> spectra = new Record<>(null);

            Duration duration = Timed.timed(() ->
                    spectra.set(applyToValues(
                            channels, DitFastFourierTransform::transform
                    ))
            );

            Logger log = LoggerFactory.getLogger(DitFftRequest.class);
            log.info("Pure 2D FFT executed in " + duration);

            ImageSpectrum imageSpectrum = new ImageSpectrum(spectra.get());
            BufferedImage bufferedPresentation = BufferedImageIO.fromImage(
                    SpectrumConversions.presentSpectrum(imageSpectrum)
            );
            results.put(IMAGE_SPECTRUM_ENTRY,
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

            Map<String, Spectrum2d> spectra = imageSpectrum.getSpectra();
            Record<Map<String, Channel>> invertedChannels = new Record<>(null);
            Duration duration = Timed.timed(() ->
                    invertedChannels.set(applyToValues(
                            spectra, DitFastFourierTransform::inverse
                    ))
            );

            Logger log = LoggerFactory.getLogger(InverseDitFftRequest.class);
            log.info("Pure 2D IFFT executed in " + duration);

            Image result = Image.fromChannels(invertedChannels.get());
            BufferedImage bufferedResult = BufferedImageIO.fromImage(result);
            results.put(IMAGE_ENTRY,
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

            Map<String, Spectrum2d> spectra = imageSpectrum.getSpectra();
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

            Map<String, Spectrum2d> spectra = imageSpectrum.getSpectra();
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
            results.put(IMAGE_SPECTRUM_ENTRY,
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
            results.put(IMAGE_SPECTRUM_ENTRY,
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
            results.put(IMAGE_SPECTRUM_ENTRY,
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
            results.put(IMAGE_SPECTRUM_ENTRY,
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
            results.put(IMAGE_SPECTRUM_ENTRY,
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
            results.put(IMAGE_SPECTRUM_ENTRY,
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
}
