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
import pl.lodz.p.michalsosn.rest.OperationStatusAttachment;

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
                    currentOperation.getSpecification().getType(),
                    currentOperation.getId()
            ));
            currentOperation = currentOperation.getChild();
        }
        return operationIds;
    }

    public OperationStatusAttachment<OperationRequest> retrieveRequest(
            String username, String processName, long operationId
    ) {
        ProcessEntity process = processRepository
                .findByAccountUsernameAndName(username, processName).get();
        OperationEntity operationEntity = operationRepository
                .findByIdAndProcess(operationId, process).get();

        OperationRequest operationRequest = operationEntity.dentitize();

        return new OperationStatusAttachment<>(
                operationEntity.isDone(),
                operationEntity.isFailed(),
                operationEntity.getSpecification().getType(),
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

        // We need to ensure that updates will be performed in this sequence
        // because of unique(child) and foreign key constraints
        // 1 -> 2    1 -> 2      1 -> 4    1 -> 4
        // 2 -> 3 => 2 -> 3  =>  2 -> 3 => 2 -> 3
        //           4 -> ~ new  4 -> ~    4 -> 2

        operationEntity = operationRepository.save(operationEntity);

        if (maybeParentId != null) {
            OperationEntity parentEntity = operationRepository
                    .findByIdAndProcess(maybeParentId, process).get();
            OperationEntity parentsChild = parentEntity.getChild();
            linkChild(parentEntity, operationEntity);
            operationRepository.setChild(parentEntity, operationEntity);
            linkChild(operationEntity, parentsChild);
            operationRepository.setChild(operationEntity, parentsChild);
        } else {
            linkChild(operationEntity, process.getOperation());
            process.setOperation(operationEntity);
            processRepository.setInitialOperation(process, operationEntity);
        }

        long newId = operationEntity.getId();
        log.info("Request {} created in parent {}, process {} by {}",
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
                process, applicationContext, username
        );

        // We need to ensure that updates will be performed in this sequence
        // because of unique(child) and foreign key constraints
        // 1 -> 2    1 -> 2    1 -> 2      1 -> 5    1 -> 5
        // 2 -> 3 => 2 -> ~ => 2 -> ~  =>  2 -> ~ => 2 deleted
        // 3 -> 4    3 -> 4    3 -> 4      3 -> 4    3 -> 4
        //                     5 -> 3 new  5 -> 3    5 -> 3

        OperationEntity previousChild = previousEntity.getChild();
        previousEntity.setChild(null);
        operationRepository.clearChild(previousEntity);

        linkChild(operationEntity, previousChild);
        operationEntity = operationRepository.save(operationEntity);
        operationRepository.setChild(operationEntity, previousChild);

        OperationEntity previousParent = previousEntity.getParent();
        if (previousParent != null) {
            linkChild(previousParent, operationEntity);
            previousEntity.setParent(null);
            operationRepository.setChild(previousParent, operationEntity);
        } else {
            process.setOperation(operationEntity);
            processRepository.setInitialOperation(process, operationEntity);
        }

        operationRepository.delete(previousEntity);

        long newId = operationEntity.getId();

        log.info("Request {} replaced with {} in process {} by {}",
                operationId, newId, processName, username
        );

        return newId;
    }

    public Long deleteOperation(
            String username, String processName, long operationId
    ) {
        ProcessEntity process = processRepository
                .findByAccountUsernameAndName(username, processName).get();
        OperationEntity operationEntity = operationRepository
                .findByIdAndProcess(operationId, process).get();

        // We need to ensure that updates will be performed in this sequence
        // because of unique(child) and foreign key constraints
        // 1 -> 2    1 -> 2    1 -> 3    1 -> 3
        // 2 -> 3 => 2 -> ~ => 2 -> ~ => 2 deleted
        // 3 -> 4    3 -> 4    3 -> 4    3 -> 4

        OperationEntity child = operationEntity.getChild();
        operationEntity.setChild(null);
        operationRepository.clearChild(operationEntity);

        OperationEntity parent = operationEntity.getParent();
        if (parent != null) {
            linkChild(parent, child);
            operationEntity.setParent(null);
            operationRepository.setChild(parent, child);
        } else {
            process.setOperation(child);
        }

//        child = operationRepository.save(child);
        operationRepository.delete(operationEntity);

        log.info("Operation {} in process {} deleted by {}.",
                operationId, processName, username
        );
        return child != null ? child.getId() : null;
    }

    private static void linkChild(OperationEntity parent,
                                  OperationEntity child) {
        parent.setChild(child);
        if (child != null) {
            child.setParent(parent);
        }
    }

    public void submitOperation(long operationId) {
        OperationEntity entity = operationRepository.findOne(operationId);

        OperationEntity parent = entity.getParent();
        Long parentId = null;
        if (parent != null) {
            parentId = parent.getId();
            if (!parent.isDone()) {
                log.info(
                    "Submit interrupted because parent {} has not yet finished "
                        + " and it will submit us again when done", parentId
                );
                return;
            }
        }

        Future<OperationEntity> oldFuture =
                asyncService.getSubmittedOperations().get(operationId);
        if (oldFuture != null) {
            oldFuture.cancel(true);
            log.info("Cancelled old future {} for {}", oldFuture, operationId);
        }

        entity.setDone(false);
        entity.setFailed(false);
//        operationRepository.setStatus(entity, false, false);
        try {
            Future<OperationEntity> future = asyncService.executeRequest(
                    operationId, parentId
            );
            asyncService.getSubmittedOperations().put(operationId, future);
            log.info("Submitted operation {}", operationId);
        } catch (Exception e) {
            throw new IllegalStateException("It should never happen", e);
        }
    }

}
