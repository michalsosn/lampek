package pl.lodz.p.michalsosn.specification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.lodz.p.michalsosn.domain.sound.sound.BufferSound;
import pl.lodz.p.michalsosn.domain.sound.sound.Sound;
import pl.lodz.p.michalsosn.domain.sound.spectrum.Spectrum1d;
import pl.lodz.p.michalsosn.domain.sound.transform.DitFastFourierTransform;
import pl.lodz.p.michalsosn.entities.ResultEntity;
import pl.lodz.p.michalsosn.entities.ResultEntity.DoubleResultEntity;
import pl.lodz.p.michalsosn.entities.ResultEntity.SoundSpectrumResultEntity;
import pl.lodz.p.michalsosn.util.Timed;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.OptionalDouble;
import java.util.function.ToDoubleFunction;
import java.util.stream.Collectors;

import static pl.lodz.p.michalsosn.domain.sound.transform.BasicFrequencyAnalysis.*;
import static pl.lodz.p.michalsosn.entities.ResultEntity.SoundResultEntity;

/**
 * @author Michał Sośnicki
 */
public final class SoundSpectrumOperationRequests {

    private static final String SOUND_ENTRY = "sound";
    private static final String SOUND_SPECTRUM_ENTRY = "sound spectrum";

    private SoundSpectrumOperationRequests() {
    }

    public static class SoundDitFftRequest extends OperationRequest {

        @Override
        protected void execute(Map<String, ResultEntity> results,
                               ResultEntity last) throws Exception {
            Spectrum1d[] result = new Spectrum1d[1];

            Duration duration = Timed.timed(() -> {
                try {
                    if (last instanceof SoundResultEntity) {
                        Sound sound = ((SoundResultEntity) last).getSound();
                        result[0] = DitFastFourierTransform.transform(sound);
                    } else {
                        Spectrum1d spectrum
                                = ((SoundSpectrumResultEntity) last).getSpectrum();
                        result[0] = DitFastFourierTransform.transform(spectrum);
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
            Logger log = LoggerFactory.getLogger(SoundDitFftRequest.class);
            log.info("Pure 1D FFT executed in " + duration);

            results.put(SOUND_SPECTRUM_ENTRY, new SoundSpectrumResultEntity(result[0]));
        }

        @Override
        public OperationSpecification getSpecification() {
            return OperationSpecification.SOUND_DIT_FFT;
        }
    }

    public static class SoundInverseDitFftRequest extends OperationRequest {

        @Override
        protected void execute(Map<String, ResultEntity> results,
                               ResultEntity last) throws Exception {
            Spectrum1d spectrum = ((SoundSpectrumResultEntity) last).getSpectrum();
            Sound[] sound = new Sound[1];

            Duration duration = Timed.timed(() ->
                    sound[0] = DitFastFourierTransform.inverse(spectrum)
            );
            Logger log = LoggerFactory.getLogger(SoundInverseDitFftRequest.class);
            log.info("Pure 1D IFFT executed in " + duration);

            results.put(SOUND_ENTRY, new SoundResultEntity(sound[0]));
        }

        @Override
        public OperationSpecification getSpecification() {
            return OperationSpecification.SOUND_INVERSE_DIT_FFT;
        }
    }

    public static class CepstrumRequest extends OperationRequest {

        @Override
        protected void execute(Map<String, ResultEntity> results,
                               ResultEntity last) throws IOException {
            Sound sound = ((SoundResultEntity) last).getSound();
            Spectrum1d result = cepstrum().apply(sound);
            results.put("cepstrum", new SoundSpectrumResultEntity(result));
        }

        @Override
        public OperationSpecification getSpecification() {
            return OperationSpecification.CEPSTRUM;
        }

    }

    public static class BasicFrequencyCepstrumRequest extends OperationRequest {

        private int windowLength;

        @Override
        protected void execute(Map<String, ResultEntity> results,
                               ResultEntity last) throws IOException {
            Sound sound = ((SoundResultEntity) last).getSound();
            List<OptionalDouble> resultList = windowed(windowLength).apply(sound)
                    .map(findByCepstrum())
                    .collect(Collectors.toList());
            for (int i = 0; i < resultList.size(); i++) {
                double result = resultList.get(i).orElse(Double.NaN);
                results.put("basic frequency " + i, new DoubleResultEntity(result));
            }
        }

        @Override
        public OperationSpecification getSpecification() {
            return OperationSpecification.BASIC_FREQUENCY_CEPSTRUM;
        }

        public int getWindowLength() {
            return windowLength;
        }
    }

    public static class ApproximateCepstrumRequest extends OperationRequest {

        private int windowLength;

        @Override
        protected void execute(Map<String, ResultEntity> results,
                               ResultEntity last) throws IOException {
            Sound sound = ((SoundResultEntity) last).getSound();

            ToDoubleFunction<Sound> frequencyFind = arg ->
                    findByCepstrum().apply(arg).orElseThrow(() ->
                            new IllegalStateException("Couldn't find a basic frequency")
                    );

            int[] values = windowed(windowLength).apply(sound)
                    .map(approximateSine(frequencyFind))
                    .flatMapToInt(Sound::values)
                    .toArray();

            Sound result = new BufferSound(values, sound.getSamplingTime());
            results.put(SOUND_ENTRY, new SoundResultEntity(result));
        }

        @Override
        public OperationSpecification getSpecification() {
            return OperationSpecification.APPROXIMATE_CEPSTRUM;
        }

        public int getWindowLength() {
            return windowLength;
        }
    }

}
