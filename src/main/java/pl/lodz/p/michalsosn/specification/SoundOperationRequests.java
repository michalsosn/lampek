package pl.lodz.p.michalsosn.specification;

import com.fasterxml.jackson.annotation.JsonIgnore;
import pl.lodz.p.michalsosn.domain.sound.TimeRange;
import pl.lodz.p.michalsosn.domain.sound.signal.Signal;
import pl.lodz.p.michalsosn.domain.sound.sound.BufferSound;
import pl.lodz.p.michalsosn.domain.sound.sound.Sound;
import pl.lodz.p.michalsosn.domain.sound.transform.Correlations;
import pl.lodz.p.michalsosn.domain.sound.transform.Generators;
import pl.lodz.p.michalsosn.entities.ResultEntity;
import pl.lodz.p.michalsosn.entities.ResultEntity.DoubleResultEntity;
import pl.lodz.p.michalsosn.entities.ResultEntity.SignalResultEntity;
import pl.lodz.p.michalsosn.entities.SoundEntity;
import pl.lodz.p.michalsosn.io.SoundIO;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.OptionalDouble;
import java.util.function.ToDoubleFunction;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

import static pl.lodz.p.michalsosn.domain.Lift.lift;
import static pl.lodz.p.michalsosn.domain.sound.transform.BasicFrequencyAnalysis.*;
import static pl.lodz.p.michalsosn.domain.sound.transform.SampleOps.*;
import static pl.lodz.p.michalsosn.entities.ResultEntity.SoundResultEntity;

/**
 * @author Michał Sośnicki
 */
public final class SoundOperationRequests {

    private static final String SOUND_ENTRY = "sound";
    private static final String SIGNAL_ENTRY = "signal";

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

    public static class GenerateSineSoundlRequest extends OperationRequest {

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
            transformSound(results, last, shorten(skip, take));
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
            transformSound(results, last, shortenToPowerOfTwo());
        }

        @Override
        public OperationSpecification getSpecification() {
            return OperationSpecification.SHORTEN_TO_POWER_OF_TWO;
        }

    }

    public static class CyclicAutocorrelationRequest extends OperationRequest {

        @Override
        protected void execute(Map<String, ResultEntity> results,
                               ResultEntity last) throws IOException {
            Sound sound = ((SoundResultEntity) last).getSound();
            Signal result = Correlations.autocorrelateCyclic().apply(sound);
            results.put(SIGNAL_ENTRY, new SignalResultEntity(result));
        }

        @Override
        public OperationSpecification getSpecification() {
            return OperationSpecification.CYCLIC_AUTOCORRELATION;
        }
    }

    public static class LinearAutocorrelationRequest extends OperationRequest {

        @Override
        protected void execute(Map<String, ResultEntity> results,
                               ResultEntity last) throws IOException {
            Sound sound = ((SoundResultEntity) last).getSound();
            Signal result = Correlations.autocorrelateLinear().apply(sound);
            results.put(SIGNAL_ENTRY, new SignalResultEntity(result));
        }

        @Override
        public OperationSpecification getSpecification() {
            return OperationSpecification.LINEAR_AUTOCORRELATION;
        }
    }

    public static class BasicFrequencyAutocorrelationRequest extends OperationRequest {

        private double threshold;
        private int windowLength;

        @Override
        protected void execute(Map<String, ResultEntity> results,
                               ResultEntity last) throws IOException {
            Sound sound = ((SoundResultEntity) last).getSound();
            List<OptionalDouble> resultList = windowed(windowLength).apply(sound)
                    .map(findByAutocorrelation(threshold))
                    .collect(Collectors.toList());
            for (int i = 0; i < resultList.size(); i++) {
                double result = resultList.get(i).orElse(Double.NaN);
                results.put("basic frequency " + i, new DoubleResultEntity(result));
            }
        }

        @Override
        public OperationSpecification getSpecification() {
            return OperationSpecification.BASIC_FREQUENCY_AUTOCORRELATION;
        }

        public double getThreshold() {
            return threshold;
        }

        public int getWindowLength() {
            return windowLength;
        }
    }

    public static class ApproximateAutocorrelationRequest extends OperationRequest {

        private double threshold;
        private int windowLength;

        @Override
        protected void execute(Map<String, ResultEntity> results,
                               ResultEntity last) throws IOException {
            Sound sound = ((SoundResultEntity) last).getSound();

            ToDoubleFunction<Sound> frequencyFind = arg ->
                    findByAutocorrelation(threshold).apply(arg).orElseThrow(() ->
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
            return OperationSpecification.APPROXIMATE_AUTOCORRELATION;
        }

        public double getThreshold() {
            return threshold;
        }

        public int getWindowLength() {
            return windowLength;
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
}
