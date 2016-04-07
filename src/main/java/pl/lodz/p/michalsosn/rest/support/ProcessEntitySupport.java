package pl.lodz.p.michalsosn.rest.support;

import org.springframework.hateoas.ResourceSupport;
import pl.lodz.p.michalsosn.entities.ProcessEntity;
import pl.lodz.p.michalsosn.rest.OperationRestController;
import pl.lodz.p.michalsosn.rest.ProcessRestController;
import pl.lodz.p.michalsosn.rest.SpecificationRestController;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

/**
 * @author Michał Sośnicki
 */
public class ProcessEntitySupport extends ResourceSupport {
    private final String name;
    private final String username;

    public ProcessEntitySupport(ProcessEntity processEntity) {
        this.name = processEntity.getName();
        this.username = processEntity.getAccount().getUsername();

        add(linkTo(methodOn(ProcessRestController.class)
                .getProcessEntity(username, name))
                .withSelfRel());
        add(linkTo(methodOn(ProcessRestController.class)
                .listProcesses(username, null, null))
                .withRel("processes"));
        add(linkTo(methodOn(OperationRestController.class)
                .listOperations(username, name))
                .withRel("operations"));
        add(linkTo(methodOn(SpecificationRestController.class)
                .getSpecifications(username, name))
                .withRel("operation specifications"));
    }

    public String getName() {
        return name;
    }

    public String getUsername() {
        return username;
    }
}

