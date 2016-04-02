package pl.lodz.p.michalsosn.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import pl.lodz.p.michalsosn.entities.OperationEntity;
import pl.lodz.p.michalsosn.entities.specification.OperationRequest;
import pl.lodz.p.michalsosn.repository.OperationRepository;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Future;

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
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Future<OperationEntity> executeRequest(
            long operationId, OperationEntity staleParent,
            OperationRequest staleRequest
    ) throws Exception {
        OperationEntity entity = null;
        try {
            entity = operationRepository.findOne(operationId);

            staleRequest.execute(entity, staleParent);

            log.info("Operation {} executed successfully", operationId);
            return new AsyncResult<>(entity);
        } catch (Exception e) {
            if (entity != null) {
                entity.setFailed(true);
            }
            log.error("Execution of operation {} failed.", operationId, e);
            throw e;
        } finally {
            if (entity != null) {
                entity.setDone(true);
                OperationEntity child = entity.getChild();
                if (child != null) {
                    log.info("Operation {} will submit its child {}",
                            operationId, child.getId());
                    operationService.submitOperation(child.getId());
                }
            }
            submittedOperations.remove(operationId);
        }
    }

}
