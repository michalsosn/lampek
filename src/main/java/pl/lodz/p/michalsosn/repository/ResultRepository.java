package pl.lodz.p.michalsosn.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import pl.lodz.p.michalsosn.entities.OperationEntity;
import pl.lodz.p.michalsosn.entities.ResultEntity;

import java.util.Optional;

/**
 * @author Michał Sośnicki
 */
@Transactional(propagation = Propagation.MANDATORY)
public interface ResultRepository extends CrudRepository<ResultEntity, Long> {
    Optional<ResultEntity> findByIdAndOperation(
            long id, OperationEntity operationEntity
    );
}
