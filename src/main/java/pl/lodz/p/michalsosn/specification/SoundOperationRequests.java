package pl.lodz.p.michalsosn.specification;

import com.fasterxml.jackson.annotation.JsonIgnore;
import pl.lodz.p.michalsosn.domain.sound.TimeRange;
import pl.lodz.p.michalsosn.domain.sound.filter.Filter;
import pl.lodz.p.michalsosn.domain.sound.signal.Signal;
import pl.lodz.p.michalsosn.domain.sound.sound.Sound;
import pl.lodz.p.michalsosn.domain.sound.transform.Correlations;
import pl.lodz.p.michalsosn.domain.sound.transform.Extensions;
import pl.lodz.p.michalsosn.domain.sound.transform.Generators;
import pl.lodz.p.michalsosn.domain.sound.transform.Windows;
import pl.lodz.p.michalsosn.entities.ResultEntity;
import pl.lodz.p.michalsosn.entities.ResultEntity.SignalResultEntity;
import pl.lodz.p.michalsosn.entities.ResultEntity.SoundFilterResultEntity;
import pl.lodz.p.michalsosn.entities.SoundEntity;
import pl.lodz.p.michalsosn.io.SoundIO;

import java.io.IOException;
import java.util.Map;
import java.util.function.UnaryOperator;

import static pl.lodz.p.michalsosn.domain.Lift.lift;
import static pl.lodz.p.michalsosn.domain.sound.transform.Extensions.*;
import static pl.lodz.p.michalsosn.domain.sound.transform.SampleOps.clipAbove;
import static pl.lodz.p.michalsosn.domain.sound.transform.SampleOps.scaleValue;
import static pl.lodz.p.michalsosn.entities.ResultEntity.SoundResultEntity;

/**
 * @author Michał Sośnicki
 */
public final class SoundOperationRequests {

    private static final String SOUND_ENTRY = "sound";
    private static final String SIGNAL_ENTRY = "signal";
    private static final String SOUND_FILTER_ENTRY = "filter";
    private static final String NOTE_SEQUENCE_ENTRY = "notes";

    private SoundOperationRequests() {
    }

    public static class LoadSoundRequest extends OperationRequest {
        private String sound;
        @JsonIgnore
        private SoundEntity soundEntity;

        @Override
        protected void execute(Map<String, ResultEntity> results,
                               ResultEntity last) throws IOException {
            byte[] soundData = soundEntity.getData();
            Sound sound = SoundIO.readSound(soundData);
            results.put(SOUND_ENTRY, new SoundResultEntity(sound));
        }

        @Override
        public OperationSpecification getSpecification() {
            return OperationSpecification.LOAD_SOUND;
        }

        public String getSound() {
            return sound;
        }
    }

    public static class GenerateSineSoundRequest extends OperationRequest {

        private int amplitude;
        private double basicFrequency;
        private double startPhase;
        private int length;
        private double samplingFrequency;

        @Override
        protected void execute(Map<String, ResultEntity> results,
                               ResultEntity last) throws IOException {
            Sound sound = Generators.sine(
                    amplitude, basicFrequency, startPhase, length,
                    TimeRange.ofFrequency(samplingFrequency)
            );
            results.put(SOUND_ENTRY, new SoundResultEntity(sound));
        }

        @Override
        public OperationSpecification getSpecification() {
            return OperationSpecification.GENERATE_SINE_SOUND;
        }

        public int getAmplitude() {
            return amplitude;
        }

        public double getBasicFrequency() {
            return basicFrequency;
        }

        public double getStartPhase() {
            return startPhase;
        }

        public int getLength() {
            return length;
        }

        public double getSamplingFrequency() {
            return samplingFrequency;
        }
    }

    public static class ScaleValueRequest extends OperationRequest {
        private double change;

        @Override
        protected void execute(Map<String, ResultEntity> results,
                               ResultEntity last) throws IOException {
            transformSound(results, last, lift(scaleValue(change)));
        }

        @Override
        public OperationSpecification getSpecification() {
            return OperationSpecification.SCALE_VALUE;
        }

        public double getChange() {
            return change;
        }
    }

    public static class ClipAboveRequest extends OperationRequest {
        private int threshold;

        @Override
        protected void execute(Map<String, ResultEntity> results,
                               ResultEntity last) throws IOException {
            transformSound(results, last, lift(clipAbove(threshold)));
        }

        @Override
        public OperationSpecification getSpecification() {
            return OperationSpecification.CLIP_ABOVE;
        }

        public int getThreshold() {
            return threshold;
        }
    }

    public static class ShortenSoundRequest extends OperationRequest {
        private int skip;
        private int take;

        @Override
        protected void execute(Map<String, ResultEntity> results,
                               ResultEntity last) throws IOException {
            if (last instanceof SoundResultEntity) {
                transformSound(results, last, shortenSound(skip, take));
            } else if (last instanceof SignalResultEntity) {
                transformSignal(results, last, shortenSignal(skip, take));
            } else {
                transformFilter(results, last, shortenFilter(skip, take));
            }
        }

