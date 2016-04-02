package pl.lodz.p.michalsosn.entities;

import pl.lodz.p.michalsosn.io.BufferedImageIO;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.Serializable;

/**
 * @author Michał Sośnicki
 */
@Entity(name = "Image")
@Table(name = "image",
       indexes = {@Index(columnList = "account_id, name", unique = true)})
@SequenceGenerator(name = "image_sequence", sequenceName = "image_sequence",
                   allocationSize = 1)
public class ImageEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE,
                    generator = "image_sequence")
    @Column(name = "image_id", nullable = false, updatable = false)
    private long id;

    @NotNull
    @Size(min = 1, max = 64)
    @Pattern(regexp = "^[A-Za-z0-9_]+$")
    @Column(name = "name", nullable = false, length = 64)
    private String name;

    @Lob
    @Column(name = "data", nullable = false)
    @Basic(fetch = FetchType.LAZY, optional = false)
    private byte[] data;

    @ManyToOne(optional = false)
    @JoinColumn(name = "account_id", referencedColumnName = "account_id",
                nullable = false)
    private AccountEntity account;

    public ImageEntity() {
    }

    public ImageEntity(String name, byte[] data, AccountEntity account) {
        this.name = name;
        this.data = data;
        this.account = account;
    }

    public ImageEntity(String name, BufferedImage image,
                       AccountEntity account) throws IOException {
        this.name = name;
        this.account = account;
        setImage(image);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BufferedImage getImage() throws IOException {
        return BufferedImageIO.fromByteArray(data);
    }

    public void setImage(BufferedImage image) throws IOException {
        data = BufferedImageIO.toByteArray(image);
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public AccountEntity getAccount() {
        return account;
    }

    public void setAccount(AccountEntity account) {
        this.account = account;
    }

    @Override
    public String toString() {
        return "ImageEntity{"
              + "id=" + id
              + ", name='" + name + '\''
              + ", account=" + account
              + '}';
    }
}
