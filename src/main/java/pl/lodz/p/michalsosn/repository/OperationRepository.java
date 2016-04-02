package pl.lodz.p.michalsosn.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import pl.lodz.p.michalsosn.entities.OperationEntity;
import pl.lodz.p.michalsosn.entities.ProcessEntity;

import java.util.Optional;

/**
 * @author Michał Sośnicki
 */
@Transactional(propagation = Propagation.MANDATORY)
public interface OperationRepository extends CrudRepository<OperationEntity, Long> {
    Optional<OperationEntity> findByIdAndProcess(
            long id, ProcessEntity processEntity
    );
}
