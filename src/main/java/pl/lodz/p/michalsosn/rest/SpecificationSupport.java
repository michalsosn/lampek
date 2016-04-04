package pl.lodz.p.michalsosn.rest;

import org.springframework.hateoas.ResourceSupport;
import pl.lodz.p.michalsosn.entities.specification.OperationSpecification;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

/**
 * @author Michał Sośnicki
 */
public class SpecificationSupport extends ResourceSupport {

    private final OperationSpecification[] specifications;

    public SpecificationSupport(String processName) {
        this(processName, OperationSpecification.values());
    }

    public SpecificationSupport(String processName,
                                OperationSpecification[] specifications) {
        this.specifications = specifications;
        add(linkTo(methodOn(SpecificationRestController.class)
                .getSpecifications(processName, null))
                .withSelfRel());
        add(linkTo(methodOn(ProcessRestController.class)
                .getProcessEntity(processName, null))
                .withRel("processes"));
    }

    public OperationSpecification[] getSpecifications() {
        return specifications;
    }
}
