package pl.lodz.p.michalsosn.rest;

import org.springframework.hateoas.ResourceSupport;
import pl.lodz.p.michalsosn.entities.specification.OperationSpecification;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

/**
 * @author Michał Sośnicki
 */
public class SpecificationSupport extends ResourceSupport {

    private final OperationSpecification[] specification;

    public SpecificationSupport() {
        this(OperationSpecification.values());
    }

    public SpecificationSupport(OperationSpecification[] specification) {
        this.specification = specification;
        add(linkTo(methodOn(ProcessRestController.class).getSpecification())
                .withSelfRel());
        add(linkTo(methodOn(ProcessRestController.class)
                .listProcesses(null, null, null))
                .withRel("processes"));
    }

    public OperationSpecification[] getSpecification() {
        return specification;
    }
}
