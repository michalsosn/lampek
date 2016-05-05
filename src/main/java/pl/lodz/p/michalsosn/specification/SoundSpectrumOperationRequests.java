package pl.lodz.p.michalsosn.specification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.lodz.p.michalsosn.domain.sound.signal.Signal;
import pl.lodz.p.michalsosn.domain.sound.sound.Sound;
import pl.lodz.p.michalsosn.domain.sound.spectrum.Spectrum1d;
import pl.lodz.p.michalsosn.domain.sound.transform.DitFastFourierTransform;
import pl.lodz.p.michalsosn.domain.sound.transform.Note;
import pl.lodz.p.michalsosn.entities.ResultEntity;
import pl.lodz.p.michalsosn.entities.ResultEntity.NoteSequenceResultEntity;
import pl.lodz.p.michalsosn.entities.ResultEntity.SignalResultEntity;
import pl.lodz.p.michalsosn.entities.ResultEntity.SoundSpectrumResultEntity;
import pl.lodz.p.michalsosn.util.Timed;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static pl.lodz.p.michalsosn.domain.sound.transform.BasicFrequencyAnalysis.*;
import static pl.lodz.p.michalsosn.domain.sound.transform.Windows.hann;
import static pl.lodz.p.michalsosn.entities.ResultEntity.SoundResultEntity;

/**
 * @author Michał Sośnicki
 */
public final class SoundSpectrumOperationRequests {

    private static final String SOUND_ENTRY = "sound";
    private static final String SOUND_SPECTRUM_ENTRY = "sound spectrum";
    private static final String SIGNAL_ENTRY = "signal";
    private static final String NOTE_SEQUENCE_ENTRY = "notes";

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

    public static class BasicFrequencyCepstrumRequest extends OperationRequest {

        private boolean useHanningWindow;
        private double threshold;
        private int windowLength;

        @Override
        protected void execute(Map<String, ResultEntity> results,
                               ResultEntity last) throws IOException {
            Sound sound = ((SoundResultEntity) last).getSound();

            List<Note> notes = joinNotes(
                    windowed(windowLength).apply(sound)
                            .map(useHanningWindow ? hann() : Function.identity())
                            .map(findByCepstrum(threshold))
                            .collect(Collectors.toList())
            );
            Sound approximation = approximateSine(notes);
            Note[] noteSequence = notes.stream().toArray(Note[]::new);

            results.put(SOUND_ENTRY, new SoundResultEntity(approximation));
            results.put(NOTE_SEQUENCE_ENTRY, new NoteSequenceResultEntity(noteSequence));
        }

        @Override
        public OperationSpecification getSpecification() {
            return OperationSpecification.BASIC_FREQUENCY_CEPSTRUM;
        }

        public boolean isUseHanningWindow() {
            return useHanningWindow;
        }

        public double getThreshold() {
            return threshold;
        }

        public int getWindowLength() {
            return windowLength;
        }
    }

}
