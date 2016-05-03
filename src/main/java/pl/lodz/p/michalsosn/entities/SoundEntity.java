package pl.lodz.p.michalsosn.entities;

import pl.lodz.p.michalsosn.io.SoundIO;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.time.Instant;

/**
 * @author Michał Sośnicki
 */
@Entity(name = "Sound")
@Table(name = "sound",
       indexes = {@Index(columnList = "account_id, name", unique = true)})
@SequenceGenerator(name = "sound_sequence", sequenceName = "sound_sequence",
                   allocationSize = 1)
public class SoundEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE,
                    generator = "sound_sequence")
    @Column(name = "sound_id", nullable = false, updatable = false)
    private long id;

    @NotNull
    @Size(min = 1, max = 64)
    @Pattern(regexp = "^[A-Za-z0-9_]+$")
    @Column(name = "name", nullable = false, length = 64)
    private String name;

    @Column(name = "modification_time", nullable = false)
    private Instant modificationTime;

    @ManyToOne(optional = false)
    @JoinColumn(name = "account_id", referencedColumnName = "account_id",
                nullable = false)
    private AccountEntity account;

    @Lob
    @Column(name = "data", nullable = false)
    @Basic(fetch = FetchType.LAZY, optional = false)
    private byte[] data;

    public SoundEntity() {
    }

    public SoundEntity(String name, AccountEntity account, InputStream inputStream)
            throws IOException {
        this.name = name;
        this.account = account;
        setDataWithConversion(inputStream);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Instant getModificationTime() {
        return modificationTime;
    }

    public AccountEntity getAccount() {
        return account;
    }

    public void setAccount(AccountEntity account) {
        this.account = account;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public void setDataWithConversion(InputStream inputStream) throws IOException {
        this.data = SoundIO.convertAudio(inputStream);
    }

    public void setDataWithConversion(byte[] inputData) throws IOException {
        this.data = SoundIO.convertAudio(inputData);
    }

    @PrePersist
    @PreUpdate
    private void updateModificationTime() {
        modificationTime = Instant.now();
    }

    @Override
    public String toString() {
        return "SoundEntity{"
              + "id=" + id
              + ", name='" + name + '\''
              + ", modificationTime=" + modificationTime
              + ", account=" + account
              + '}';
    }
}
