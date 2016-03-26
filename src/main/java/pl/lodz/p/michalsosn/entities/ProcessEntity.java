package pl.lodz.p.michalsosn.entities;

import javax.persistence.*;
import java.io.Serializable;

/**
 * @author Michał Sośnicki
 */
@Entity
@Table(name = "process")
@SequenceGenerator(name = "process_sequence", sequenceName = "process_sequence")
public class ProcessEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE,
                    generator = "process_sequence")
    @Column(name = "process_id", nullable = false, updatable = false)
    private long id;

}
