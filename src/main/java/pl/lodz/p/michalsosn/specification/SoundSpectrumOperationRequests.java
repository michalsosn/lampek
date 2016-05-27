package pl.lodz.p.michalsosn.specification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.lodz.p.michalsosn.domain.sound.TimeRange;
import pl.lodz.p.michalsosn.domain.sound.filter.Filter;
import pl.lodz.p.michalsosn.domain.sound.signal.Signal;
import pl.lodz.p.michalsosn.domain.sound.sound.Sound;
import pl.lodz.p.michalsosn.domain.sound.spectrum.Spectrum1d;
import pl.lodz.p.michalsosn.domain.sound.transform.*;
import pl.lodz.p.michalsosn.entities.ResultEntity;
import pl.lodz.p.michalsosn.entities.ResultEntity.NoteSequenceResultEntity;
import pl.lodz.p.michalsosn.entities.ResultEntity.SignalResultEntity;
import pl.lodz.p.michalsosn.entities.ResultEntity.SoundFilterResultEntity;
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
import static pl.lodz.p.michalsosn.domain.sound.transform.Conversions.toSound;
import static pl.lodz.p.michalsosn.domain.sound.transform.Conversions.toSpectrum1d;
import static pl.lodz.p.michalsosn.entities.ResultEntity.SoundResultEntity;

/**
 * @author Michał Sośnicki
 */
public final class SoundSpectrumOperationRequests {

