package pl.lodz.p.michalsosn.specification;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.context.ApplicationContext;
import pl.lodz.p.michalsosn.entities.ArgumentEntity;
import pl.lodz.p.michalsosn.entities.ImageEntity;
import pl.lodz.p.michalsosn.repository.ImageRepository;

import java.lang.reflect.Field;

/**
 * @author Michał Sośnicki
 */
public class ImageParameterSpecification extends ParameterSpecification {

    @JsonIgnore
    private Field nameField;

    ImageParameterSpecification() {
    }

    public ImageParameterSpecification(
            Class<ImageEntity> tClass, String imageField, String nameField
    ) throws NoSuchFieldException {
        this(null, tClass, imageField, nameField);
    }

    public ImageParameterSpecification(String description, Class tClass,
                                       String imageField, String nameField
    ) throws NoSuchFieldException {
        super(description, tClass, imageField);
        this.nameField = tClass.getDeclaredField(nameField);
        if (getField().getType() != ImageEntity.class) {
            throw new NoSuchFieldException("Wrong image field type");
        }
        if (this.nameField.getType() != String.class) {
            throw new NoSuchFieldException("Wrong name field type");
        }
    }

    @Override
    public ArgumentEntity createArgumentEntity(
            Object object, ApplicationContext context, String username
    ) {
        String name = (String) getValue(object, nameField);
        ImageRepository imageRepository
                = context.getBean(ImageRepository.class);
        ImageEntity entity = imageRepository
                .findByAccountUsernameAndName(username, name).get();
        setValue(object, entity);
        return new ArgumentEntity.ImageArgumentEntity(entity);
    }

    @Override
    public void applyArgumentEntity(Object object,
                                    ArgumentEntity argument) {
        ArgumentEntity.ImageArgumentEntity imageArgument
                = (ArgumentEntity.ImageArgumentEntity) argument;
        setValue(object, imageArgument.getImage());
        setValue(object, imageArgument.getImage().getName(), nameField);
    }
}
