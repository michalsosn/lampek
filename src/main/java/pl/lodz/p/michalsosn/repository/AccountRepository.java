package pl.lodz.p.michalsosn.repository;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import pl.lodz.p.michalsosn.entities.AccountEntity;

import java.util.Optional;

/**
 * @author Michał Sośnicki
 */
@Transactional(propagation = Propagation.MANDATORY)
public interface AccountRepository extends PagingAndSortingRepository<AccountEntity, Long> {
    Optional<AccountEntity> findByUsername(String username);
}
