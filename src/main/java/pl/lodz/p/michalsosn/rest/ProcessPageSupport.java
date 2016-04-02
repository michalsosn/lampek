package pl.lodz.p.michalsosn.rest;

import org.springframework.data.domain.Page;
import org.springframework.hateoas.ResourceSupport;

import java.util.List;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

/**
 * @author Michał Sośnicki
 */
public class ProcessPageSupport extends ResourceSupport {

    private final List<ProcessNameSupport> nameList;
    private final int pageCount;

    public static class ProcessNameSupport extends ResourceSupport {
        private final String name;

        public ProcessNameSupport(String name) {
            this.name = name;
            add(linkTo(methodOn(ProcessRestController.class)
                    .getProcessEntity(name, null))
                    .withSelfRel());
        }

        public String getName() {
            return name;
        }
    }

    public ProcessPageSupport(Page<String> namePage) {
        this.nameList = namePage.map(ProcessNameSupport::new).getContent();
        this.pageCount = namePage.getTotalPages();
        if (namePage.hasPrevious()) {
            add(linkTo(methodOn(ProcessRestController.class)
                    .listProcesses(namePage.getNumber() - 1,
                            namePage.getSize(), null))
                    .withRel("previous"));
        }
        add(linkTo(methodOn(ProcessRestController.class)
                .listProcesses(namePage.getNumber(), namePage.getSize(), null))
                .withSelfRel());
        if (namePage.hasNext()) {
            add(linkTo(methodOn(ProcessRestController.class)
                    .listProcesses(namePage.getNumber() + 1,
                            namePage.getSize(), null))
                    .withRel("next"));
        }
        add(linkTo(methodOn(ProcessRestController.class).getSpecification())
                .withRel("available operations"));
    }

    public List<ProcessNameSupport> getNameList() {
        return nameList;
    }

    public int getPageCount() {
        return pageCount;
    }
}
