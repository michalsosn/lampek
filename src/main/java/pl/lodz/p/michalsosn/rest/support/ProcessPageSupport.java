package pl.lodz.p.michalsosn.rest.support;

import org.springframework.data.domain.Page;
import org.springframework.hateoas.ResourceSupport;
import pl.lodz.p.michalsosn.entities.ProcessEntity;
import pl.lodz.p.michalsosn.rest.ProcessRestController;

import java.time.Instant;
import java.util.List;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

/**
 * @author Michał Sośnicki
 */
public class ProcessPageSupport extends ResourceSupport {

    private final List<ProcessItemSupport> processList;
    private final int pageCount;

    public static class ProcessItemSupport extends ResourceSupport {
        private final String name;
        private final Instant modificationTime;

        public ProcessItemSupport(String username, ProcessEntity process) {
            this.name = process.getName();
            this.modificationTime = process.getModificationTime();
            add(linkTo(methodOn(ProcessRestController.class)
                    .getProcessEntity(username, name))
                    .withSelfRel());
        }

        public String getName() {
            return name;
        }

        public Instant getModificationTime() {
            return modificationTime;
        }
    }

    public ProcessPageSupport(String username, Page<ProcessEntity> processPage) {
        this.processList = processPage
                .map(process -> new ProcessItemSupport(username, process))
                .getContent();
        this.pageCount = processPage.getTotalPages();
        if (processPage.hasPrevious()) {
            add(linkTo(methodOn(ProcessRestController.class)
                    .listProcesses(username, processPage.getNumber() - 1,
                            processPage.getSize()))
                    .withRel("previous"));
        }
        add(linkTo(methodOn(ProcessRestController.class)
                .listProcesses(username, processPage.getNumber(),
                               processPage.getSize()))
                .withSelfRel());
        if (processPage.hasNext()) {
            add(linkTo(methodOn(ProcessRestController.class)
                    .listProcesses(username, processPage.getNumber() + 1,
                                   processPage.getSize()))
                    .withRel("next"));
        }
    }

    public List<ProcessItemSupport> getProcessList() {
        return processList;
    }

    public int getPageCount() {
        return pageCount;
    }
}
