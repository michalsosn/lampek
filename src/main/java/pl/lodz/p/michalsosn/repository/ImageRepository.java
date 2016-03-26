package pl.lodz.p.michalsosn.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import pl.lodz.p.michalsosn.entities.ImageEntity;

import java.util.Optional;

/**
 * @author Michał Sośnicki
 */
@Transactional(propagation = Propagation.MANDATORY)
public interface ImageRepository extends PagingAndSortingRepository<ImageEntity, Long> {
    Page<ImageEntity> findByAccountUsername(String username, Pageable pageable);
    Optional<ImageEntity> findByAccountUsernameAndName(String username, String name);
    void deleteByAccountUsernameAndName(String username, String name);
}