        @Override
        public OperationSpecification getSpecification() {
            return OperationSpecification.SHORTEN_SOUND;
        }

        public int getSkip() {
            return skip;
        }

        public int getTake() {
            return take;
        }
    }

    public static class ShortenToPowerOfTwoRequest extends OperationRequest {

        @Override
        protected void execute(Map<String, ResultEntity> results,
                               ResultEntity last) throws IOException {
            if (last instanceof SoundResultEntity) {
                transformSound(results, last,
                               shortenToPowerOfTwo(Extensions::shortenSound));
            } else if (last instanceof SignalResultEntity) {
                transformSignal(results, last,
                                shortenToPowerOfTwo(Extensions::shortenSignal));
            } else {
                transformFilter(results, last,
                                shortenToPowerOfTwo(Extensions::shortenFilter));
            }
        }

        @Override
        public OperationSpecification getSpecification() {
            return OperationSpecification.SHORTEN_TO_POWER_OF_TWO;
        }
    }

    public static class PadWithZeroRequest extends OperationRequest {

        private int newLength;

        @Override
        protected void execute(Map<String, ResultEntity> results,
                               ResultEntity last) throws IOException {
            if (last instanceof SoundResultEntity) {
                transformSound(results, last, zeroPadSound(newLength));
            } else if (last instanceof SignalResultEntity) {
                transformSignal(results, last, zeroPadSignal(newLength));
            } else {
                transformFilter(results, last, zeroPadFilter(newLength));
            }
        }

        @Override
        public OperationSpecification getSpecification() {
            return OperationSpecification.PAD_WITH_ZERO;
        }

        public int getNewLength() {
            return newLength;
        }

    }

    public static class PadWithZeroToPowerOfTwoRequest extends OperationRequest {

        @Override
        protected void execute(Map<String, ResultEntity> results,
                               ResultEntity last) throws IOException {
            if (last instanceof SoundResultEntity) {
                transformSound(results, last,
                               zeroPadToPowerOfTwo(Extensions::zeroPadSound));
            } else if (last instanceof SignalResultEntity) {
                transformSignal(results, last,
                               zeroPadToPowerOfTwo(Extensions::zeroPadSignal));
            } else {
                transformFilter(results, last,
                               zeroPadToPowerOfTwo(Extensions::zeroPadFilter));
            }
        }

        @Override
        public OperationSpecification getSpecification() {
            return OperationSpecification.PAD_WITH_ZERO_TO_POWER_OF_TWO;
        }
    }

    public static class WindowRequest extends OperationRequest {

        private Windows.WindowType window;

        @Override
        protected void execute(Map<String, ResultEntity> results,
                               ResultEntity last) throws IOException {
            if (last instanceof SoundResultEntity) {
                transformSound(results, last, window.getSoundFunction());
            } else if (last instanceof SignalResultEntity) {
                transformSignal(results, last, window.getSignalFunction());
            } else {
                transformFilter(results, last, window.getFilterFunction());
            }
        }

        @Override
        public OperationSpecification getSpecification() {
            return OperationSpecification.WINDOW;
        }

        public Windows.WindowType getWindow() {
            return window;
        }
    }

    public static class AutocorrelationRequest extends OperationRequest {

        private Correlations.CorrelationType type;

        @Override
        protected void execute(Map<String, ResultEntity> results,
                               ResultEntity last) throws IOException {
            Sound sound = ((SoundResultEntity) last).getSound();
            Signal result = type.getAutocorrelation().apply(sound);
            results.put(SIGNAL_ENTRY, new SignalResultEntity(result));
        }

        @Override
        public OperationSpecification getSpecification() {
            return OperationSpecification.AUTOCORRELATION;
        }

        public Correlations.CorrelationType getType() {
            return type;
        }
    }

    private static void transformSound(
            Map<String, ResultEntity> results, ResultEntity last,
            UnaryOperator<Sound> soundMapper
    ) throws IOException {
        Sound sound = ((SoundResultEntity) last).getSound();
        Sound result = soundMapper.apply(sound);
        results.put(SOUND_ENTRY, new SoundResultEntity(result));
    }

    private static void transformSignal(
            Map<String, ResultEntity> results, ResultEntity last,
            UnaryOperator<Signal> signalMapper
    ) throws IOException {
        Signal signal = ((SignalResultEntity) last).getSignal();
        Signal result = signalMapper.apply(signal);
        results.put(SIGNAL_ENTRY, new SignalResultEntity(result));
    }

    private static void transformFilter(
            Map<String, ResultEntity> results, ResultEntity last,
            UnaryOperator<Filter> signalMapper
    ) throws IOException {
        Filter signal = ((SoundFilterResultEntity) last).getFilter();
        Filter result = signalMapper.apply(signal);
        results.put(SOUND_FILTER_ENTRY, new SoundFilterResultEntity(result));
    }

}
