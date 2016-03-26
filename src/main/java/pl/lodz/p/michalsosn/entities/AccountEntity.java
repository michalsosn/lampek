package pl.lodz.p.michalsosn.entities;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Michał Sośnicki
 */
@Entity
@Table(name = "account")
@SequenceGenerator(name = "account_sequence", sequenceName = "account_sequence")
public class AccountEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "account_sequence")
    @Column(name = "account_id", nullable = false, updatable = false)
    private long id;

    @NotNull
    @Size(min = 4, max = 32) // , message = "{account.username.size}"
    @Column(name = "username", nullable = false, length = 32, unique = true)
    private String username;

    @NotNull
    @Size(min = 59, max = 60)
    @Column(name = "password", columnDefinition = "char(60) not null")
    private String password;

    @OneToMany(mappedBy = "account", orphanRemoval = true)
    private List<ImageEntity> images = new ArrayList<>();

//    @OneToMany(mappedBy = "account", orphanRemoval = true)
//    private List<ProcessEntity> processes = new ArrayList<>();

    AccountEntity() {
    }

    public AccountEntity(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public List<ImageEntity> getImages() {
        return images;
    }

    @Override
    public String toString() {
        return "AccountEntity{" +
                "username='" + username + '\'' +
                ", id=" + id +
                '}';
    }
}
