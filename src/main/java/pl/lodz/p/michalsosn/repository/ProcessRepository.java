package pl.lodz.p.michalsosn.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import pl.lodz.p.michalsosn.entities.OperationEntity;
import pl.lodz.p.michalsosn.entities.ProcessEntity;

import java.util.Optional;

/**
 * @author Michał Sośnicki
 */
@Transactional(propagation = Propagation.MANDATORY)
public interface ProcessRepository extends PagingAndSortingRepository<ProcessEntity, Long> {
    Page<ProcessEntity> findByAccountUsername(
            String username, Pageable pageable
    );
    Optional<ProcessEntity> findByAccountUsernameAndName(
            String username, String name
    );
    void deleteByAccountUsernameAndName(
            String username, String name
    );

    @Modifying
    @Query("UPDATE Process p SET p.operation = ?2 WHERE p = ?1")
    void setInitialOperation(ProcessEntity process, OperationEntity child);
}
