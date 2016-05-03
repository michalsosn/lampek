package pl.lodz.p.michalsosn.specification;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.context.ApplicationContext;
import pl.lodz.p.michalsosn.entities.ArgumentEntity;
import pl.lodz.p.michalsosn.entities.SoundEntity;
import pl.lodz.p.michalsosn.repository.SoundRepository;

import java.lang.reflect.Field;

/**
 * @author Michał Sośnicki
 */
public class SoundParameterSpecification extends ParameterSpecification {

    @JsonIgnore
    private Field nameField;

    SoundParameterSpecification() {
    }

    public SoundParameterSpecification(
            Class<SoundEntity> tClass, String soundField, String nameField
    ) throws NoSuchFieldException {
        this(null, tClass, soundField, nameField);
    }

    public SoundParameterSpecification(String description, Class tClass,
                                       String soundField, String nameField
    ) throws NoSuchFieldException {
        super(description, tClass, soundField);
        this.nameField = tClass.getDeclaredField(nameField);
        if (getField().getType() != SoundEntity.class) {
            throw new NoSuchFieldException("Wrong sound field type");
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
        SoundRepository soundRepository
                = context.getBean(SoundRepository.class);
        SoundEntity entity = soundRepository
                .findByAccountUsernameAndName(username, name).get();
        setValue(object, entity);
        return new ArgumentEntity.SoundArgumentEntity(entity);
    }

    @Override
    public void applyArgumentEntity(Object object,
                                    ArgumentEntity argument) {
        ArgumentEntity.SoundArgumentEntity soundArgument
                = (ArgumentEntity.SoundArgumentEntity) argument;
        setValue(object, soundArgument.getSound());
        setValue(object, soundArgument.getSound().getName(), nameField);
    }
}
