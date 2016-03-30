package pl.lodz.p.michalsosn.entities;

import javax.persistence.*;
import java.io.Serializable;

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

//    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
//    @JoinColumn(name = "settingspackage_id")
//    @MapKeyColumn(name = "key")
//    private Map<String, ArgumentEntity> entries = new HashMap<>();

//    @Enumerated(EnumType.STRING)
//    private EntryType type;

}
