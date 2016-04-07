package pl.lodz.p.michalsosn.rest.support;

import org.springframework.hateoas.ResourceSupport;
import pl.lodz.p.michalsosn.rest.ProcessRestController;
import pl.lodz.p.michalsosn.rest.SpecificationRestController;
import pl.lodz.p.michalsosn.specification.OperationSpecification;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

/**
 * @author Michał Sośnicki
 */
public class SpecificationSupport extends ResourceSupport {

    private final OperationSpecification[] specifications;

    public SpecificationSupport(String username, String processName) {
        this(username, processName, OperationSpecification.values());
    }

    public SpecificationSupport(String username, String processName,
                                OperationSpecification[] specifications) {
        this.specifications = specifications;
        add(linkTo(methodOn(SpecificationRestController.class)
                .getSpecifications(username, processName))
                .withSelfRel());
        add(linkTo(methodOn(ProcessRestController.class)
                .getProcessEntity(username, processName))
                .withRel("processes"));
    }

    public OperationSpecification[] getSpecifications() {
        return specifications;
    }
}
