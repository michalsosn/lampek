package pl.lodz.p.michalsosn.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.lodz.p.michalsosn.entities.OperationEntity;
import pl.lodz.p.michalsosn.entities.ProcessEntity;
import pl.lodz.p.michalsosn.entities.specification.OperationRequest;
import pl.lodz.p.michalsosn.repository.OperationRepository;
import pl.lodz.p.michalsosn.repository.ProcessRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

/**
 * @author Michał Sośnicki
 */
@Service
@Transactional
public class OperationService {

    private final Logger log = LoggerFactory.getLogger(OperationService.class);

    @Autowired
    private ApplicationContext applicationContext;
    @Autowired
    private ProcessRepository processRepository;
    @Autowired
    private OperationRepository operationRepository;
    @Autowired
    private AsyncService asyncService;

    public List<OperationStatusAttachment<Long>> listOperationIds(
            String username, String processName
    ) {
        ProcessEntity process = processRepository
                .findByAccountUsernameAndName(username, processName).get();

        List<OperationStatusAttachment<Long>> operationIds = new ArrayList<>();

        OperationEntity currentOperation = process.getOperation();
        while (currentOperation != null) {
            operationIds.add(new OperationStatusAttachment<>(
                    currentOperation.isDone(),
                    currentOperation.isFailed(),
                    currentOperation.getId()
            ));
            currentOperation = currentOperation.getChild();
        }
        return operationIds;
    }

    public OperationStatusAttachment<OperationRequest> retrieveRequest(
            String username, String processName, long operationId
    ) {
        OperationEntity operationEntity
                = findEntity(username, processName, operationId);

        OperationRequest operationRequest = operationEntity.dentitize();

        return new OperationStatusAttachment<>(
                operationEntity.isDone(),
                operationEntity.isFailed(),
                operationRequest
        );
    }

    public long acceptRequest(
            String username, String processName, Long maybeParentId,
            OperationRequest operationRequest
    ) {
        ProcessEntity process = processRepository
                .findByAccountUsernameAndName(username, processName).get();

        OperationEntity operationEntity = operationRequest.entitize(
                process, applicationContext, username
        );

        operationEntity = operationRepository.save(operationEntity);

        if (maybeParentId != null) {
            OperationEntity parentEntity = operationRepository
                    .findByIdAndProcess(maybeParentId, process).get();
            parentEntity.setChild(operationEntity);
        } else {
            if (process.getOperation() != null) {
                throw new IllegalStateException(
                        "Process already has an initial operation."
                );
            }
            process.setOperation(operationEntity);
        }

        long newId = operationEntity.getId();
        log.info("request {} created for parent {}, process {} by {}",
                newId, maybeParentId, processName, username
        );
        return newId;
    }

    public long replaceOperation(
            String username, String processName, long operationId,
            OperationRequest operationRequest
    ) {
        ProcessEntity process = processRepository
                .findByAccountUsernameAndName(username, processName).get();
        OperationEntity previousEntity = operationRepository
                .findByIdAndProcess(operationId, process).get();

        OperationEntity operationEntity = operationRequest.entitize(
                previousEntity.getProcess(),
                applicationContext, username
        );

        operationEntity.setChild(previousEntity.getChild());
        OperationEntity previousParent = previousEntity.getParent();
        if (previousParent != null) {
            previousParent.setChild(operationEntity);
        } else {
            process.setOperation(operationEntity);
        }

        operationRepository.delete(previousEntity);
        operationEntity = operationRepository.save(operationEntity);

        long newId = operationEntity.getId();

        log.info("request {} replaced with {} for process {} by {}",
                operationId, newId, processName, username
        );

        return newId;
    }

    public void submitOperation(long operationId) {
        OperationEntity entity = operationRepository.findOne(operationId);
        OperationRequest operationRequest = entity.dentitize();

        OperationEntity parent = entity.getParent();
        if (parent != null && parent.isFailed()) {
            entity.setFailed(true);
            log.info("Submit interrupted because parent has failed");
            return;
        }
        if (parent != null && !parent.isDone()) {
            log.info("Submit interrupted because parent has not yet finished");
            return; // parent will submit this operation after it finishes
        }

        Future<OperationEntity> oldFuture =
                asyncService.getSubmittedOperations().get(operationId);
        if (oldFuture != null) {
            oldFuture.cancel(true);
            log.info("Cancelled old future {} for {}", oldFuture, operationId);
        }

        entity.setDone(false);
        entity.setFailed(false);
        try {
            Future<OperationEntity> future = asyncService.executeRequest(
                    operationId, parent, operationRequest
            );
            asyncService.getSubmittedOperations().put(operationId, future);
            log.info("Submitted operation {}", operationId);
        } catch (Exception e) {
            throw new IllegalStateException("It should never happen", e);
        }
    }

    public void deleteOperation(
            String username, String processName, long operationId
    ) {
        OperationEntity operationEntity
                = findEntity(username, processName, operationId);
        operationRepository.delete(operationEntity);
        log.info("Operation {} in process {} deleted by {}.",
                operationId, processName, username
        );
    }

    private OperationEntity findEntity(String username, String processName,
                                       long operationId) {
        ProcessEntity process = processRepository
                .findByAccountUsernameAndName(username, processName).get();
        return operationRepository
                .findByIdAndProcess(operationId, process).get();
    }

    public static class OperationStatusAttachment<T> {
        private final boolean done;
        private final boolean failed;
        private final T payload;

        public OperationStatusAttachment(boolean done, boolean failed,
                                         T payload) {
            this.done = done;
            this.failed = failed;
            this.payload = payload;
        }

        public boolean isDone() {
            return done;
        }

        public boolean isFailed() {
            return failed;
        }

        public T getPayload() {
            return payload;
        }
    }

}
