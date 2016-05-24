package pl.lodz.p.michalsosn.specification;

import org.springframework.context.ApplicationContext;
import pl.lodz.p.michalsosn.entities.ArgumentEntity;

/**
 * @author Michał Sośnicki
 */
public class EnumParameterSpecification<E extends Enum<E>> extends ParameterSpecification {

    private Class<E> enumClass;

    EnumParameterSpecification() {
    }

    public <T> EnumParameterSpecification(
            Class<T> tClass, String name, Class<E> enumClass
    ) throws NoSuchFieldException {
        this(null, tClass, name, enumClass);
    }

    public <T> EnumParameterSpecification(
            String description, Class<T> tClass, String name, Class<E> enumClass
    ) throws NoSuchFieldException {
        super(description, tClass, name);
        if (!getField().getType().equals(enumClass)) {
            throw new NoSuchFieldException("Field not of the enum class");
        }
        this.enumClass = enumClass;
    }

    public E[] getValues() {
        return enumClass.getEnumConstants();
    }

    @Override
    public ArgumentEntity createArgumentEntity(
            Object object, ApplicationContext context, String username
    ) {
        E enumValue = (E) getValue(object);
        return new ArgumentEntity.EnumArgumentEntity<>(enumValue);
    }

    @Override
    public void applyArgumentEntity(Object object, ArgumentEntity argument) {
        ArgumentEntity.EnumArgumentEntity<E> enumArgument
                = (ArgumentEntity.EnumArgumentEntity<E>) argument;
        setValue(object, enumArgument.getValue(enumClass));
    }
}

