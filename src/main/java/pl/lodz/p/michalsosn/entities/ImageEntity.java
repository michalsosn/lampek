package pl.lodz.p.michalsosn.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.imageio.ImageIO;
import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;

/**
 * @author Michał Sośnicki
 */
@Entity
@Table(name = "image",
       indexes = {@Index(columnList = "account_id, name", unique = true)})
@SequenceGenerator(name = "image_sequence", sequenceName = "image_sequence")
public class ImageEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "image_sequence")
    @Column(name = "image_id", nullable = false, updatable = false)
    private long id;

    @NotNull
    @Size(min = 1, max = 255)
    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @JsonIgnore
    @Transient
    private BufferedImage image;

    @ManyToOne(optional = false)
    @JoinColumn(name = "account_id", referencedColumnName = "account_id", nullable = false)
    private AccountEntity account;

    public ImageEntity() {
    }

    public ImageEntity(String name, BufferedImage image, AccountEntity account) {
        this.name = name;
        this.image = image;
        this.account = account;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BufferedImage getImage() {
        return image;
    }

    public void setImage(BufferedImage image) {
        this.image = image;
    }

    @Access(AccessType.PROPERTY)
    @Lob
    @Column(name = "data", nullable = false)
    public byte[] getData() throws IOException {
        ByteArrayOutputStream arrayOutputStream = new ByteArrayOutputStream();
        ImageIO.write(image, "png", arrayOutputStream);
        return arrayOutputStream.toByteArray();
    }

    public void setData(byte[] data) throws IOException {
        ByteArrayInputStream arrayInputStream = new ByteArrayInputStream(data);
        image = ImageIO.read(arrayInputStream);
    }

    public AccountEntity getAccount() {
        return account;
    }

    public void setAccount(AccountEntity account) {
        this.account = account;
    }

    @Override
    public String toString() {
        return "ImageEntity{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", account=" + account +
                '}';
    }
}
