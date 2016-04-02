package pl.lodz.p.michalsosn.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.lodz.p.michalsosn.entities.AccountEntity;
import pl.lodz.p.michalsosn.entities.ProcessEntity;
import pl.lodz.p.michalsosn.entities.specification.OperationRequest;
import pl.lodz.p.michalsosn.repository.AccountRepository;
import pl.lodz.p.michalsosn.repository.ProcessRepository;

import java.util.Optional;

/**
 * @author Michał Sośnicki
 */
@Service
@Transactional
public class ProcessService {

    private final Logger log = LoggerFactory.getLogger(ProcessService.class);

    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private ProcessRepository processRepository;
    @Autowired
    private OperationService operationService;

    public ProcessService() {
    }

    public Page<String> listProcessNames(String username, int page, int size) {
        Pageable pageRequest = new PageRequest(
                page, size, Sort.Direction.ASC, "name"
        );
        return processRepository
                .findByAccountUsername(username, pageRequest)
                .map(ProcessEntity::getName);
    }

    public ProcessEntity getProcessEntity(String username, String name) {
        return processRepository
                .findByAccountUsernameAndName(username, name)
                .get();
    }

    public ReplaceResult replaceProcess(
            String username, String name,
            OperationRequest.StartRequest operationRequest
    ) {
        AccountEntity account
                = accountRepository.findByUsername(username).get();

        Optional<ProcessEntity> replaced = processRepository
                .findByAccountUsernameAndName(username, name);
        boolean found = replaced.isPresent();

        long id;
        if (found) {
            ProcessEntity processEntity = replaced.get();
            id = operationService.replaceOperation(
                    username, processEntity.getName(),
                    processEntity.getOperation().getId(), operationRequest
            );
        } else {
            ProcessEntity processEntity
                    = new ProcessEntity(name, account);
            processRepository.save(processEntity);
            id = operationService.acceptRequest(
                    username, processEntity.getName(), null, operationRequest
            );
        }

        log.info("Process {} {} by {}.",
                name, found ? "replaced" : "created", username
        );
        return new ReplaceResult(id, found);
    }

    public void deleteProcess(String username, String name) {
        processRepository.deleteByAccountUsernameAndName(username, name);
        log.info("Process {} deleted by {}.", name, username);
    }

    public static class ReplaceResult {
        private final long id;
        private final boolean found;

        public ReplaceResult(long id, boolean found) {
            this.id = id;
            this.found = found;
        }

        public long getId() {
            return id;
        }

        public boolean isFound() {
            return found;
        }
    }

}