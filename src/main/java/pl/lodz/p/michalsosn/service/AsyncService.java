package pl.lodz.p.michalsosn.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import pl.lodz.p.michalsosn.entities.OperationEntity;
import pl.lodz.p.michalsosn.security.OwnerOnly;
import pl.lodz.p.michalsosn.specification.OperationRequest;
import pl.lodz.p.michalsosn.repository.OperationRepository;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Future;
import java.util.function.IntConsumer;

/**
 * @author Michał Sośnicki
 */
@Service
@Transactional
public class AsyncService {

    private final Logger log = LoggerFactory.getLogger(AsyncService.class);

    @Autowired
    private OperationRepository operationRepository;
    @Autowired
    private OperationService operationService;

    private final ConcurrentMap<Long, Future<OperationEntity>>
            submittedOperations = new ConcurrentHashMap<>();

    public ConcurrentMap<Long, Future<OperationEntity>>
    getSubmittedOperations() {
        return submittedOperations;
    }

    public void cancelOperation(long operationId) {
        Future<OperationEntity> oldFuture =
                submittedOperations.get(operationId);
        if (oldFuture != null) {
            oldFuture.cancel(true);
            log.info("Cancelled future {} for {}", oldFuture, operationId);
        }
    }

    @Async
    @OwnerOnly
    public Future<OperationEntity> executeRequest(
            long operationId, Long parentId
    ) throws Exception {
        log.info("Operation {} started executing", operationId);
        OperationEntity entity = null;
        try {
            entity = operationRepository.findOne(operationId);

            OperationEntity parent = null;
            if (parentId != null) {
                parent = operationRepository.findOne(parentId);
            }

            OperationEntity child = entity.getChild();
            if (child != null) {
                child.setDone(false);
                child.setFailed(false);
                operationRepository.setStatus(child, false, false);
                long childId = child.getId();
                afterCompletion(status -> {
                    log.info("Operation {} will submit its child {}",
                            operationId, childId);
                    operationService.submitOperation(childId);
                });
            }

            OperationRequest request = entity.dentitize();
            entity.getResults().clear();
            Instant before = Instant.now();
            request.execute(entity, parent);
            Instant after = Instant.now();

            entity.setFailed(false);
            entity.setDone(true);
            operationRepository.setStatus(entity, true, false);
            log.info("Operation {} executed successfully in {}",
                    operationId, Duration.between(before, after));
            return new AsyncResult<>(entity);
        } catch (Exception e) {
            if (entity != null) {
                entity.setFailed(true);
                entity.setDone(true);
                operationRepository.setStatus(entity, true, true);
            }
            log.error("Execution of operation {} failed.", operationId, e);
            return new AsyncResult<>(entity);
        } finally {
            submittedOperations.remove(operationId);
        }
    }

    private void afterCompletion(IntConsumer statusConsumer) {
        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronizationAdapter() {
                    @Override
                    public void afterCompletion(int status) {
                        statusConsumer.accept(status);
                    }
                }
        );
    }

}
