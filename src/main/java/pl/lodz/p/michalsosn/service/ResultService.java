package pl.lodz.p.michalsosn.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.lodz.p.michalsosn.entities.OperationEntity;
import pl.lodz.p.michalsosn.entities.ProcessEntity;
import pl.lodz.p.michalsosn.entities.ResultEntity;
import pl.lodz.p.michalsosn.entities.ValueType;
import pl.lodz.p.michalsosn.repository.OperationRepository;
import pl.lodz.p.michalsosn.repository.ProcessRepository;
import pl.lodz.p.michalsosn.rest.OperationStatusAttachment;

import java.io.IOException;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * @author Michał Sośnicki
 */
@Service
@Transactional
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
        ProcessEntity process = processRepository
                .findByAccountUsernameAndName(username, processName).get();
        OperationEntity operation = operationRepository
                .findByIdAndProcess(operationId, process).get();
        ResultEntity result = operation.getResults().get(resultName);
        if (result != null || result.getType() == ValueType.IMAGE) {
            return ((ResultEntity.ImageResultEntity) result).getData();
        } else {
            throw new NoSuchElementException(
                    "Result not found or of wrong type"
            );
        }
    }

}
