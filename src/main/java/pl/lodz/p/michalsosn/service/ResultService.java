package pl.lodz.p.michalsosn.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.lodz.p.michalsosn.domain.image.channel.ConstChannel;
import pl.lodz.p.michalsosn.domain.image.channel.GrayImage;
import pl.lodz.p.michalsosn.domain.image.channel.Image;
import pl.lodz.p.michalsosn.domain.image.transform.segmentation.Mask;
import pl.lodz.p.michalsosn.domain.sound.sound.Sound;
import pl.lodz.p.michalsosn.entities.OperationEntity;
import pl.lodz.p.michalsosn.entities.ProcessEntity;
import pl.lodz.p.michalsosn.entities.ResultEntity;
import pl.lodz.p.michalsosn.io.BufferedImageIO;
import pl.lodz.p.michalsosn.io.SoundIO;
import pl.lodz.p.michalsosn.repository.OperationRepository;
import pl.lodz.p.michalsosn.repository.ProcessRepository;
import pl.lodz.p.michalsosn.rest.support.SignalChartPack;
import pl.lodz.p.michalsosn.rest.support.OperationStatusAttachment;
import pl.lodz.p.michalsosn.rest.support.SoundChartPack;
import pl.lodz.p.michalsosn.rest.support.SoundSpectrumChartPack;
import pl.lodz.p.michalsosn.security.OwnerOnly;

import java.io.IOException;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * @author Michał Sośnicki
 */
@Service
@Transactional
@OwnerOnly
public class ResultService {

    @Autowired
    private ProcessRepository processRepository;
    @Autowired
    private OperationRepository operationRepository;

    public OperationStatusAttachment<Map<String, ResultEntity>> listResults(
            String username, String processName, long operationId
    ) {
        ProcessEntity process = processRepository
                .findByAccountUsernameAndName(username, processName).get();
        OperationEntity operation = operationRepository
                .findByIdAndProcess(operationId, process).get();
        // Lazy loading outside transaction fix
        operation.getResults().forEach((s, resultEntity) ->
                resultEntity.getType()
        );
        return new OperationStatusAttachment<>(
                operation.isDone(), operation.isFailed(),
                operation.getSpecification().getType(),
                operation.getResults()
        );
    }

    public byte[] getResultAsPng(String username, String processName,
                                 long operationId, String resultName)
            throws IOException {
        ResultEntity result = findResult(username, processName, operationId, resultName);

        switch (result.getType()) {
            case IMAGE:
                return ((ResultEntity.ImageResultEntity) result).getData();
            case IMAGE_SPECTRUM:
                return ((ResultEntity.ImageSpectrumResultEntity) result)
                        .getPresentationData();
            case IMAGE_MASK:
                Mask mask = ((ResultEntity.ImageMaskResultEntity) result)
                        .getMask();
                Image image = new GrayImage(new ConstChannel(
                        mask.getHeight(), mask.getWidth(), Image.MAX_VALUE
                ));
                Image maskImage = image.map(mask.toOperator());
                return BufferedImageIO.toByteArray(BufferedImageIO.fromImage(maskImage));
            default:
                throw new NoSuchElementException("Result of wrong type");
        }
    }

    public byte[] getResultAsWave(String username, String processName,
                                  long operationId, String resultName)
            throws IOException {
        return getAudioResult(
                username, processName, operationId, resultName, SoundIO.AudioType.WAVE
        );
    }

    public byte[] getResultAsFlac(String username, String processName,
                                  long operationId, String resultName)
            throws IOException {
        return getAudioResult(
                username, processName, operationId, resultName, SoundIO.AudioType.FLAC
        );
    }

    private byte[] getAudioResult(String username, String processName,
                                 long operationId, String resultName,
                                 SoundIO.AudioType type) throws IOException {
        ResultEntity result = findResult(username, processName, operationId, resultName);

        switch (result.getType()) {
            case SOUND:
                Sound sound = ((ResultEntity.SoundResultEntity) result).getSound();
                return SoundIO.writeSound(sound, type);
            default:
                throw new NoSuchElementException("Result of wrong type");
        }
    }

    public Object getResultAsChart(String username, String processName,
                                   long operationId, String resultName,
                                   Double start, Double end)
            throws IOException {
        ResultEntity result = findResult(username, processName, operationId, resultName);

        switch (result.getType()) {
            case SOUND:
                return new SoundChartPack(
                        (ResultEntity.SoundResultEntity) result, start, end
                );
            case SOUND_SPECTRUM:
                return new SoundSpectrumChartPack(
                        (ResultEntity.SoundSpectrumResultEntity) result, start, end
                );
            case SIGNAL:
                return new SignalChartPack(
                        (ResultEntity.SignalResultEntity) result, start, end
                );
            default:
                throw new NoSuchElementException("Result of wrong type");
        }
    }

    private ResultEntity findResult(String username, String processName,
                                    long operationId, String resultName) {
        ProcessEntity process = processRepository
                .findByAccountUsernameAndName(username, processName).get();
        OperationEntity operation = operationRepository
                .findByIdAndProcess(operationId, process).get();
        ResultEntity result = operation.getResults().get(resultName);
        if (result == null)  {
            throw new NoSuchElementException("Result not found");
        }
        return result;
    }


}
