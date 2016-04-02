package pl.lodz.p.michalsosn.entities;

import pl.lodz.p.michalsosn.entities.specification.OperationRequest;
import pl.lodz.p.michalsosn.entities.specification.OperationSpecification;

import javax.persistence.*;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Michał Sośnicki
 */
@Entity(name = "Operation")
@Table(name = "operation")
@SequenceGenerator(name = "operation_sequence",
                   sequenceName = "operation_sequence",
                   allocationSize = 1)
public class OperationEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE,
                    generator = "operation_sequence")
    @Column(name = "operation_id", nullable = false, updatable = false)
    private long id;

    @Column(name = "done", nullable = false)
    private boolean done;

    @Column(name = "failed", nullable = false)
    private boolean failed;

    @Enumerated(EnumType.ORDINAL)
    @Column(name = "type", nullable = false, updatable = false)
    private OperationSpecification specification;

    @Column(name = "previous_result", updatable = false)
    private String previousResult;

    @ManyToOne(optional = false)
    @JoinColumn(name = "process_id", referencedColumnName = "process_id",
                nullable = false, updatable = false)
    private ProcessEntity process;

    @OneToOne(mappedBy = "child")
    private OperationEntity parent;

    @OneToOne(orphanRemoval = true)
    @JoinColumn(name = "child_id", referencedColumnName = "operation_id",
                unique = true)
    private OperationEntity child;

    @OneToMany(mappedBy = "operation", cascade = CascadeType.ALL,
               orphanRemoval = true)
    @MapKey(name = "role")
    private Map<String, ArgumentEntity> arguments = new HashMap<>();

    @OneToMany(mappedBy = "operation", cascade = CascadeType.ALL,
               orphanRemoval = true)
    @MapKey(name = "role")
    private Map<String, ResultEntity> results = new HashMap<>();

    OperationEntity() {
    }

    public OperationEntity(OperationSpecification specification,
                           String previousResult,
                           ProcessEntity process) {
        this.specification = specification;
        this.previousResult = previousResult;
        this.process = process;
    }

    public OperationRequest dentitize() {
        return getSpecification().dentitize(this);
    }

    public long getId() {
        return id;
    }

    public boolean isDone() {
        return done;
    }

    public void setDone(boolean done) {
        this.done = done;
    }

    public boolean isFailed() {
        return failed;
    }

    public void setFailed(boolean failed) {
        this.failed = failed;
    }

    public OperationSpecification getSpecification() {
        return specification;
    }

    public String getPreviousResult() {
        return previousResult;
    }

    public ProcessEntity getProcess() {
        return process;
    }

    public void setProcess(ProcessEntity process) {
        this.process = process;
    }

    public OperationEntity getParent() {
        return parent;
    }

    public void setParent(OperationEntity parent) {
        this.parent = parent;
    }

    public OperationEntity getChild() {
        return child;
    }

    public void setChild(OperationEntity child) {
        this.child = child;
    }

    public Map<String, ArgumentEntity> getArguments() {
        return arguments;
    }

    public void setArguments(Map<String, ArgumentEntity> arguments) {
        this.arguments = arguments;
    }

    public Map<String, ResultEntity> getResults() {
        return results;
    }

    public void setResults(Map<String, ResultEntity> results) {
        this.results = results;
    }

    @Override
    public String toString() {
        return "OperationEntity{"
             + "results=" + results
             + ", arguments=" + arguments
             + ", specification='" + specification + '\''
             + ", id=" + id
             + '}';
    }
}
