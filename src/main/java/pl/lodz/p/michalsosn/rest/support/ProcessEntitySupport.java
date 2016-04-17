package pl.lodz.p.michalsosn.rest.support;

import org.springframework.hateoas.ResourceSupport;
import pl.lodz.p.michalsosn.entities.ProcessEntity;
import pl.lodz.p.michalsosn.rest.OperationRestController;
import pl.lodz.p.michalsosn.rest.ProcessRestController;
import pl.lodz.p.michalsosn.rest.SpecificationRestController;

import java.time.Instant;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

/**
 * @author Michał Sośnicki
 */
public class ProcessEntitySupport extends ResourceSupport {
    private final String name;
    private final Instant modificationTime;
    private final String account;

    public ProcessEntitySupport(ProcessEntity processEntity) {
        this.name = processEntity.getName();
        this.modificationTime = processEntity.getModificationTime();
        this.account = processEntity.getAccount().getUsername();

        add(linkTo(methodOn(ProcessRestController.class)
                .getProcessEntity(account, name))
                .withSelfRel());
        add(linkTo(methodOn(ProcessRestController.class)
                .listProcesses(account, null, null))
                .withRel("processes"));
        add(linkTo(methodOn(OperationRestController.class)
                .listOperations(account, name))
                .withRel("operations"));
        add(linkTo(methodOn(SpecificationRestController.class)
                .getSpecifications(account, name))
                .withRel("operation specifications"));
    }

    public String getName() {
        return name;
    }

    public Instant getModificationTime() {
        return modificationTime;
    }

    public String getAccount() {
        return account;
    }
}

