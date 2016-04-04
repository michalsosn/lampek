package pl.lodz.p.michalsosn.rest;

import org.springframework.hateoas.ResourceSupport;
import pl.lodz.p.michalsosn.entities.ProcessEntity;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

/**
 * @author Michał Sośnicki
 */
public class ProcessEntitySupport extends ResourceSupport {
    private final String name;
    private final String account;

    public ProcessEntitySupport(ProcessEntity processEntity) {
        this.name = processEntity.getName();
        this.account = processEntity.getAccount().getUsername();

        add(linkTo(methodOn(ProcessRestController.class)
                .getProcessEntity(name, null))
                .withSelfRel());
        add(linkTo(methodOn(ProcessRestController.class)
                .listProcesses(null, null, null))
                .withRel("processes"));
        add(linkTo(methodOn(OperationRestController.class)
                .listOperations(name, null))
                .withRel("operations"));
        add(linkTo(methodOn(SpecificationRestController.class)
                .getSpecifications(name, null))
                .withRel("operation specifications"));
    }

    public String getName() {
        return name;
    }

    public String getAccount() {
        return account;
    }
}

