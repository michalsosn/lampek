package pl.lodz.p.michalsosn.repository;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
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

    @Modifying
    @Query("UPDATE Operation o SET o.child = NULL WHERE o = ?1")
    void clearChild(OperationEntity operation);

    @Modifying
    @Query("UPDATE Operation o SET o.child = ?2 WHERE o = ?1")
    void setChild(OperationEntity operation, OperationEntity child);

    @Modifying
    @Query("UPDATE Operation o SET o.done = ?2, o.failed = ?3 WHERE o = ?1")
    void setStatus(OperationEntity operation, boolean done, boolean failed);
}
