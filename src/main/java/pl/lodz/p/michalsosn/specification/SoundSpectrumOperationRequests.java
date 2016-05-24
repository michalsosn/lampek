package pl.lodz.p.michalsosn.specification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.lodz.p.michalsosn.domain.sound.TimeRange;
import pl.lodz.p.michalsosn.domain.sound.signal.Signal;
import pl.lodz.p.michalsosn.domain.sound.sound.BufferSound;
import pl.lodz.p.michalsosn.domain.sound.sound.Sound;
import pl.lodz.p.michalsosn.domain.sound.spectrum.Spectrum1d;
import pl.lodz.p.michalsosn.domain.sound.transform.*;
import pl.lodz.p.michalsosn.entities.ResultEntity;
import pl.lodz.p.michalsosn.entities.ResultEntity.NoteSequenceResultEntity;
import pl.lodz.p.michalsosn.entities.ResultEntity.SignalResultEntity;
import pl.lodz.p.michalsosn.entities.ResultEntity.SoundSpectrumResultEntity;
import pl.lodz.p.michalsosn.util.Timed;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.function.DoubleFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

import static pl.lodz.p.michalsosn.domain.sound.transform.BasicFrequencyAnalysis.*;
import static pl.lodz.p.michalsosn.entities.ResultEntity.SoundResultEntity;

/**
 * @author Michał Sośnicki
 */
public final class SoundSpectrumOperationRequests {

    private static final String SOUND_ENTRY = "sound";
    private static final String SOUND_SPECTRUM_ENTRY = "sound spectrum";
    private static final String SIGNAL_ENTRY = "signal";
    private static final String NOTE_SEQUENCE_ENTRY = "notes";
    private static final String FILTER_ENTRY = "filter";

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
            Signal result = cepstrum().apply(sound);
            results.put(SIGNAL_ENTRY, new SignalResultEntity(result));
        }

        @Override
        public OperationSpecification getSpecification() {
            return OperationSpecification.CEPSTRUM;
        }

    }

    public static class BasicFrequencyRequest extends OperationRequest {

        public enum Method {
            AUTOCORRELATION(BasicFrequencyAnalysis::findByAutocorrelation),
            CEPSTRUM(BasicFrequencyAnalysis::findByCepstrum);

            private final DoubleFunction<Function<Sound, Note>> function;

            Method(DoubleFunction<Function<Sound, Note>> function) {
                this.function = function;
            }
        }

        private Method method;
        private Windows.WindowType window;
        private double threshold;
        private int windowLength;

        @Override
        protected void execute(Map<String, ResultEntity> results,
                               ResultEntity last) throws IOException {
            Sound sound = ((SoundResultEntity) last).getSound();

            List<Note> notes = joinNotes(
                    Windows.sliding(windowLength).apply(sound)
                            .map(window.getSoundFunction())
                            .map(method.function.apply(threshold))
                            .collect(Collectors.toList())
            );
            Sound approximation = approximateSine(notes);
            Note[] noteSequence = notes.stream().toArray(Note[]::new);

            results.put(SOUND_ENTRY, new SoundResultEntity(approximation));
            results.put(NOTE_SEQUENCE_ENTRY, new NoteSequenceResultEntity(noteSequence));
        }

        @Override
        public OperationSpecification getSpecification() {
            return OperationSpecification.BASIC_FREQUENCY;
        }

        public Method getMethod() {
            return method;
        }

        public Windows.WindowType getWindow() {
            return window;
        }

        public double getThreshold() {
            return threshold;
        }

        public int getWindowLength() {
            return windowLength;
        }
    }

    public static class GenerateSincRequest extends OperationRequest {

        private double samplingFrequency;
        private double cutoffFrequency;
        private int length;

        @Override
        protected void execute(Map<String, ResultEntity> results,
                               ResultEntity last) throws IOException {
            Signal signal = Filters.sincResponse(
                    TimeRange.ofFrequency(samplingFrequency),
                    TimeRange.ofFrequency(cutoffFrequency),
                    length
            );
            results.put(SIGNAL_ENTRY, new SignalResultEntity(signal));
        }

        @Override
        public OperationSpecification getSpecification() {
            return OperationSpecification.GENERATE_SINC;
        }

        public double getSamplingFrequency() {
            return samplingFrequency;
        }

        public double getCutoffFrequency() {
            return cutoffFrequency;
        }

        public int getLength() {
            return length;
        }
    }

    public static class FilterInTimeRequest extends OperationRequest {

        private double cutoffFrequency;
        private int filterLength;
        private Windows.WindowType filterWindow;

        @Override
        protected void execute(Map<String, ResultEntity> results,
                               ResultEntity last) throws IOException {
            Sound sound = ((SoundResultEntity) last).getSound();

            Signal filter = Filters.sincResponse(
                    sound.getSamplingTime(),
                    TimeRange.ofFrequency(cutoffFrequency),
                    filterLength
            );

            final Signal convolution
                    = Convolutions.convolveLinearTime(filter).apply(sound);

            final BufferSound result = BufferSound.of(convolution);

            results.put(FILTER_ENTRY, new SignalResultEntity(filter));
            results.put(SOUND_ENTRY, new SoundResultEntity(result));
        }

        @Override
        public OperationSpecification getSpecification() {
            return OperationSpecification.FILTER_IN_TIME;
        }

        public double getCutoffFrequency() {
            return cutoffFrequency;
        }

        public int getFilterLength() {
            return filterLength;
        }

        public Windows.WindowType getFilterWindow() {
            return filterWindow;
        }
    }

    public static class FilterOverlapAddRequest extends OperationRequest {

        private double cutoffFrequency;
        private int filterLength;
        private Windows.WindowType filterWindow;

        private int windowLength;
        private int hopSize;
        private Windows.WindowType windowWindow;

        @Override
        protected void execute(Map<String, ResultEntity> results,
                               ResultEntity last) throws IOException {
            Sound sound = ((SoundResultEntity) last).getSound();

            Signal filter = Filters.sincResponse(
                    sound.getSamplingTime(),
                    TimeRange.ofFrequency(cutoffFrequency),
                    filterLength
            );

            final Signal convolution = Convolutions.convolveLinearOverlapAdd(
                    windowWindow.getSignalFunction(), windowLength, hopSize, filter
            ).apply(sound);

            final BufferSound result = BufferSound.of(convolution);

            results.put(FILTER_ENTRY, new SignalResultEntity(filter));
            results.put(SOUND_ENTRY, new SoundResultEntity(result));
        }

        @Override
        public OperationSpecification getSpecification() {
            return OperationSpecification.FILTER_OVERLAP_ADD;
        }

        public double getCutoffFrequency() {
            return cutoffFrequency;
        }

        public int getFilterLength() {
            return filterLength;
        }

        public Windows.WindowType getFilterWindow() {
            return filterWindow;
        }

        public int getWindowLength() {
            return windowLength;
        }

        public int getHopSize() {
            return hopSize;
        }

        public Windows.WindowType getWindowWindow() {
            return windowWindow;
        }
    }

}
