package pl.lodz.p.michalsosn.entities;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Michał Sośnicki
 */
@Entity(name = "Process")
@Table(name = "process",
       indexes = {@Index(columnList = "account_id, name", unique = true)})
@SequenceGenerator(name = "process_sequence", sequenceName = "process_sequence",
                   allocationSize = 1)
public class ProcessEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE,
                    generator = "process_sequence")
    @Column(name = "process_id", nullable = false, updatable = false)
    private long id;

    @NotNull
    @Size(min = 1, max = 64)
    @Pattern(regexp = "^[A-Za-z0-9_]+$")
    @Column(name = "name", nullable = false, length = 64)
    private String name;

    @OneToOne
    @JoinColumn(name = "operation_id", referencedColumnName = "operation_id")
    private OperationEntity operation;

    @OneToMany(mappedBy = "process", orphanRemoval = true)
    private List<OperationEntity> operations = new ArrayList<>();

    @ManyToOne(optional = false)
    @JoinColumn(name = "account_id", referencedColumnName = "account_id",
                nullable = false)
    private AccountEntity account;

    ProcessEntity() {
    }

    public ProcessEntity(String name, AccountEntity account) {
        this.name = name;
        this.account = account;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public OperationEntity getOperation() {
        return operation;
    }

    public void setOperation(OperationEntity operation) {
        this.operation = operation;
    }

    public AccountEntity getAccount() {
        return account;
    }

    public void setAccount(AccountEntity account) {
        this.account = account;
    }

    @Override
    public String toString() {
        return "ProcessEntity{"
             + "name='" + name + '\''
             + ", id=" + id
             + '}';
    }
}