    private static final String SOUND_ENTRY = "sound";
    private static final String SOUND_SPECTRUM_ENTRY = "spectrum";
    private static final String SIGNAL_ENTRY = "signal";
    private static final String NOTE_SEQUENCE_ENTRY = "notes";
    private static final String SOUND_FILTER_ENTRY = "filter";

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
                        result[0] = DitFastFourierTransform.transform(
                                toSpectrum1d(sound)
                        );
                    } else if (last instanceof SignalResultEntity) {
                        Signal signal = ((SignalResultEntity) last).getSignal();
                        result[0] = DitFastFourierTransform.transform(
                                toSpectrum1d(signal)
                        );
                    } else if (last instanceof SoundFilterResultEntity) {
                        Filter filter = ((SoundFilterResultEntity) last).getFilter();
                        result[0] = DitFastFourierTransform.transform(
                                toSpectrum1d(filter)
                        );
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
                    sound[0] = toSound(DitFastFourierTransform.inverse(spectrum))
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
        private boolean causal;

        @Override
        protected void execute(Map<String, ResultEntity> results,
                               ResultEntity last) throws IOException {
            Filter filter = Filters.sincFilter(
                    TimeRange.ofFrequency(samplingFrequency),
                    cutoffFrequency, length, causal
            );
            results.put(SOUND_FILTER_ENTRY, new SoundFilterResultEntity(filter));
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

        public boolean isCausal() {
            return causal;
        }
    }

    public static class ModulateRequest extends OperationRequest {

        private double amplitude;
        private double frequency;

        @Override
        protected void execute(Map<String, ResultEntity> results,
                               ResultEntity last) throws IOException {
            Filter filter = ((SoundFilterResultEntity) last).getFilter();
            Filter result = Filters.modulate(amplitude, frequency).apply(filter);
            results.put(SOUND_FILTER_ENTRY, new SoundFilterResultEntity(result));
        }

        @Override
        public OperationSpecification getSpecification() {
            return OperationSpecification.MODULATE;
        }

        public double getAmplitude() {
            return amplitude;
        }

        public double getFrequency() {
            return frequency;
        }
    }

    public static class FilterInTimeRequest extends OperationRequest {

        private double cutoffFrequency;
        private int filterLength;
        private Windows.WindowType filterWindow;
        private boolean causal;

        @Override
        protected void execute(Map<String, ResultEntity> results,
                               ResultEntity last) throws IOException {
            Sound sound = ((SoundResultEntity) last).getSound();

            Filter filter = filterWindow.getFilterFunction().apply(Filters.sincFilter(
                    sound.getSamplingTime(), cutoffFrequency, filterLength, causal
            ));

            final Signal convolution
                    = Convolutions.convolveLinearTime(filter).apply(sound);

            final Sound result = toSound(convolution);

            results.put(SOUND_FILTER_ENTRY, new SoundFilterResultEntity(filter));
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

        public boolean isCausal() {
            return causal;
        }
    }

    public static class FilterOverlapAddRequest extends OperationRequest {

        private double cutoffFrequency;
        private int filterLength;
        private Windows.WindowType filterWindow;
        private boolean causal;

        private int windowLength;
        private int hopSize;
        private Windows.WindowType windowWindow;

        @Override
        protected void execute(Map<String, ResultEntity> results,
                               ResultEntity last) throws IOException {
            Sound sound = ((SoundResultEntity) last).getSound();

            Filter filter = filterWindow.getFilterFunction().apply(Filters.sincFilter(
                    sound.getSamplingTime(), cutoffFrequency, filterLength, causal
            ));

            final Signal convolution = Convolutions.convolveLinearOverlapAdd(
                    windowWindow.getSignalFunction(), windowLength, hopSize, filter
            ).apply(sound);

            final Sound result = toSound(convolution);

            results.put(SOUND_FILTER_ENTRY, new SoundFilterResultEntity(filter));
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

        public boolean isCausal() {
            return causal;
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

    public static class Equalizer10BandRequest extends OperationRequest {

        private int windowLength;
        private int hopSize;
        private Windows.WindowType windowWindow;

        private int filterLength;
        private Windows.WindowType filterWindow;

        private double amplification0;
        private double amplification1;
        private double amplification2;
        private double amplification3;
        private double amplification4;
        private double amplification5;
        private double amplification6;
        private double amplification7;
        private double amplification8;
        private double amplification9;

        @Override
        protected void execute(Map<String, ResultEntity> results,
                               ResultEntity last) throws IOException {
            final Sound sound = ((SoundResultEntity) last).getSound();

            final double[] amplification = {
                    amplification0, amplification1, amplification2, amplification3,
                    amplification4, amplification5, amplification6, amplification7,
                    amplification8, amplification9
            };

            final Filter[] filterBase = Equalizers.filterBase(
                    sound.getSamplingTime(), filterLength,
                    filterWindow.getFilterFunction(),
                    20, 12, amplification
            );

            final Sound equalized = Equalizers.equalize(
                    filterBase,
                    filter -> Convolutions.convolveLinearOverlapAdd(
                            windowWindow.getSignalFunction(), windowLength,
                            hopSize, filter
                    )
            ).apply(sound);
//            final Sound equalized = toSound(Convolutions.convolveLinearOverlapAdd(
//                    windowWindow.getSignalFunction(), windowLength,
//                    hopSize, filterBase
//            ).apply(sound));

            for (int i = 0; i < filterBase.length; i++) {
                final Spectrum1d spectrum
                        = DitFastFourierTransform.transform(toSpectrum1d(filterBase[i]));
                results.put("Filter - " + i, new SoundSpectrumResultEntity(spectrum));
            }
            results.put(SOUND_ENTRY, new SoundResultEntity(equalized));
        }

        @Override
        public OperationSpecification getSpecification() {
            return OperationSpecification.EQUALIZER_10_BAND;
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

        public int getFilterLength() {
            return filterLength;
        }

        public Windows.WindowType getFilterWindow() {
            return filterWindow;
        }

        public double getAmplification0() {
            return amplification0;
        }

        public double getAmplification1() {
            return amplification1;
        }

        public double getAmplification2() {
            return amplification2;
        }

        public double getAmplification3() {
            return amplification3;
        }

        public double getAmplification4() {
            return amplification4;
        }

        public double getAmplification5() {
            return amplification5;
        }

        public double getAmplification6() {
            return amplification6;
        }

        public double getAmplification7() {
            return amplification7;
        }

        public double getAmplification8() {
            return amplification8;
        }

        public double getAmplification9() {
            return amplification9;
        }
    }

}
