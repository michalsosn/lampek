package pl.lodz.p.michalsosn.entities;

import javax.persistence.*;
import java.io.Serializable;

/**
 * @author Michał Sośnicki
 */
@Entity
@Table(name = "operation")
@SequenceGenerator(name = "operation_sequence",
                   sequenceName = "operation_sequence")
public class OperationEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE,
                    generator = "operation_sequence")
    @Column(name = "operation_id", nullable = false, updatable = false)
    private long id;

}
