package pl.lodz.p.michalsosn.rest.support;

import org.springframework.data.domain.Page;
import org.springframework.hateoas.ResourceSupport;
import pl.lodz.p.michalsosn.rest.ProcessRestController;

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

        public ProcessNameSupport(String username, String name) {
            this.name = name;
            add(linkTo(methodOn(ProcessRestController.class)
                    .getProcessEntity(username, name))
                    .withSelfRel());
        }

        public String getName() {
            return name;
        }
    }

    public ProcessPageSupport(String username, Page<String> namePage) {
        this.nameList = namePage
                .map(name -> new ProcessNameSupport(username, name))
                .getContent();
        this.pageCount = namePage.getTotalPages();
        if (namePage.hasPrevious()) {
            add(linkTo(methodOn(ProcessRestController.class)
                    .listProcesses(username, namePage.getNumber() - 1,
                            namePage.getSize()))
                    .withRel("previous"));
        }
        add(linkTo(methodOn(ProcessRestController.class)
                .listProcesses(username, namePage.getNumber(),
                               namePage.getSize()))
                .withSelfRel());
        if (namePage.hasNext()) {
            add(linkTo(methodOn(ProcessRestController.class)
                    .listProcesses(username, namePage.getNumber() + 1,
                                   namePage.getSize()))
                    .withRel("next"));
        }
    }

    public List<ProcessNameSupport> getNameList() {
        return nameList;
    }

    public int getPageCount() {
        return pageCount;
    }
}
